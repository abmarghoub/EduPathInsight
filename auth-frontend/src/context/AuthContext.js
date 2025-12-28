import React, { createContext, useState, useContext, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (token) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      // Extraire les informations utilisateur du token (simplifié)
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        setUser({
          username: payload.sub,
          email: payload.email,
          roles: payload.roles ? payload.roles.split(',') : []
        });
      } catch (e) {
        console.error('Error parsing token:', e);
        logout();
      }
    }
    setLoading(false);
  }, [token]);

  const login = async (usernameOrEmail, password) => {
    try {
      const response = await api.post('/api/auth/login', {
        usernameOrEmail,
        password
      });
      const { token: newToken, username, email, roles } = response.data;
      if (newToken) {
        localStorage.setItem('token', newToken);
        setToken(newToken);
        api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
        setUser({ username, email, roles: roles || [] });
        return { success: true };
      } else {
        return { success: false, message: response.data.message || 'Code de vérification requis' };
      }
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || 'Erreur de connexion'
      };
    }
  };

  const register = async (username, email, password) => {
    try {
      await api.post('/api/auth/register', {
        username,
        email,
        password
      });
      return { success: true };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || 'Erreur d\'inscription'
      };
    }
  };

  const verifyEmail = async (email, code) => {
    try {
      const response = await api.post('/api/auth/verify', {
        email,
        code
      });
      const { token: newToken, username, email: userEmail, roles } = response.data;
      if (newToken) {
        localStorage.setItem('token', newToken);
        setToken(newToken);
        api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
        setUser({ username, email: userEmail, roles: roles || [] });
        return { success: true };
      }
      return { success: false };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || 'Code de vérification invalide'
      };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
    delete api.defaults.headers.common['Authorization'];
  };

  const isAdmin = () => {
    return user?.roles?.includes('ROLE_ADMIN') || false;
  };

  const value = {
    user,
    token,
    loading,
    login,
    register,
    verifyEmail,
    logout,
    isAdmin
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};


