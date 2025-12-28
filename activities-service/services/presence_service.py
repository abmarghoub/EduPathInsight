from models import db, Presence, PresenceStatus
from datetime import datetime, date
from typing import List, Optional, Dict
import requests


class PresenceService:
    
    def __init__(self, module_service_url: str):
        self.module_service_url = module_service_url
    
    def create_presence(self, data: Dict, teacher_id: str, teacher_username: str) -> Presence:
        """Créer une présence"""
        # Récupérer les informations du module
        module_info = self._get_module_info(data['module_id'])
        
        presence = Presence(
            student_id=data['student_id'],
            student_username=data.get('student_username', data['student_id']),  # Fallback
            module_id=data['module_id'],
            module_code=module_info.get('code', ''),
            module_name=module_info.get('name', ''),
            session_date=data['session_date'],
            session_time=data.get('session_time'),
            status=PresenceStatus[data['status']],
            notes=data.get('notes'),
            teacher_id=teacher_id,
            teacher_username=teacher_username
        )
        
        db.session.add(presence)
        db.session.commit()
        return presence
    
    def update_presence(self, presence_id: int, data: Dict, teacher_id: str) -> Optional[Presence]:
        """Mettre à jour une présence"""
        presence = Presence.query.get(presence_id)
        if not presence:
            return None
        
        # Vérifier que c'est le même enseignant
        if presence.teacher_id != teacher_id:
            raise ValueError("Vous n'êtes pas autorisé à modifier cette présence")
        
        if 'session_date' in data:
            presence.session_date = data['session_date']
        if 'session_time' in data:
            presence.session_time = data['session_time']
        if 'status' in data:
            presence.status = PresenceStatus[data['status']]
        if 'notes' in data:
            presence.notes = data['notes']
        
        presence.updated_at = datetime.utcnow()
        db.session.commit()
        return presence
    
    def delete_presence(self, presence_id: int, teacher_id: str) -> bool:
        """Supprimer une présence"""
        presence = Presence.query.get(presence_id)
        if not presence:
            return False
        
        # Vérifier que c'est le même enseignant
        if presence.teacher_id != teacher_id:
            raise ValueError("Vous n'êtes pas autorisé à supprimer cette présence")
        
        db.session.delete(presence)
        db.session.commit()
        return True
    
    def get_presence_by_id(self, presence_id: int) -> Optional[Presence]:
        """Obtenir une présence par ID"""
        return Presence.query.get(presence_id)
    
    def get_presences_by_module(self, module_id: int) -> List[Presence]:
        """Obtenir toutes les présences d'un module"""
        return Presence.query.filter_by(module_id=module_id).all()
    
    def get_presences_by_student(self, student_id: str) -> List[Presence]:
        """Obtenir toutes les présences d'un étudiant"""
        return Presence.query.filter_by(student_id=student_id).all()
    
    def get_presences_by_student_and_module(self, student_id: str, module_id: int) -> List[Presence]:
        """Obtenir les présences d'un étudiant pour un module"""
        return Presence.query.filter_by(student_id=student_id, module_id=module_id).all()
    
    def get_presence_statistics(self, student_id: str, module_id: int) -> Dict:
        """Calculer les statistiques de présence pour un étudiant dans un module"""
        presences = self.get_presences_by_student_and_module(student_id, module_id)
        
        total = len(presences)
        present = sum(1 for p in presences if p.status == PresenceStatus.PRESENT)
        absent = sum(1 for p in presences if p.status == PresenceStatus.ABSENT)
        late = sum(1 for p in presences if p.status == PresenceStatus.LATE)
        excused = sum(1 for p in presences if p.status == PresenceStatus.EXCUSED)
        
        presence_rate = (present / total * 100) if total > 0 else 0
        
        return {
            'total_sessions': total,
            'present': present,
            'absent': absent,
            'late': late,
            'excused': excused,
            'presence_rate': round(presence_rate, 2)
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


