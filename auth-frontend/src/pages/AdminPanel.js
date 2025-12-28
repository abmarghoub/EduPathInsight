import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

const AdminPanel = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [createForm, setCreateForm] = useState({
    username: '',
    email: '',
    password: '',
    roles: ['ROLE_STUDENT']
  });
  const { user: currentUser } = useAuth();

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      const response = await api.get('/api/auth/admin/users');
      setUsers(response.data);
      setLoading(false);
    } catch (error) {
      setError('Erreur lors du chargement des utilisateurs');
      setLoading(false);
    }
  };

  const handleCreateUser = async (e) => {
    e.preventDefault();
    setError('');

    if (createForm.password.length < 6) {
      setError('Le mot de passe doit contenir au moins 6 caractères');
      return;
    }

    try {
      await api.post('/api/auth/admin/create-user', createForm);
      setShowCreateForm(false);
      setCreateForm({
        username: '',
        email: '',
        password: '',
        roles: ['ROLE_STUDENT']
      });
      fetchUsers();
    } catch (error) {
      setError(error.response?.data?.message || 'Erreur lors de la création de l\'utilisateur');
    }
  };

  const toggleRole = (role) => {
    setCreateForm(prev => ({
      ...prev,
      roles: prev.roles.includes(role)
        ? prev.roles.filter(r => r !== role)
        : [...prev.roles, role]
    }));
  };

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h1>Panneau administrateur</h1>
        <Link to="/dashboard" className="btn btn-secondary" style={{ width: 'auto', padding: '10px 20px', textDecoration: 'none', display: 'inline-block' }}>
          Retour
        </Link>
      </div>

      <div className="nav">
        <Link to="/dashboard" className="nav-link">Tableau de bord</Link>
        <Link to="/change-password" className="nav-link">Changer le mot de passe</Link>
        <Link to="/admin" className="nav-link active">Panneau administrateur</Link>
      </div>

      {error && <div className="error">{error}</div>}

      <div style={{ marginBottom: '20px' }}>
        <button
          onClick={() => setShowCreateForm(!showCreateForm)}
          className="btn btn-success"
          style={{ width: 'auto' }}
        >
          {showCreateForm ? 'Annuler' : 'Créer un utilisateur'}
        </button>
      </div>

      {showCreateForm && (
        <div className="container" style={{ marginBottom: '30px' }}>
          <h2>Créer un utilisateur</h2>
          <form onSubmit={handleCreateUser}>
            <div className="form-group">
              <label>Nom d'utilisateur</label>
              <input
                type="text"
                value={createForm.username}
                onChange={(e) => setCreateForm({ ...createForm, username: e.target.value })}
                required
                minLength={3}
              />
            </div>
            <div className="form-group">
              <label>Email</label>
              <input
                type="email"
                value={createForm.email}
                onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Mot de passe temporaire</label>
              <input
                type="password"
                value={createForm.password}
                onChange={(e) => setCreateForm({ ...createForm, password: e.target.value })}
                required
                minLength={6}
              />
            </div>
            <div className="form-group">
              <label>Rôles</label>
              <div>
                <label style={{ display: 'block', marginBottom: '10px' }}>
                  <input
                    type="checkbox"
                    checked={createForm.roles.includes('ROLE_STUDENT')}
                    onChange={() => toggleRole('ROLE_STUDENT')}
                    style={{ marginRight: '8px' }}
                  />
                  ROLE_STUDENT
                </label>
                <label style={{ display: 'block', marginBottom: '10px' }}>
                  <input
                    type="checkbox"
                    checked={createForm.roles.includes('ROLE_ADMIN')}
                    onChange={() => toggleRole('ROLE_ADMIN')}
                    style={{ marginRight: '8px' }}
                  />
                  ROLE_ADMIN
                </label>
              </div>
            </div>
            <button type="submit" className="btn">
              Créer l'utilisateur
            </button>
          </form>
        </div>
      )}

      <h2>Liste des utilisateurs</h2>
      {loading ? (
        <p>Chargement...</p>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nom d'utilisateur</th>
              <th>Email</th>
              <th>Email vérifié</th>
              <th>Actif</th>
              <th>Rôles</th>
              <th>Date de création</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td>{user.id}</td>
                <td>{user.username}</td>
                <td>{user.email}</td>
                <td>
                  <span className={user.emailVerified ? 'badge badge-success' : 'badge badge-danger'}>
                    {user.emailVerified ? 'Oui' : 'Non'}
                  </span>
                </td>
                <td>
                  <span className={user.enabled ? 'badge badge-success' : 'badge badge-danger'}>
                    {user.enabled ? 'Oui' : 'Non'}
                  </span>
                </td>
                <td>{Array.isArray(user.roles) ? user.roles.join(', ') : user.roles}</td>
                <td>{new Date(user.createdAt).toLocaleDateString('fr-FR')}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default AdminPanel;


