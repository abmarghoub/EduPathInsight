import torch
import torch.nn as nn
import torch.nn.functional as F
from torch_geometric.nn import GCNConv, global_mean_pool, global_max_pool
from torch_geometric.data import Data, Batch
import numpy as np
import os


class StudentModuleGNN(nn.Module):
    """Modèle GNN pour prédire les trajectoires d'apprentissage"""
    
    def __init__(self, input_dim=32, hidden_dim=64, output_dim=4, num_layers=3):
        super(StudentModuleGNN, self).__init__()
        
        self.num_layers = num_layers
        self.convs = nn.ModuleList()
        self.batch_norms = nn.ModuleList()
        
        # Première couche
        self.convs.append(GCNConv(input_dim, hidden_dim))
        self.batch_norms.append(nn.BatchNorm1d(hidden_dim))
        
        # Couches cachées
        for _ in range(num_layers - 2):
            self.convs.append(GCNConv(hidden_dim, hidden_dim))
            self.batch_norms.append(nn.BatchNorm1d(hidden_dim))
        
        # Dernière couche
        self.convs.append(GCNConv(hidden_dim, hidden_dim))
        self.batch_norms.append(nn.BatchNorm1d(hidden_dim))
        
        # Couches de sortie
        self.fc1 = nn.Linear(hidden_dim * 2, hidden_dim)
        self.fc2 = nn.Linear(hidden_dim, output_dim)
        self.dropout = nn.Dropout(0.3)
    
    def forward(self, x, edge_index, batch):
        """Forward pass"""
        # Convolutions GNN
        for i in range(self.num_layers):
            x = self.convs[i](x, edge_index)
            x = self.batch_norms[i](x)
            x = F.relu(x)
            x = self.dropout(x)
        
        # Pooling global
        x_mean = global_mean_pool(x, batch)
        x_max = global_max_pool(x, batch)
        x = torch.cat([x_mean, x_max], dim=1)
        
        # Couches fully connected
        x = self.fc1(x)
        x = F.relu(x)
        x = self.dropout(x)
        x = self.fc2(x)
        
        return x


class PredictionModel:
    """Modèle wrapper pour les prédictions"""
    
    def __init__(self, model_path=None, device='cpu'):
        self.device = torch.device(device)
        self.model = StudentModuleGNN(input_dim=32, hidden_dim=64, output_dim=4, num_layers=3)
        self.model.to(self.device)
        self.model.eval()
        
        if model_path and os.path.exists(model_path):
            self.model.load_state_dict(torch.load(model_path, map_location=self.device))
    
    def predict(self, features):
        """
        Prédire les probabilités
        Retourne: success_prob, dropout_prob, predicted_grade, confidence
        """
        with torch.no_grad():
            # Convertir les features en tenseur
            x = torch.tensor(features['node_features'], dtype=torch.float).to(self.device)
            edge_index = torch.tensor(features['edge_index'], dtype=torch.long).to(self.device)
            batch = torch.tensor(features['batch'], dtype=torch.long).to(self.device)
            
            # Prédiction
            output = self.model(x, edge_index, batch)
            output = F.softmax(output, dim=1)
            
            # Extraire les probabilités
            success_prob = output[0][0].item()
            dropout_prob = output[0][1].item()
            predicted_grade = output[0][2].item() * 20  # Convertir en note sur 20
            confidence = output[0][3].item()
            
            return success_prob, dropout_prob, predicted_grade, confidence
    
    def get_risk_level(self, success_prob, dropout_prob):
        """Déterminer le niveau de risque"""
        if dropout_prob > 0.6 or success_prob < 0.3:
            return "CRITICAL"
        elif dropout_prob > 0.4 or success_prob < 0.5:
            return "HIGH"
        elif dropout_prob > 0.25 or success_prob < 0.6:
            return "MEDIUM"
        else:
            return "LOW"


