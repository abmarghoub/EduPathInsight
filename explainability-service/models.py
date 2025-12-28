from flask_sqlalchemy import SQLAlchemy
from datetime import datetime

db = SQLAlchemy()


class Explanation(db.Model):
    __tablename__ = 'explanations'

    id = db.Column(db.Integer, primary_key=True)
    student_id = db.Column(db.String(100), nullable=False, index=True)
    module_id = db.Column(db.Integer, nullable=False, index=True)
    prediction_id = db.Column(db.Integer, nullable=True)  # ID de la prédiction associée
    explanation_type = db.Column(db.String(50), nullable=False)  # LIME, SHAP, etc.
    explanation_data = db.Column(db.JSON, nullable=False)  # Données d'explication (features importantes, etc.)
    feature_importance = db.Column(db.JSON, nullable=True)  # Importance des features
    local_prediction = db.Column(db.Float, nullable=True)  # Prédiction locale pour l'explication
    confidence_score = db.Column(db.Float, nullable=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    def to_dict(self):
        return {
            'id': self.id,
            'student_id': self.student_id,
            'module_id': self.module_id,
            'prediction_id': self.prediction_id,
            'explanation_type': self.explanation_type,
            'explanation_data': self.explanation_data,
            'feature_importance': self.feature_importance,
            'local_prediction': self.local_prediction,
            'confidence_score': self.confidence_score,
            'created_at': self.created_at.isoformat() if self.created_at else None
        }


class FeatureImportance(db.Model):
    __tablename__ = 'feature_importance'

    id = db.Column(db.Integer, primary_key=True)
    student_id = db.Column(db.String(100), nullable=False, index=True)
    module_id = db.Column(db.Integer, nullable=False, index=True)
    feature_name = db.Column(db.String(100), nullable=False)
    importance_score = db.Column(db.Float, nullable=False)  # Score d'importance (-1 à 1 pour LIME)
    feature_value = db.Column(db.Float, nullable=True)  # Valeur de la feature
    impact_direction = db.Column(db.String(20), nullable=True)  # POSITIVE, NEGATIVE, NEUTRAL
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    def to_dict(self):
        return {
            'id': self.id,
            'student_id': self.student_id,
            'module_id': self.module_id,
            'feature_name': self.feature_name,
            'importance_score': self.importance_score,
            'feature_value': self.feature_value,
            'impact_direction': self.impact_direction,
            'created_at': self.created_at.isoformat() if self.created_at else None
        }


class Report(db.Model):
    __tablename__ = 'reports'

    id = db.Column(db.Integer, primary_key=True)
    student_id = db.Column(db.String(100), nullable=False, index=True)
    module_id = db.Column(db.Integer, nullable=False, index=True)
    report_type = db.Column(db.String(50), nullable=False)  # DASHBOARD, MOBILE, DETAILED
    report_data = db.Column(db.JSON, nullable=False)  # Contenu du rapport
    summary = db.Column(db.Text, nullable=True)  # Résumé textuel
    key_factors = db.Column(db.JSON, nullable=True)  # Facteurs clés identifiés
    recommendations = db.Column(db.JSON, nullable=True)  # Recommandations basées sur l'analyse
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    def to_dict(self):
        return {
            'id': self.id,
            'student_id': self.student_id,
            'module_id': self.module_id,
            'report_type': self.report_type,
            'report_data': self.report_data,
            'summary': self.summary,
            'key_factors': self.key_factors,
            'recommendations': self.recommendations,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None
        }


