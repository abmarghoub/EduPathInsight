from sqlalchemy import create_engine, Column, Integer, String, Float, DateTime, Text, Boolean, JSON
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from datetime import datetime
import os
from dotenv import load_dotenv

load_dotenv()

DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "postgresql://postgres:postgres@localhost:5432/edupath_predictions"
)

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


class Prediction(Base):
    __tablename__ = "predictions"

    id = Column(Integer, primary_key=True, index=True)
    student_id = Column(String(100), nullable=False, index=True)
    module_id = Column(Integer, nullable=False, index=True)
    success_probability = Column(Float, nullable=False)  # Probabilité de réussite (0-1)
    dropout_probability = Column(Float, nullable=False)  # Probabilité d'abandon (0-1)
    risk_level = Column(String(20), nullable=False)  # LOW, MEDIUM, HIGH, CRITICAL
    predicted_grade = Column(Float, nullable=True)  # Note prédite
    confidence_score = Column(Float, nullable=False)  # Score de confiance (0-1)
    features = Column(JSON, nullable=True)  # Features utilisées pour la prédiction
    model_version = Column(String(50), nullable=False)  # Version du modèle utilisé
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)


class StudentTrajectory(Base):
    __tablename__ = "student_trajectories"

    id = Column(Integer, primary_key=True, index=True)
    student_id = Column(String(100), nullable=False, index=True)
    module_id = Column(Integer, nullable=False, index=True)
    trajectory_data = Column(JSON, nullable=False)  # Données de trajectoire détaillée
    milestones = Column(JSON, nullable=True)  # Jalons prévus
    recommendations = Column(JSON, nullable=True)  # Recommandations
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)


class RiskModule(Base):
    __tablename__ = "risk_modules"

    id = Column(Integer, primary_key=True, index=True)
    module_id = Column(Integer, nullable=False, unique=True, index=True)
    module_code = Column(String(50), nullable=False)
    module_name = Column(String(200), nullable=False)
    risk_score = Column(Float, nullable=False)  # Score de risque (0-1)
    at_risk_students_count = Column(Integer, nullable=False, default=0)
    average_success_probability = Column(Float, nullable=False)
    average_dropout_probability = Column(Float, nullable=False)
    risk_factors = Column(JSON, nullable=True)  # Facteurs de risque
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)


def init_db():
    """Initialiser la base de données"""
    Base.metadata.create_all(bind=engine)


def get_db():
    """Obtenir une session de base de données"""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


