from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import uvicorn
from dotenv import load_dotenv
import os

from database import init_db, get_db
from routes import prediction_routes, training_routes, trajectory_routes
from services.rabbitmq_service import RabbitMQService

load_dotenv()

app = FastAPI(
    title="EduPath Prediction IA Service",
    description="Service de prédiction des trajectoires d'apprentissage",
    version="1.0.0"
)

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routes
app.include_router(prediction_routes.router, prefix="/api/predictions", tags=["Predictions"])
app.include_router(training_routes.router, prefix="/api/training", tags=["Training"])
app.include_router(trajectory_routes.router, prefix="/api/trajectories", tags=["Trajectories"])


@app.on_event("startup")
async def startup_event():
    """Initialisation au démarrage"""
    init_db()
    # Initialiser RabbitMQ
    rabbitmq_service = RabbitMQService()
    await rabbitmq_service.connect()


@app.on_event("shutdown")
async def shutdown_event():
    """Nettoyage à l'arrêt"""
    rabbitmq_service = RabbitMQService()
    await rabbitmq_service.close()


@app.get("/")
async def root():
    return {"message": "EduPath Prediction IA Service", "version": "1.0.0"}


@app.get("/health")
async def health_check():
    return {"status": "healthy"}


if __name__ == "__main__":
    port = int(os.getenv("PORT", 8001))
    uvicorn.run("app:app", host="0.0.0.0", port=port, reload=True)


