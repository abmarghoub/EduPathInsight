from models import db, Report, Explanation, FeatureImportance
from services.explanation_service import ExplanationService
from typing import Dict, List
import json


class ReportService:
    
    def __init__(self):
        self.explanation_service = ExplanationService()
    
    def generate_dashboard_report(self, student_id: str, module_id: int) -> Dict:
        """Générer un rapport pour le Dashboard"""
        # Obtenir l'explication
        explanation = self.explanation_service.explain_prediction(student_id, module_id)
        
        # Obtenir les top facteurs
        top_factors = self.explanation_service.get_top_factors(student_id, module_id, top_n=5)
        
        # Générer le résumé
        summary = self._generate_summary(explanation, top_factors)
        
        # Générer les recommandations
        recommendations = self._generate_recommendations(explanation, top_factors)
        
        # Créer le rapport
        report_data = {
            'prediction': explanation['prediction'],
            'key_factors': top_factors,
            'feature_importance': explanation['feature_importance'],
            'summary': summary,
            'recommendations': recommendations,
            'confidence': explanation['confidence_score']
        }
        
        # Sauvegarder le rapport
        report = Report(
            student_id=student_id,
            module_id=module_id,
            report_type='DASHBOARD',
            report_data=report_data,
            summary=summary,
            key_factors=top_factors,
            recommendations=recommendations
        )
        db.session.add(report)
        db.session.commit()
        db.session.refresh(report)
        
        return report.to_dict()
    
    def generate_mobile_report(self, student_id: str, module_id: int) -> Dict:
        """Générer un rapport simplifié pour Mobile"""
        # Obtenir l'explication
        explanation = self.explanation_service.explain_prediction(student_id, module_id)
        
        # Obtenir les top 3 facteurs
        top_factors = self.explanation_service.get_top_factors(student_id, module_id, top_n=3)
        
        # Générer un résumé court
        summary = self._generate_summary(explanation, top_factors, short=True)
        
        # Recommandations courtes
        recommendations = self._generate_recommendations(explanation, top_factors, short=True)
        
        # Créer le rapport simplifié
        report_data = {
            'prediction_summary': {
                'success_probability': explanation['prediction']['success_probability'],
                'dropout_probability': explanation['prediction']['dropout_probability'],
                'risk_level': explanation['prediction']['risk_level']
            },
            'top_3_factors': top_factors,
            'summary': summary,
            'recommendations': recommendations[:3]  # Limiter à 3 recommandations
        }
        
        # Sauvegarder le rapport
        report = Report(
            student_id=student_id,
            module_id=module_id,
            report_type='MOBILE',
            report_data=report_data,
            summary=summary,
            key_factors=top_factors,
            recommendations=recommendations[:3]
        )
        db.session.add(report)
        db.session.commit()
        db.session.refresh(report)
        
        return report.to_dict()
    
    def generate_detailed_report(self, student_id: str, module_id: int) -> Dict:
        """Générer un rapport détaillé"""
        # Obtenir l'explication complète
        explanation = self.explanation_service.explain_prediction(student_id, module_id)
        
        # Obtenir toutes les features importantes
        all_factors = self.explanation_service.get_feature_importance(student_id, module_id)
        
        # Générer un résumé détaillé
        summary = self._generate_summary(explanation, all_factors, detailed=True)
        
        # Recommandations détaillées
        recommendations = self._generate_recommendations(explanation, all_factors, detailed=True)
        
        # Créer le rapport détaillé
        report_data = {
            'prediction': explanation['prediction'],
            'all_factors': all_factors,
            'feature_importance': explanation['feature_importance'],
            'explanation_data': explanation['explanation_data'],
            'summary': summary,
            'recommendations': recommendations,
            'confidence': explanation['confidence_score'],
            'methodology': 'LIME (Local Interpretable Model-agnostic Explanations)'
        }
        
        # Sauvegarder le rapport
        report = Report(
            student_id=student_id,
            module_id=module_id,
            report_type='DETAILED',
            report_data=report_data,
            summary=summary,
            key_factors=all_factors,
            recommendations=recommendations
        )
        db.session.add(report)
        db.session.commit()
        db.session.refresh(report)
        
        return report.to_dict()
    
    def _generate_summary(self, explanation: Dict, factors: List[Dict], short: bool = False, detailed: bool = False) -> str:
        """Générer un résumé textuel"""
        prediction = explanation['prediction']
        success_prob = prediction['success_probability']
        dropout_prob = prediction['dropout_probability']
        
        if short:
            return f"Probabilité de réussite: {success_prob*100:.1f}%. " \
                   f"Facteurs clés: {', '.join([f['feature_name'] for f in factors[:3]])}."
        
        summary = f"Analyse de la prédiction pour l'étudiant {explanation['student_id']} dans le module {explanation['module_id']}. "
        summary += f"Probabilité de réussite: {success_prob*100:.1f}%, Probabilité d'abandon: {dropout_prob*100:.1f}%. "
        
        if factors:
            top_positive = [f for f in factors if f.get('impact_direction') == 'POSITIVE'][:2]
            top_negative = [f for f in factors if f.get('impact_direction') == 'NEGATIVE'][:2]
            
            if top_positive:
                summary += f"Facteurs positifs: {', '.join([f['feature_name'] for f in top_positive])}. "
            if top_negative:
                summary += f"Facteurs négatifs: {', '.join([f['feature_name'] for f in top_negative])}. "
        
        if detailed:
            summary += f"Confiance du modèle: {explanation['confidence_score']*100:.1f}%. "
            summary += f"Analyse basée sur {len(factors)} facteurs identifiés."
        
        return summary
    
    def _generate_recommendations(self, explanation: Dict, factors: List[Dict], short: bool = False, detailed: bool = False) -> List[Dict]:
        """Générer des recommandations basées sur l'analyse"""
        recommendations = []
        prediction = explanation['prediction']
        
        # Analyser les facteurs négatifs
        negative_factors = [f for f in factors if f.get('impact_direction') == 'NEGATIVE']
        positive_factors = [f for f in factors if f.get('impact_direction') == 'POSITIVE']
        
        if prediction['dropout_probability'] > 0.4:
            recommendations.append({
                'type': 'HIGH_DROPOUT_RISK',
                'priority': 'HIGH',
                'title': 'Risque d\'abandon élevé',
                'description': 'La probabilité d\'abandon est élevée. Action immédiate recommandée.',
                'actions': [
                    'Organiser une rencontre avec l\'étudiant',
                    'Mettre en place un plan de soutien personnalisé',
                    'Augmenter le suivi hebdomadaire'
                ]
            })
        
        if prediction['success_probability'] < 0.5:
            recommendations.append({
                'type': 'LOW_SUCCESS_PROBABILITY',
                'priority': 'MEDIUM',
                'title': 'Probabilité de réussite faible',
                'description': 'Les facteurs actuels suggèrent une probabilité de réussite faible.',
                'actions': [
                    'Renforcer les cours dans les domaines faibles',
                    'Offrir du tutorat supplémentaire',
                    'Réviser les méthodes d\'apprentissage'
                ]
            })
        
        if negative_factors:
            top_negative = negative_factors[0] if negative_factors else None
            if top_negative:
                recommendations.append({
                    'type': 'NEGATIVE_FACTOR',
                    'priority': 'MEDIUM',
                    'title': f'Facteur négatif: {top_negative["feature_name"]}',
                    'description': f'Ce facteur impacte négativement la prédiction (score: {top_negative["importance_score"]:.3f}).',
                    'actions': [
                        f'Améliorer le facteur {top_negative["feature_name"]}',
                        'Analyser les causes sous-jacentes',
                        'Mettre en place des actions correctives'
                    ]
                })
        
        if positive_factors and not short:
            recommendations.append({
                'type': 'POSITIVE_FACTOR',
                'priority': 'LOW',
                'title': 'Facteurs positifs identifiés',
                'description': 'Certains facteurs contribuent positivement à la prédiction.',
                'actions': [
                    'Maintenir ces facteurs positifs',
                    'Répliquer ces pratiques dans d\'autres contextes'
                ]
            })
        
        return recommendations if not short else recommendations[:3]
    
    def get_reports_by_student(self, student_id: str, report_type: str = None) -> List[Dict]:
        """Obtenir tous les rapports d'un étudiant"""
        query = Report.query.filter_by(student_id=student_id)
        if report_type:
            query = query.filter_by(report_type=report_type)
        reports = query.all()
        return [r.to_dict() for r in reports]


