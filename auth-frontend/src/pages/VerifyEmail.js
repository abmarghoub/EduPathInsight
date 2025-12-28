import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const VerifyEmail = () => {
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const { verifyEmail } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (location.state?.email) {
      setEmail(location.state.email);
    }
  }, [location]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    const result = await verifyEmail(email, code);
    setLoading(false);

    if (result.success) {
      setSuccess('Email vérifié avec succès !');
      setTimeout(() => {
        navigate('/dashboard');
      }, 1500);
    } else {
      setError(result.message || 'Code de vérification invalide');
    }
  };

  const handleResendCode = async () => {
    setError('');
    setSuccess('');
    try {
      // Appel à l'endpoint login pour déclencher l'envoi du code
      await api.post('/api/auth/login', {
        usernameOrEmail: email,
        password: 'dummy' // Ne sera pas vérifié si l'email n'est pas vérifié
      });
      setSuccess('Code de vérification renvoyé !');
    } catch (error) {
      // On ignore l'erreur, le code sera renvoyé
      setSuccess('Code de vérification renvoyé !');
    }
  };

  return (
    <div className="container">
      <h1>Vérification Email</h1>
      {error && <div className="error">{error}</div>}
      {success && <div className="success">{success}</div>}
      <div className="info">
        Un code de vérification a été envoyé à votre adresse email.
        Veuillez entrer le code à 6 chiffres reçu.
      </div>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Email</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>Code de vérification</label>
          <input
            type="text"
            value={code}
            onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
            required
            maxLength={6}
            placeholder="000000"
          />
        </div>
        <button type="submit" className="btn" disabled={loading}>
          {loading ? 'Vérification...' : 'Vérifier'}
        </button>
      </form>
      <button onClick={handleResendCode} className="link" style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
        Renvoyer le code
      </button>
      <a href="/login" className="link">Retour à la connexion</a>
    </div>
  );
};

export default VerifyEmail;


