import React from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { ToastContainer } from 'react-toastify'
import 'react-toastify/dist/ReactToastify.css'

import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import UsersManagement from './pages/UsersManagement'
import ModulesManagement from './pages/ModulesManagement'
import NotesManagement from './pages/NotesManagement'
import ActivitiesManagement from './pages/ActivitiesManagement'
import Statistics from './pages/Statistics'
import Predictions from './pages/Predictions'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/"
            element={
              <ProtectedRoute requiredRole="ROLE_ADMIN">
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route path="dashboard" element={<Dashboard />} />
            <Route path="users" element={<UsersManagement />} />
            <Route path="modules" element={<ModulesManagement />} />
            <Route path="notes" element={<NotesManagement />} />
            <Route path="activities" element={<ActivitiesManagement />} />
            <Route path="statistics" element={<Statistics />} />
            <Route path="predictions" element={<Predictions />} />
          </Route>
        </Routes>
        <ToastContainer position="top-right" autoClose={3000} />
      </Router>
    </AuthProvider>
  )
}

export default App


