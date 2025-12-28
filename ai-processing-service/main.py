from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Dict, Optional
import pandas as pd
import numpy as np
import json
import os
from datetime import datetime

app = FastAPI(title="AI Processing Service", version="1.0.0")

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class FeatureRequest(BaseModel):
    entity_type: str
    entity_id: str
    raw_data: Dict

class FeatureResponse(BaseModel):
    entity_type: str
    entity_id: str
    features: Dict
    metadata: Optional[Dict] = None

class ProcessingRequest(BaseModel):
    data: List[Dict]
    processing_type: str  # clean, transform, extract_features

class ProcessingResponse(BaseModel):
    processed_data: List[Dict]
    statistics: Dict

@app.get("/")
def root():
    return {"message": "AI Processing Service is running", "version": "1.0.0"}

@app.get("/health")
def health():
    return {"status": "healthy"}

@app.post("/process/clean", response_model=ProcessingResponse)
async def clean_data(request: ProcessingRequest):
    """
    Nettoie les données: supprime les valeurs nulles, normalise les formats, etc.
    """
    try:
        df = pd.DataFrame(request.data)
        
        # Supprimer les colonnes entièrement vides
        df = df.dropna(axis=1, how='all')
        
        # Remplacer les valeurs vides par None
        df = df.replace('', None)
        
        # Normaliser les noms de colonnes
        df.columns = df.columns.str.strip().str.lower().str.replace(' ', '_')
        
        # Conversion des types numériques
        for col in df.select_dtypes(include=['object']).columns:
            try:
                df[col] = pd.to_numeric(df[col], errors='ignore')
            except:
                pass
        
        processed_data = df.to_dict('records')
        
        statistics = {
            "original_rows": len(request.data),
            "processed_rows": len(processed_data),
            "columns_removed": len(request.data[0]) - len(df.columns) if request.data else 0,
            "null_values": int(df.isnull().sum().sum())
        }
        
        return ProcessingResponse(
            processed_data=processed_data,
            statistics=statistics
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erreur lors du nettoyage: {str(e)}")

@app.post("/process/transform", response_model=ProcessingResponse)
async def transform_data(request: ProcessingRequest):
    """
    Transforme les données: normalisation, encodage, etc.
    """
    try:
        df = pd.DataFrame(request.data)
        
        # Normalisation des valeurs numériques
        numeric_cols = df.select_dtypes(include=[np.number]).columns
        for col in numeric_cols:
            if df[col].std() > 0:
                df[col] = (df[col] - df[col].mean()) / df[col].std()
        
        # Encodage one-hot pour les colonnes catégorielles (optionnel)
        # df_encoded = pd.get_dummies(df, columns=cat_cols)
        
        processed_data = df.to_dict('records')
        
        statistics = {
            "original_rows": len(request.data),
            "processed_rows": len(processed_data),
            "numeric_columns_normalized": len(numeric_cols)
        }
        
        return ProcessingResponse(
            processed_data=processed_data,
            statistics=statistics
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erreur lors de la transformation: {str(e)}")

@app.post("/extract/features", response_model=FeatureResponse)
async def extract_features(request: FeatureRequest):
    """
    Extrait des features pour l'IA à partir des données brutes
    """
    try:
        features = {}
        raw_data = request.raw_data
        
        # Extraction de features statistiques
        if isinstance(raw_data, dict):
            # Features numériques
            numeric_values = [v for v in raw_data.values() if isinstance(v, (int, float))]
            if numeric_values:
                features['mean'] = float(np.mean(numeric_values))
                features['std'] = float(np.std(numeric_values))
                features['min'] = float(np.min(numeric_values))
                features['max'] = float(np.max(numeric_values))
            
            # Features catégorielles
            categorical_values = [v for v in raw_data.values() if isinstance(v, str)]
            features['categorical_count'] = len(categorical_values)
            features['unique_categories'] = len(set(categorical_values))
            
            # Features de complétude
            total_fields = len(raw_data)
            non_null_fields = sum(1 for v in raw_data.values() if v is not None and v != '')
            features['completeness'] = non_null_fields / total_fields if total_fields > 0 else 0.0
        
        metadata = {
            "extracted_at": datetime.now().isoformat(),
            "feature_count": len(features)
        }
        
        return FeatureResponse(
            entity_type=request.entity_type,
            entity_id=request.entity_id,
            features=features,
            metadata=metadata
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erreur lors de l'extraction: {str(e)}")

@app.post("/process/batch")
async def process_batch(request: ProcessingRequest):
    """
    Traite un batch de données avec nettoyage et transformation
    """
    try:
        # Nettoyage
        clean_request = ProcessingRequest(
            data=request.data,
            processing_type="clean"
        )
        clean_response = await clean_data(clean_request)
        
        # Transformation
        transform_request = ProcessingRequest(
            data=clean_response.processed_data,
            processing_type="transform"
        )
        transform_response = await transform_data(transform_request)
        
        return {
            "processed_data": transform_response.processed_data,
            "statistics": {
                "cleaning": clean_response.statistics,
                "transformation": transform_response.statistics
            }
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erreur lors du traitement batch: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)


