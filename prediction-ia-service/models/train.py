import torch
import torch.nn as nn
import torch.optim as optim
from torch_geometric.data import Data, Batch
from torch_geometric.loader import DataLoader
import numpy as np
import os
import sys

# Ajouter le répertoire parent au path
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from models.gnn_model import StudentModuleGNN


def generate_synthetic_dataset(num_samples=1000, num_nodes=10):
    """Générer un dataset synthétique pour l'entraînement"""
    dataset = []
    
    for _ in range(num_samples):
        # Générer des features de nœuds aléatoires
        node_features = np.random.randn(num_nodes, 32).astype(np.float32)
        
        # Générer un graphe d'edges (chaque étudiant connecté à quelques modules)
        num_edges = np.random.randint(num_nodes, num_nodes * 2)
        edge_index = []
        for _ in range(num_edges):
            src = np.random.randint(0, num_nodes)
            dst = np.random.randint(0, num_nodes)
            if src != dst:
                edge_index.append([src, dst])
        
        if len(edge_index) == 0:
            edge_index = [[0, 1]]  # Au moins une edge
        
        edge_index = np.array(edge_index).T.astype(np.int64)
        
        # Générer des labels synthétiques basés sur les features
        # Plus les features sont élevées, plus la probabilité de réussite est élevée
        avg_feature = np.mean(node_features)
        success_prob = 1 / (1 + np.exp(-avg_feature))  # Sigmoid
        
        # Labels: [success_prob, dropout_prob, predicted_grade_normalized, confidence]
        dropout_prob = 1 - success_prob
        predicted_grade = success_prob * 20 / 20  # Normaliser sur [0, 1]
        confidence = min(1.0, abs(avg_feature) / 2)
        
        labels = np.array([
            success_prob,
            dropout_prob,
            predicted_grade,
            confidence
        ], dtype=np.float32)
        
        # Créer un Data object
        data = Data(
            x=torch.tensor(node_features, dtype=torch.float),
            edge_index=torch.tensor(edge_index, dtype=torch.long),
            y=torch.tensor(labels, dtype=torch.float)
        )
        
        dataset.append(data)
    
    return dataset


def train_model(dataset, model_path='models/trained_model.pth', epochs=100, batch_size=32, lr=0.001):
    """Entraîner le modèle GNN"""
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    
    # Diviser le dataset
    train_size = int(0.8 * len(dataset))
    train_dataset = dataset[:train_size]
    val_dataset = dataset[train_size:]
    
    train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True)
    val_loader = DataLoader(val_dataset, batch_size=batch_size, shuffle=False)
    
    # Créer le modèle
    model = StudentModuleGNN(input_dim=32, hidden_dim=64, output_dim=4, num_layers=3)
    model.to(device)
    
    # Optimizer et loss
    optimizer = optim.Adam(model.parameters(), lr=lr)
    criterion = nn.MSELoss()
    
    best_val_loss = float('inf')
    
    print(f"Entraînement sur {len(train_dataset)} échantillons, validation sur {len(val_dataset)}")
    print(f"Device: {device}")
    
    for epoch in range(epochs):
        # Training
        model.train()
        train_loss = 0
        for batch in train_loader:
            batch = batch.to(device)
            optimizer.zero_grad()
            
            out = model(batch.x, batch.edge_index, batch.batch)
            loss = criterion(out, batch.y)
            
            loss.backward()
            optimizer.step()
            
            train_loss += loss.item()
        
        train_loss /= len(train_loader)
        
        # Validation
        model.eval()
        val_loss = 0
        with torch.no_grad():
            for batch in val_loader:
                batch = batch.to(device)
                out = model(batch.x, batch.edge_index, batch.batch)
                loss = criterion(out, batch.y)
                val_loss += loss.item()
        
        val_loss /= len(val_loader)
        
        if (epoch + 1) % 10 == 0:
            print(f"Epoch {epoch+1}/{epochs}, Train Loss: {train_loss:.4f}, Val Loss: {val_loss:.4f}")
        
        # Sauvegarder le meilleur modèle
        if val_loss < best_val_loss:
            best_val_loss = val_loss
            os.makedirs(os.path.dirname(model_path), exist_ok=True)
            torch.save(model.state_dict(), model_path)
            print(f"Nouveau meilleur modèle sauvegardé (Val Loss: {val_loss:.4f})")
    
    print(f"Entraînement terminé. Meilleur Val Loss: {best_val_loss:.4f}")
    return model


if __name__ == "__main__":
    # Générer le dataset
    print("Génération du dataset synthétique...")
    dataset = generate_synthetic_dataset(num_samples=2000, num_nodes=10)
    print(f"Dataset généré: {len(dataset)} échantillons")
    
    # Entraîner le modèle
    print("Démarrage de l'entraînement...")
    model = train_model(dataset, epochs=100, batch_size=32)
    print("Entraînement terminé!")

