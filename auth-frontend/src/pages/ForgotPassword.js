import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

const ForgotPassword = () => {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      await api.post('/api/auth/forgot-password', { email });
      setSuccess('Un email de réinitialisation a été envoyé à votre adresse.');
      setEmail('');
    } catch (error) {
      setError(error.response?.data?.message || 'Erreur lors de l\'envoi de l\'email');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container">
      <h1>Mot de passe oublié</h1>
      {error && <div className="error">{error}</div>}
      {success && <div className="success">{success}</div>}
      <div className="info">
        Entrez votre adresse email et nous vous enverrons un lien pour réinitialiser votre mot de passe.
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
        <button type="submit" className="btn" disabled={loading}>
          {loading ? 'Envoi...' : 'Envoyer le lien de réinitialisation'}
        </button>
      </form>
      <Link to="/login" className="link">
        Retour à la connexion
      </Link>
    </div>
  );
};

export default ForgotPassword;


