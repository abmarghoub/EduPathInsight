from fastapi import APIRouter, HTTPException, BackgroundTasks
from pydantic import BaseModel
from models.train import generate_synthetic_dataset, train_model
import os

router = APIRouter()


class TrainingRequest(BaseModel):
    num_samples: int = 2000
    num_nodes: int = 10
    epochs: int = 100
    batch_size: int = 32
    learning_rate: float = 0.001


@router.post("/generate-dataset")
async def generate_dataset(request: TrainingRequest):
    """Générer un dataset synthétique"""
    try:
        dataset = generate_synthetic_dataset(
            num_samples=request.num_samples,
            num_nodes=request.num_nodes
        )
        return {
            "message": f"Dataset généré avec {len(dataset)} échantillons",
            "num_samples": len(dataset)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/train")
async def train(background_tasks: BackgroundTasks, request: TrainingRequest):
    """Entraîner le modèle (en arrière-plan)"""
    try:
        model_path = os.getenv("MODEL_PATH", "models/trained_model.pth")
        
        def train_task():
            dataset = generate_synthetic_dataset(
                num_samples=request.num_samples,
                num_nodes=request.num_nodes
            )
            train_model(
                dataset,
                model_path=model_path,
                epochs=request.epochs,
                batch_size=request.batch_size,
                lr=request.learning_rate
            )
        
        background_tasks.add_task(train_task)
        
        return {
            "message": "Entraînement démarré en arrière-plan",
            "config": {
                "num_samples": request.num_samples,
                "epochs": request.epochs,
                "batch_size": request.batch_size,
                "learning_rate": request.learning_rate
            }
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/status")
async def training_status():
    """Obtenir le statut de l'entraînement"""
    model_path = os.getenv("MODEL_PATH", "models/trained_model.pth")
    model_exists = os.path.exists(model_path)
    
    return {
        "model_exists": model_exists,
        "model_path": model_path if model_exists else None
    }


