from flask_restful import Resource
from flask import request, jsonify
from services.explanation_service import ExplanationService


class ExplanationResource(Resource):
    def get(self, student_id, module_id):
        """Générer une explication pour une prédiction"""
        try:
            service = ExplanationService()
            explanation = service.explain_prediction(student_id, module_id)
            return jsonify(explanation)
        except Exception as e:
            return {'error': str(e)}, 500


class ExplanationListResource(Resource):
    def get(self, student_id):
        """Obtenir toutes les explications d'un étudiant"""
        try:
            service = ExplanationService()
            explanations = service.get_explanations_by_student(student_id)
            return jsonify(explanations)
        except Exception as e:
            return {'error': str(e)}, 500


class FeatureImportanceResource(Resource):
    def get(self, student_id, module_id):
        """Obtenir l'importance des features pour un étudiant/module"""
        try:
            service = ExplanationService()
            features = service.get_feature_importance(student_id, module_id)
            return jsonify(features)
        except Exception as e:
            return {'error': str(e)}, 500


