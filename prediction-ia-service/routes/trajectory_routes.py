from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel
from database import get_db
from services.prediction_service import PredictionService

router = APIRouter()


class TrajectoryRequest(BaseModel):
    student_id: str
    module_id: int


@router.post("/student-module")
async def get_student_trajectory(
    request: TrajectoryRequest,
    db: Session = Depends(get_db)
):
    """Obtenir la trajectoire détaillée d'un étudiant pour un module"""
    try:
        service = PredictionService(db)
        trajectory = service.get_student_trajectory(
            request.student_id,
            request.module_id
        )
        return trajectory
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/student/{student_id}/module/{module_id}")
async def get_trajectory(
    student_id: str,
    module_id: int,
    db: Session = Depends(get_db)
):
    """Obtenir la trajectoire d'un étudiant pour un module"""
    try:
        service = PredictionService(db)
        trajectory = service.get_student_trajectory(student_id, module_id)
        return trajectory
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/risk-modules")
async def get_risk_modules(db: Session = Depends(get_db)):
    """Obtenir les modules à risque"""
    try:
        service = PredictionService(db)
        risk_modules = service.get_risk_modules()
        return risk_modules
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


