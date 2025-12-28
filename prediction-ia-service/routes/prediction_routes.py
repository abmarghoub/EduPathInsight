from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel
from typing import Optional
from database import get_db
from services.prediction_service import PredictionService
from services.rabbitmq_service import RabbitMQService

router = APIRouter()


class PredictionRequest(BaseModel):
    student_id: str
    module_id: int
    use_cache: Optional[bool] = True


class PredictionResponse(BaseModel):
    id: int
    student_id: str
    module_id: int
    success_probability: float
    dropout_probability: float
    risk_level: str
    predicted_grade: Optional[float]
    confidence_score: float
    created_at: str


@router.post("/student-module", response_model=PredictionResponse)
async def predict_student_module(
    request: PredictionRequest,
    db: Session = Depends(get_db)
):
    """Prédire la trajectoire d'un étudiant pour un module"""
    try:
        service = PredictionService(db)
        prediction = service.predict_student_module(
            request.student_id,
            request.module_id,
            request.use_cache
        )
        
        # Envoyer une alerte si risque élevé
        if prediction['risk_level'] in ['HIGH', 'CRITICAL']:
            rabbitmq = RabbitMQService()
            await rabbitmq.connect()
            rabbitmq.send_high_risk_alert(
                request.student_id,
                request.module_id,
                prediction['risk_level'],
                prediction
            )
        
        return prediction
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/student/{student_id}/module/{module_id}")
async def get_prediction(
    student_id: str,
    module_id: int,
    db: Session = Depends(get_db)
):
    """Obtenir une prédiction existante"""
    service = PredictionService(db)
    prediction = service.predict_student_module(student_id, module_id)
    
    return prediction


@router.get("/student/{student_id}")
async def get_student_predictions(
    student_id: str,
    db: Session = Depends(get_db)
):
    """Obtenir toutes les prédictions d'un étudiant"""
    from database import Prediction
    
    predictions = db.query(Prediction).filter_by(student_id=student_id).all()
    
    return [
        {
            'id': p.id,
            'student_id': p.student_id,
            'module_id': p.module_id,
            'success_probability': p.success_probability,
            'dropout_probability': p.dropout_probability,
            'risk_level': p.risk_level,
            'predicted_grade': p.predicted_grade,
            'confidence_score': p.confidence_score,
            'created_at': p.created_at.isoformat()
        }
        for p in predictions
    ]


