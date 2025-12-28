from models import db, Explanation, FeatureImportance
from lime.lime_tabular import LimeTabularExplainer
import numpy as np
import pandas as pd
import requests
import os
from typing import Dict, List, Tuple
from dotenv import load_dotenv

load_dotenv()

PREDICTION_SERVICE_URL = os.getenv('PREDICTION_SERVICE_URL', 'http://localhost:8001')


class ExplanationService:
    
    def __init__(self):
        self.prediction_service_url = PREDICTION_SERVICE_URL
        self.explainer = None
    
    def get_prediction(self, student_id: str, module_id: int) -> Dict:
        """Récupérer une prédiction depuis le service de prédiction"""
        try:
            response = requests.get(
                f"{self.prediction_service_url}/api/predictions/student/{student_id}/module/{module_id}",
                timeout=10
            )
            if response.status_code == 200:
                return response.json()
            else:
                raise Exception(f"Erreur lors de la récupération de la prédiction: {response.status_code}")
        except Exception as e:
            print(f"Erreur: {e}")
            # Retourner une prédiction mock pour les tests
            return {
                'student_id': student_id,
                'module_id': module_id,
                'success_probability': 0.7,
                'dropout_probability': 0.2,
                'predicted_grade': 14.0,
                'confidence_score': 0.8
            }
    
    def extract_features(self, student_id: str, module_id: int) -> np.ndarray:
        """Extraire les features pour l'explication"""
        # En production, cela récupérerait les vraies features depuis les services
        # Pour l'instant, générer des features synthétiques
        prediction = self.get_prediction(student_id, module_id)
        
        # Features synthétiques (en production, récupérer depuis les services)
        features = np.array([
            prediction.get('success_probability', 0.7),
            prediction.get('dropout_probability', 0.2),
            prediction.get('predicted_grade', 14.0) / 20.0,  # Normaliser
            prediction.get('confidence_score', 0.8),
            # Ajouter d'autres features (notes, présences, etc.)
            np.random.rand(),  # Feature 5
            np.random.rand(),  # Feature 6
            np.random.rand(),  # Feature 7
            np.random.rand(),  # Feature 8
            np.random.rand(),  # Feature 9
            np.random.rand(),  # Feature 10
        ])
        
        return features.reshape(1, -1)
    
    def create_explainer(self, training_data: np.ndarray = None):
        """Créer un explainer LIME"""
        if training_data is None:
            # Générer des données d'entraînement synthétiques
            training_data = np.random.rand(100, 10)
        
        feature_names = [
            'success_probability',
            'dropout_probability',
            'predicted_grade_norm',
            'confidence_score',
            'feature_5',
            'feature_6',
            'feature_7',
            'feature_8',
            'feature_9',
            'feature_10'
        ]
        
        self.explainer = LimeTabularExplainer(
            training_data,
            feature_names=feature_names,
            class_names=['success_probability'],
            mode='regression'
        )
    
    def predict_proba_wrapper(self, instances):
        """Wrapper pour les prédictions (utilisé par LIME)"""
        # Simuler des prédictions basées sur les features
        predictions = []
        for instance in instances:
            # Utiliser une fonction simple pour simuler les prédictions
            # En production, cela appellerait le vrai modèle
            pred = instance[0] * 0.5 + instance[1] * 0.3 + instance[2] * 0.2
            predictions.append([pred])
        return np.array(predictions)
    
    def explain_prediction(self, student_id: str, module_id: int) -> Dict:
        """Générer une explication LIME pour une prédiction"""
        # Récupérer la prédiction
        prediction = self.get_prediction(student_id, module_id)
        
        # Extraire les features
        features = self.extract_features(student_id, module_id)
        
        # Créer l'explainer si nécessaire
        if self.explainer is None:
            self.create_explainer()
        
        # Générer l'explication
        explanation = self.explainer.explain_instance(
            features[0],
            self.predict_proba_wrapper,
            num_features=10
        )
        
        # Extraire les informations importantes
        explanation_data = {}
        feature_importance_list = []
        
        # Obtenir les features importantes
        exp_list = explanation.as_list()
        for feature_name, importance_score in exp_list:
            explanation_data[feature_name] = importance_score
            feature_importance_list.append({
                'feature_name': feature_name,
                'importance_score': importance_score,
                'impact_direction': 'POSITIVE' if importance_score > 0 else 'NEGATIVE' if importance_score < 0 else 'NEUTRAL'
            })
        
        # Sauvegarder l'explication en base de données
        explanation_record = Explanation(
            student_id=student_id,
            module_id=module_id,
            prediction_id=prediction.get('id'),
            explanation_type='LIME',
            explanation_data=explanation_data,
            feature_importance=feature_importance_list,
            local_prediction=prediction.get('success_probability'),
            confidence_score=prediction.get('confidence_score')
        )
        db.session.add(explanation_record)
        
        # Sauvegarder les features importantes
        for fi in feature_importance_list:
            feature_imp = FeatureImportance(
                student_id=student_id,
                module_id=module_id,
                feature_name=fi['feature_name'],
                importance_score=fi['importance_score'],
                impact_direction=fi['impact_direction']
            )
            db.session.add(feature_imp)
        
        db.session.commit()
        db.session.refresh(explanation_record)
        
        return {
            'id': explanation_record.id,
            'student_id': student_id,
            'module_id': module_id,
            'prediction': prediction,
            'explanation_type': 'LIME',
            'feature_importance': feature_importance_list,
            'explanation_data': explanation_data,
            'local_prediction': explanation_record.local_prediction,
            'confidence_score': explanation_record.confidence_score,
            'created_at': explanation_record.created_at.isoformat()
        }
    
    def get_explanations_by_student(self, student_id: str) -> List[Dict]:
        """Obtenir toutes les explications d'un étudiant"""
        explanations = Explanation.query.filter_by(student_id=student_id).all()
        return [exp.to_dict() for exp in explanations]
    
    def get_feature_importance(self, student_id: str, module_id: int) -> List[Dict]:
        """Obtenir l'importance des features pour un étudiant/module"""
        features = FeatureImportance.query.filter_by(
            student_id=student_id,
            module_id=module_id
        ).order_by(FeatureImportance.importance_score.desc()).all()
        
        return [f.to_dict() for f in features]
    
    def get_top_factors(self, student_id: str, module_id: int, top_n: int = 5) -> List[Dict]:
        """Obtenir les top N facteurs les plus influents"""
        features = self.get_feature_importance(student_id, module_id)
        # Trier par valeur absolue de l'importance
        features.sort(key=lambda x: abs(x['importance_score']), reverse=True)
        return features[:top_n]


