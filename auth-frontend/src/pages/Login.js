import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Login = () => {
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const result = await login(usernameOrEmail, password);
    setLoading(false);

    if (result.success) {
      navigate('/dashboard');
    } else {
      if (result.message && result.message.includes('Code de vérification requis')) {
        navigate('/verify-email', { state: { email: usernameOrEmail } });
      } else {
        setError(result.message || 'Erreur de connexion');
      }
    }
  };

  return (
    <div className="container">
      <h1>Connexion</h1>
      {error && <div className="error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Nom d'utilisateur ou Email</label>
          <input
            type="text"
            value={usernameOrEmail}
            onChange={(e) => setUsernameOrEmail(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>Mot de passe</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit" className="btn" disabled={loading}>
          {loading ? 'Connexion...' : 'Se connecter'}
        </button>
      </form>
      <Link to="/register" className="link">
        Pas encore de compte ? S'inscrire
      </Link>
      <Link to="/forgot-password" className="link">
        Mot de passe oublié ?
      </Link>
    </div>
  );
};

export default Login;


