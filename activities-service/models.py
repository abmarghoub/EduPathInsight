from flask_sqlalchemy import SQLAlchemy
from datetime import datetime
from enum import Enum

db = SQLAlchemy()


class PresenceStatus(Enum):
    PRESENT = "PRESENT"
    ABSENT = "ABSENT"
    LATE = "LATE"
    EXCUSED = "EXCUSED"


class ActivityType(Enum):
    LECTURE = "LECTURE"
    PRACTICAL = "PRACTICAL"
    LAB = "LAB"
    ASSIGNMENT = "ASSIGNMENT"
    PROJECT = "PROJECT"
    EXAM = "EXAM"
    OTHER = "OTHER"


class AnomalyType(Enum):
    HIGH_ABSENTEEISM = "HIGH_ABSENTEEISM"
    FREQUENT_LATENESS = "FREQUENT_LATENESS"
    NO_ACTIVITY_PARTICIPATION = "NO_ACTIVITY_PARTICIPATION"
    INCONSISTENT_PRESENCE = "INCONSISTENT_PRESENCE"


class AnomalyStatus(Enum):
    ACTIVE = "ACTIVE"
    ACKNOWLEDGED = "ACKNOWLEDGED"
    RESOLVED = "RESOLVED"
    DISMISSED = "DISMISSED"


class Presence(db.Model):
    __tablename__ = 'presences'

    id = db.Column(db.Integer, primary_key=True)
    student_id = db.Column(db.String(100), nullable=False, index=True)
    student_username = db.Column(db.String(100), nullable=False)
    module_id = db.Column(db.Integer, nullable=False, index=True)
    module_code = db.Column(db.String(50), nullable=False)
    module_name = db.Column(db.String(200), nullable=False)
    session_date = db.Column(db.Date, nullable=False, index=True)
    session_time = db.Column(db.Time, nullable=True)
    status = db.Column(db.Enum(PresenceStatus), nullable=False, default=PresenceStatus.PRESENT)
    notes = db.Column(db.Text, nullable=True)
    teacher_id = db.Column(db.String(100), nullable=False)  # ID de l'enseignant qui enregistre
    teacher_username = db.Column(db.String(100), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    def to_dict(self):
        return {
            'id': self.id,
            'student_id': self.student_id,
            'student_username': self.student_username,
            'module_id': self.module_id,
            'module_code': self.module_code,
            'module_name': self.module_name,
            'session_date': self.session_date.isoformat() if self.session_date else None,
            'session_time': self.session_time.strftime('%H:%M:%S') if self.session_time else None,
            'status': self.status.value if self.status else None,
            'notes': self.notes,
            'teacher_id': self.teacher_id,
            'teacher_username': self.teacher_username,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None
        }


class Activity(db.Model):
    __tablename__ = 'activities'

    id = db.Column(db.Integer, primary_key=True)
    student_id = db.Column(db.String(100), nullable=False, index=True)
    student_username = db.Column(db.String(100), nullable=False)
    module_id = db.Column(db.Integer, nullable=False, index=True)
    module_code = db.Column(db.String(50), nullable=False)
    module_name = db.Column(db.String(200), nullable=False)
    activity_type = db.Column(db.Enum(ActivityType), nullable=False)
    title = db.Column(db.String(200), nullable=False)
    description = db.Column(db.Text, nullable=True)
    activity_date = db.Column(db.Date, nullable=False, index=True)
    duration_minutes = db.Column(db.Integer, nullable=True)
    completed = db.Column(db.Boolean, default=False, nullable=False)
    participation_score = db.Column(db.Float, nullable=True)  # Score de participation (0-100)
    notes = db.Column(db.Text, nullable=True)
    teacher_id = db.Column(db.String(100), nullable=False)  # ID de l'enseignant qui enregistre
    teacher_username = db.Column(db.String(100), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    def to_dict(self):
        return {
            'id': self.id,
            'student_id': self.student_id,
            'student_username': self.student_username,
            'module_id': self.module_id,
            'module_code': self.module_code,
            'module_name': self.module_name,
            'activity_type': self.activity_type.value if self.activity_type else None,
            'title': self.title,
            'description': self.description,
            'activity_date': self.activity_date.isoformat() if self.activity_date else None,
            'duration_minutes': self.duration_minutes,
            'completed': self.completed,
            'participation_score': self.participation_score,
            'notes': self.notes,
            'teacher_id': self.teacher_id,
            'teacher_username': self.teacher_username,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None
        }


class Anomaly(db.Model):
    __tablename__ = 'anomalies'

    id = db.Column(db.Integer, primary_key=True)
    student_id = db.Column(db.String(100), nullable=False, index=True)
    module_id = db.Column(db.Integer, nullable=False, index=True)
    anomaly_type = db.Column(db.Enum(AnomalyType), nullable=False)
    title = db.Column(db.String(200), nullable=False)
    description = db.Column(db.Text, nullable=True)
    severity = db.Column(db.String(20), nullable=False, default='MEDIUM')  # LOW, MEDIUM, HIGH, CRITICAL
    status = db.Column(db.Enum(AnomalyStatus), nullable=False, default=AnomalyStatus.ACTIVE)
    anomaly_metadata = db.Column('metadata', db.JSON, nullable=True)  # Données supplémentaires (ex: nombre d'absences, etc.)
    detected_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)
    acknowledged_at = db.Column(db.DateTime, nullable=True)
    resolved_at = db.Column(db.DateTime, nullable=True)

    def to_dict(self):
        return {
            'id': self.id,
            'student_id': self.student_id,
            'module_id': self.module_id,
            'anomaly_type': self.anomaly_type.value if self.anomaly_type else None,
            'title': self.title,
            'description': self.description,
            'severity': self.severity,
            'status': self.status.value if self.status else None,
            'metadata': self.anomaly_metadata,
            'detected_at': self.detected_at.isoformat() if self.detected_at else None,
            'acknowledged_at': self.acknowledged_at.isoformat() if self.acknowledged_at else None,
            'resolved_at': self.resolved_at.isoformat() if self.resolved_at else None
        }


