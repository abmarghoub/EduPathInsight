import React, { createContext, useState, useContext, useEffect } from 'react'
import api from '../services/api'
import { toast } from 'react-toastify'

const AuthContext = createContext(null)

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(localStorage.getItem('token'))
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const initAuth = async () => {
      if (token) {
        await validateToken()
      } else {
        setLoading(false)
      }
    }
    initAuth()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const validateToken = async () => {
    try {
      // Décoder le JWT pour extraire les informations
      // Le token contient: sub (username), email, roles
      const tokenParts = token.split('.')
      if (tokenParts.length === 3) {
        const payload = JSON.parse(atob(tokenParts[1]))
        const userData = {
          username: payload.sub || payload.username,
          email: payload.email,
          roles: payload.roles ? (typeof payload.roles === 'string' ? payload.roles.split(',') : payload.roles) : []
        }
        setUser(userData)
        setLoading(false)
      } else {
        throw new Error('Token invalide')
      }
    } catch (error) {
      // En cas d'erreur, nettoyer et continuer
      localStorage.removeItem('token')
      setToken(null)
      setUser(null)
      setLoading(false)
    }
  }

  const login = async (username, password) => {
    try {
      const response = await api.post('/api/auth/login', { usernameOrEmail: username, password })
      const { token: newToken, username: responseUsername, email, roles, message } = response.data
      
      if (newToken) {
        // Mettre à jour le token d'abord
        localStorage.setItem('token', newToken)
        setToken(newToken)
        
        // Construire l'objet user à partir de la réponse
        const userData = {
          username: responseUsername,
          email: email,
          roles: roles ? (Array.isArray(roles) ? roles : [roles]) : []
        }
        setUser(userData)
        
        if (message) {
          toast.success(message)
        }
        
        // Forcer la mise à jour de l'état
        return { success: true, user: userData, token: newToken }
      } else {
        throw new Error('Token non reçu')
      }
    } catch (error) {
      const message = error.response?.data?.message || error.message || 'Erreur de connexion'
      toast.error(message)
      return { success: false, error: message }
    }
  }

  const logout = () => {
    localStorage.removeItem('token')
    setToken(null)
    setUser(null)
  }

  const hasRole = (role) => {
    if (!user || !user.roles) return false
    // Les rôles peuvent être un tableau de strings ou un tableau d'objets
    return user.roles.some(r => {
      if (typeof r === 'string') {
        return r === role || r === `ROLE_${role}` || r === role.replace('ROLE_', '')
      }
      return r.name === role || r === role
    })
  }

  const value = {
    user,
    token,
    loading,
    login,
    logout,
    hasRole,
    isAuthenticated: !!token && !!user
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}


