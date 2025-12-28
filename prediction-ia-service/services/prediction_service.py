from sqlalchemy.orm import Session
from database import Prediction, StudentTrajectory, RiskModule
from models.gnn_model import PredictionModel
import numpy as np
import redis
import json
from datetime import datetime
import os
from dotenv import load_dotenv

load_dotenv()

# Redis client
redis_client = redis.Redis(
    host=os.getenv("REDIS_HOST", "localhost"),
    port=int(os.getenv("REDIS_PORT", "6379")),
    db=0,
    decode_responses=True
)

# Modèle de prédiction
model = PredictionModel(
    model_path=os.getenv("MODEL_PATH", "models/trained_model.pth"),
    device=os.getenv("DEVICE", "cpu")
)


class PredictionService:
    
    def __init__(self, db: Session):
        self.db = db
    
    def extract_features(self, student_id: str, module_id: int):
        """
        Extraire les features pour la prédiction
        En production, cela récupérerait les données depuis les autres services
        """
        # Pour l'instant, générer des features synthétiques
        # En production, on récupérerait :
        # - Notes actuelles depuis note-service
        # - Présences depuis activities-service
        # - Historique depuis les autres services
        
        num_nodes = 10
        node_features = np.random.randn(num_nodes, 32).astype(np.float32)
        
        # Générer des edges
        num_edges = np.random.randint(num_nodes, num_nodes * 2)
        edge_index = []
        for _ in range(num_edges):
            src = np.random.randint(0, num_nodes)
            dst = np.random.randint(0, num_nodes)
            if src != dst:
                edge_index.append([src, dst])
        
        if len(edge_index) == 0:
            edge_index = [[0, 1]]
        
        edge_index = np.array(edge_index).T.astype(np.int64)
        batch = np.zeros(num_nodes, dtype=np.int64)
        
        return {
            'node_features': node_features.tolist(),
            'edge_index': edge_index.tolist(),
            'batch': batch.tolist()
        }
    
    def predict_student_module(self, student_id: str, module_id: int, use_cache=True):
        """Prédire la trajectoire d'un étudiant pour un module"""
        cache_key = f"prediction:{student_id}:{module_id}"
        
        # Vérifier le cache Redis
        if use_cache:
            cached = redis_client.get(cache_key)
            if cached:
                return json.loads(cached)
        
        # Extraire les features
        features = self.extract_features(student_id, module_id)
        
        # Faire la prédiction
        success_prob, dropout_prob, predicted_grade, confidence = model.predict(features)
        risk_level = model.get_risk_level(success_prob, dropout_prob)
        
        # Sauvegarder en base de données
        prediction = Prediction(
            student_id=student_id,
            module_id=module_id,
            success_probability=success_prob,
            dropout_probability=dropout_prob,
            risk_level=risk_level,
            predicted_grade=predicted_grade,
            confidence_score=confidence,
            features=features,
            model_version="1.0.0"
        )
        self.db.add(prediction)
        self.db.commit()
        self.db.refresh(prediction)
        
        result = {
            'id': prediction.id,
            'student_id': student_id,
            'module_id': module_id,
            'success_probability': success_prob,
            'dropout_probability': dropout_prob,
            'risk_level': risk_level,
            'predicted_grade': predicted_grade,
            'confidence_score': confidence,
            'created_at': prediction.created_at.isoformat()
        }
        
        # Mettre en cache (expiration 1 heure)
        if use_cache:
            redis_client.setex(cache_key, 3600, json.dumps(result))
        
        return result
    
    def get_student_trajectory(self, student_id: str, module_id: int):
        """Obtenir la trajectoire détaillée d'un étudiant pour un module"""
        # Vérifier si une trajectoire existe
        trajectory = self.db.query(StudentTrajectory).filter_by(
            student_id=student_id,
            module_id=module_id
        ).first()
        
        if trajectory:
            return {
                'id': trajectory.id,
                'student_id': trajectory.student_id,
                'module_id': trajectory.module_id,
                'trajectory_data': trajectory.trajectory_data,
                'milestones': trajectory.milestones,
                'recommendations': trajectory.recommendations,
                'created_at': trajectory.created_at.isoformat(),
                'updated_at': trajectory.updated_at.isoformat()
            }
        
        # Générer une nouvelle trajectoire
        prediction = self.predict_student_module(student_id, module_id)
        
        # Générer des données de trajectoire
        trajectory_data = {
            'current_progress': 0.5,  # En production, calculé depuis les données réelles
            'expected_progress': 0.7,
            'predicted_completion_date': None,
            'milestones': [
                {'date': '2024-02-01', 'milestone': 'Examen 1', 'probability': 0.8},
                {'date': '2024-03-15', 'milestone': 'Examen 2', 'probability': 0.7},
                {'date': '2024-05-01', 'milestone': 'Examen Final', 'probability': prediction['success_probability']}
            ],
            'risk_factors': []
        }
        
        # Générer des recommandations
        recommendations = []
        if prediction['dropout_probability'] > 0.4:
            recommendations.append({
                'type': 'HIGH_DROPOUT_RISK',
                'message': 'Risque élevé d\'abandon détecté',
                'actions': ['Augmenter le suivi', 'Organiser des sessions de soutien']
            })
        if prediction['success_probability'] < 0.5:
            recommendations.append({
                'type': 'LOW_SUCCESS_PROBABILITY',
                'message': 'Probabilité de réussite faible',
                'actions': ['Renforcer les cours', 'Offrir du tutorat']
            })
        
        trajectory = StudentTrajectory(
            student_id=student_id,
            module_id=module_id,
            trajectory_data=trajectory_data,
            milestones=trajectory_data['milestones'],
            recommendations=recommendations
        )
        self.db.add(trajectory)
        self.db.commit()
        self.db.refresh(trajectory)
        
        return {
            'id': trajectory.id,
            'student_id': trajectory.student_id,
            'module_id': trajectory.module_id,
            'trajectory_data': trajectory.trajectory_data,
            'milestones': trajectory.milestones,
            'recommendations': trajectory.recommendations,
            'created_at': trajectory.created_at.isoformat(),
            'updated_at': trajectory.updated_at.isoformat()
        }
    
    def get_risk_modules(self):
        """Obtenir les modules à risque"""
        # Récupérer toutes les prédictions récentes
        predictions = self.db.query(Prediction).all()
        
        # Grouper par module
        module_stats = {}
        for pred in predictions:
            if pred.module_id not in module_stats:
                module_stats[pred.module_id] = {
                    'predictions': [],
                    'module_code': f"MOD{pred.module_id}",
                    'module_name': f"Module {pred.module_id}"
                }
            module_stats[pred.module_id]['predictions'].append(pred)
        
        risk_modules = []
        for module_id, stats in module_stats.items():
            predictions = stats['predictions']
            avg_success = np.mean([p.success_probability for p in predictions])
            avg_dropout = np.mean([p.dropout_probability for p in predictions])
            at_risk_count = sum(1 for p in predictions if p.risk_level in ['HIGH', 'CRITICAL'])
            
            # Calculer le score de risque
            risk_score = (1 - avg_success) * 0.6 + avg_dropout * 0.4
            
            # Créer ou mettre à jour le module à risque
            risk_module = self.db.query(RiskModule).filter_by(module_id=module_id).first()
            if not risk_module:
                risk_module = RiskModule(
                    module_id=module_id,
                    module_code=stats['module_code'],
                    module_name=stats['module_name'],
                    risk_score=risk_score,
                    at_risk_students_count=at_risk_count,
                    average_success_probability=avg_success,
                    average_dropout_probability=avg_dropout
                )
                self.db.add(risk_module)
            else:
                risk_module.risk_score = risk_score
                risk_module.at_risk_students_count = at_risk_count
                risk_module.average_success_probability = avg_success
                risk_module.average_dropout_probability = avg_dropout
            
            self.db.commit()
            self.db.refresh(risk_module)
            
            risk_modules.append({
                'id': risk_module.id,
                'module_id': risk_module.module_id,
                'module_code': risk_module.module_code,
                'module_name': risk_module.module_name,
                'risk_score': risk_module.risk_score,
                'at_risk_students_count': risk_module.at_risk_students_count,
                'average_success_probability': risk_module.average_success_probability,
                'average_dropout_probability': risk_module.average_dropout_probability,
                'updated_at': risk_module.updated_at.isoformat()
            })
        
        # Trier par score de risque décroissant
        risk_modules.sort(key=lambda x: x['risk_score'], reverse=True)
        
        return risk_modules


