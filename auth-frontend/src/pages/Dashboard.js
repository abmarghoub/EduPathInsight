import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Dashboard = () => {
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    window.location.href = '/login';
  };

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h1>Tableau de bord</h1>
        <button onClick={handleLogout} className="btn btn-danger" style={{ width: 'auto', padding: '10px 20px' }}>
          Déconnexion
        </button>
      </div>

      <div className="user-info">
        <h2>Informations utilisateur</h2>
        <p><strong>Nom d'utilisateur:</strong> {user?.username}</p>
        <p><strong>Email:</strong> {user?.email}</p>
        <p><strong>Rôles:</strong> {user?.roles?.join(', ') || 'Aucun'}</p>
      </div>

      <div className="nav">
        <Link to="/dashboard" className="nav-link active">Tableau de bord</Link>
        <Link to="/change-password" className="nav-link">Changer le mot de passe</Link>
        {user?.roles?.includes('ROLE_ADMIN') && (
          <Link to="/admin" className="nav-link">Panneau administrateur</Link>
        )}
      </div>

      <div>
        <h2>Bienvenue, {user?.username} !</h2>
        <p>Vous êtes connecté avec succès à EduPath Insight.</p>
      </div>
    </div>
  );
};

export default Dashboard;


