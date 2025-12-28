from models import db, Activity, ActivityType
from datetime import datetime
from typing import List, Optional, Dict
import requests


class ActivityService:
    
    def __init__(self, module_service_url: str):
        self.module_service_url = module_service_url
    
    def create_activity(self, data: Dict, teacher_id: str, teacher_username: str) -> Activity:
        """Créer une activité"""
        # Récupérer les informations du module
        module_info = self._get_module_info(data['module_id'])
        
        activity = Activity(
            student_id=data['student_id'],
            student_username=data.get('student_username', data['student_id']),  # Fallback
            module_id=data['module_id'],
            module_code=module_info.get('code', ''),
            module_name=module_info.get('name', ''),
            activity_type=ActivityType[data['activity_type']],
            title=data['title'],
            description=data.get('description'),
            activity_date=data['activity_date'],
            duration_minutes=data.get('duration_minutes'),
            completed=data.get('completed', False),
            participation_score=data.get('participation_score'),
            notes=data.get('notes'),
            teacher_id=teacher_id,
            teacher_username=teacher_username
        )
        
        db.session.add(activity)
        db.session.commit()
        return activity
    
    def update_activity(self, activity_id: int, data: Dict, teacher_id: str) -> Optional[Activity]:
        """Mettre à jour une activité"""
        activity = Activity.query.get(activity_id)
        if not activity:
            return None
        
        # Vérifier que c'est le même enseignant
        if activity.teacher_id != teacher_id:
            raise ValueError("Vous n'êtes pas autorisé à modifier cette activité")
        
        if 'activity_type' in data:
            activity.activity_type = ActivityType[data['activity_type']]
        if 'title' in data:
            activity.title = data['title']
        if 'description' in data:
            activity.description = data['description']
        if 'activity_date' in data:
            activity.activity_date = data['activity_date']
        if 'duration_minutes' in data:
            activity.duration_minutes = data['duration_minutes']
        if 'completed' in data:
            activity.completed = data['completed']
        if 'participation_score' in data:
            activity.participation_score = data['participation_score']
        if 'notes' in data:
            activity.notes = data['notes']
        
        activity.updated_at = datetime.utcnow()
        db.session.commit()
        return activity
    
    def delete_activity(self, activity_id: int, teacher_id: str) -> bool:
        """Supprimer une activité"""
        activity = Activity.query.get(activity_id)
        if not activity:
            return False
        
        # Vérifier que c'est le même enseignant
        if activity.teacher_id != teacher_id:
            raise ValueError("Vous n'êtes pas autorisé à supprimer cette activité")
        
        db.session.delete(activity)
        db.session.commit()
        return True
    
    def get_activity_by_id(self, activity_id: int) -> Optional[Activity]:
        """Obtenir une activité par ID"""
        return Activity.query.get(activity_id)
    
    def get_activities_by_module(self, module_id: int) -> List[Activity]:
        """Obtenir toutes les activités d'un module"""
        return Activity.query.filter_by(module_id=module_id).all()
    
    def get_activities_by_student(self, student_id: str) -> List[Activity]:
        """Obtenir toutes les activités d'un étudiant"""
        return Activity.query.filter_by(student_id=student_id).all()
    
    def get_activities_by_student_and_module(self, student_id: str, module_id: int) -> List[Activity]:
        """Obtenir les activités d'un étudiant pour un module"""
        return Activity.query.filter_by(student_id=student_id, module_id=module_id).all()
    
    def get_activity_statistics(self, student_id: str, module_id: int) -> Dict:
        """Calculer les statistiques d'activité pour un étudiant dans un module"""
        activities = self.get_activities_by_student_and_module(student_id, module_id)
        
        total = len(activities)
        completed = sum(1 for a in activities if a.completed)
        completion_rate = (completed / total * 100) if total > 0 else 0
        
        participation_scores = [a.participation_score for a in activities if a.participation_score is not None]
        avg_participation = sum(participation_scores) / len(participation_scores) if participation_scores else None
        
        return {
            'total_activities': total,
            'completed': completed,
            'not_completed': total - completed,
            'completion_rate': round(completion_rate, 2),
            'average_participation_score': round(avg_participation, 2) if avg_participation else None
        }
    
    def _get_module_info(self, module_id: int) -> Dict:
        """Récupérer les informations d'un module depuis module-service"""
        try:
            response = requests.get(
                f"{self.module_service_url}/api/modules/admin/modules/{module_id}",
                timeout=5
            )
            if response.status_code == 200:
                return response.json()
        except Exception as e:
            print(f"Erreur lors de la récupération du module: {e}")
        return {}


