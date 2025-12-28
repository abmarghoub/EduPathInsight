from models import db, Anomaly, AnomalyType, AnomalyStatus, Presence, PresenceStatus, Activity
from datetime import datetime, timedelta
from typing import List, Dict, Optional
from services.presence_service import PresenceService
from services.activity_service import ActivityService


class AnomalyDetectionService:
    
    def __init__(self, presence_service: PresenceService, activity_service: ActivityService):
        self.presence_service = presence_service
        self.activity_service = activity_service
        
        # Seuils configurables
        self.absenteeism_threshold = 0.3  # 30% d'absences
        self.lateness_threshold = 3  # 3 retards
        self.no_participation_threshold = 0  # Aucune participation
    
    def check_anomalies(self, student_id: str, module_id: int) -> List[Anomaly]:
        """Vérifier et créer des anomalies pour un étudiant dans un module"""
        anomalies = []
        
        # Vérifier l'absentéisme
        absenteeism_anomaly = self._check_absenteeism(student_id, module_id)
        if absenteeism_anomaly:
            anomalies.append(absenteeism_anomaly)
        
        # Vérifier les retards fréquents
        lateness_anomaly = self._check_frequent_lateness(student_id, module_id)
        if lateness_anomaly:
            anomalies.append(lateness_anomaly)
        
        # Vérifier la non-participation aux activités
        participation_anomaly = self._check_no_activity_participation(student_id, module_id)
        if participation_anomaly:
            anomalies.append(participation_anomaly)
        
        # Vérifier l'incohérence de présence
        inconsistency_anomaly = self._check_inconsistent_presence(student_id, module_id)
        if inconsistency_anomaly:
            anomalies.append(inconsistency_anomaly)
        
        return anomalies
    
    def _check_absenteeism(self, student_id: str, module_id: int) -> Optional[Anomaly]:
        """Vérifier l'absentéisme élevé"""
        stats = self.presence_service.get_presence_statistics(student_id, module_id)
        
        if stats['total_sessions'] == 0:
            return None
        
        absence_rate = (stats['absent'] / stats['total_sessions'])
        
        if absence_rate >= self.absenteeism_threshold:
            # Vérifier si une anomalie similaire existe déjà
            existing = Anomaly.query.filter_by(
                student_id=student_id,
                module_id=module_id,
                anomaly_type=AnomalyType.HIGH_ABSENTEEISM,
                status=AnomalyStatus.ACTIVE
            ).first()
            
            if existing:
                # Mettre à jour l'anomalie existante
                existing.description = f"Taux d'absentéisme de {absence_rate * 100:.2f}% ({stats['absent']} absences sur {stats['total_sessions']} sessions)"
                existing.anomaly_metadata = stats
                return existing
            
            anomaly = Anomaly(
                student_id=student_id,
                module_id=module_id,
                anomaly_type=AnomalyType.HIGH_ABSENTEEISM,
                title="Absentéisme élevé",
                description=f"Taux d'absentéisme de {absence_rate * 100:.2f}% ({stats['absent']} absences sur {stats['total_sessions']} sessions)",
                severity='HIGH' if absence_rate >= 0.5 else 'MEDIUM',
                status=AnomalyStatus.ACTIVE,
                anomaly_metadata=stats
            )
            db.session.add(anomaly)
            db.session.commit()
            return anomaly
        
        return None
    
    def _check_frequent_lateness(self, student_id: str, module_id: int) -> Optional[Anomaly]:
        """Vérifier les retards fréquents"""
        stats = self.presence_service.get_presence_statistics(student_id, module_id)
        
        if stats['late'] >= self.lateness_threshold:
            # Vérifier si une anomalie similaire existe déjà
            existing = Anomaly.query.filter_by(
                student_id=student_id,
                module_id=module_id,
                anomaly_type=AnomalyType.FREQUENT_LATENESS,
                status=AnomalyStatus.ACTIVE
            ).first()
            
            if existing:
                existing.description = f"{stats['late']} retards détectés"
                existing.anomaly_metadata = stats
                return existing
            
            anomaly = Anomaly(
                student_id=student_id,
                module_id=module_id,
                anomaly_type=AnomalyType.FREQUENT_LATENESS,
                title="Retards fréquents",
                description=f"{stats['late']} retards détectés sur {stats['total_sessions']} sessions",
                severity='MEDIUM',
                status=AnomalyStatus.ACTIVE,
                anomaly_metadata=stats
            )
            db.session.add(anomaly)
            db.session.commit()
            return anomaly
        
        return None
    
    def _check_no_activity_participation(self, student_id: str, module_id: int) -> Optional[Anomaly]:
        """Vérifier l'absence de participation aux activités"""
        activities = self.activity_service.get_activities_by_student_and_module(student_id, module_id)
        
        if len(activities) == 0:
            return None
        
        completed_activities = [a for a in activities if a.completed]
        
        if len(completed_activities) == 0:
            # Vérifier si une anomalie similaire existe déjà
            existing = Anomaly.query.filter_by(
                student_id=student_id,
                module_id=module_id,
                anomaly_type=AnomalyType.NO_ACTIVITY_PARTICIPATION,
                status=AnomalyStatus.ACTIVE
            ).first()
            
            if existing:
                existing.description = f"Aucune activité complétée sur {len(activities)} activités"
                existing.anomaly_metadata = {'total_activities': len(activities), 'completed': 0}
                return existing
            
            anomaly = Anomaly(
                student_id=student_id,
                module_id=module_id,
                anomaly_type=AnomalyType.NO_ACTIVITY_PARTICIPATION,
                title="Absence de participation aux activités",
                description=f"Aucune activité complétée sur {len(activities)} activités",
                severity='HIGH',
                status=AnomalyStatus.ACTIVE,
                anomaly_metadata={'total_activities': len(activities), 'completed': 0}
            )
            db.session.add(anomaly)
            db.session.commit()
            return anomaly
        
        return None
    
    def _check_inconsistent_presence(self, student_id: str, module_id: int) -> Optional[Anomaly]:
        """Vérifier l'incohérence de présence (ex: présence alternée)"""
        presences = self.presence_service.get_presences_by_student_and_module(student_id, module_id)
        
        if len(presences) < 4:  # Besoin d'au moins 4 sessions pour détecter un pattern
            return None
        
        # Vérifier un pattern alterné (présent/absent/présent/absent)
        sorted_presences = sorted(presences, key=lambda x: x.session_date)
        pattern_detected = True
        
        for i in range(1, len(sorted_presences) - 1):
            prev_status = sorted_presences[i-1].status
            curr_status = sorted_presences[i].status
            next_status = sorted_presences[i+1].status
            
            # Pattern alterné suspect
            if prev_status == PresenceStatus.PRESENT and curr_status == PresenceStatus.ABSENT and next_status == PresenceStatus.PRESENT:
                pattern_detected = True
                break
        
        if pattern_detected:
            existing = Anomaly.query.filter_by(
                student_id=student_id,
                module_id=module_id,
                anomaly_type=AnomalyType.INCONSISTENT_PRESENCE,
                status=AnomalyStatus.ACTIVE
            ).first()
            
            if existing:
                return existing
            
            anomaly = Anomaly(
                student_id=student_id,
                module_id=module_id,
                anomaly_type=AnomalyType.INCONSISTENT_PRESENCE,
                title="Présence incohérente",
                description="Pattern de présence alterné détecté (présent/absent alternés)",
                severity='LOW',
                status=AnomalyStatus.ACTIVE,
                anomaly_metadata={'total_sessions': len(presences)}
            )
            db.session.add(anomaly)
            db.session.commit()
            return anomaly
        
        return None
    
    def get_anomalies_by_student(self, student_id: str) -> List[Anomaly]:
        """Obtenir toutes les anomalies d'un étudiant"""
        return Anomaly.query.filter_by(student_id=student_id).all()
    
    def get_active_anomalies_by_student(self, student_id: str) -> List[Anomaly]:
        """Obtenir les anomalies actives d'un étudiant"""
        return Anomaly.query.filter_by(student_id=student_id, status=AnomalyStatus.ACTIVE).all()
    
    def acknowledge_anomaly(self, anomaly_id: int) -> Optional[Anomaly]:
        """Prendre en compte une anomalie"""
        anomaly = Anomaly.query.get(anomaly_id)
        if not anomaly:
            return None
        
        anomaly.status = AnomalyStatus.ACKNOWLEDGED
        anomaly.acknowledged_at = datetime.utcnow()
        db.session.commit()
        return anomaly
    
    def resolve_anomaly(self, anomaly_id: int) -> Optional[Anomaly]:
        """Résoudre une anomalie"""
        anomaly = Anomaly.query.get(anomaly_id)
        if not anomaly:
            return None
        
        anomaly.status = AnomalyStatus.RESOLVED
        anomaly.resolved_at = datetime.utcnow()
        db.session.commit()
        return anomaly


