import React, { useState } from 'react'
import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import './Layout.css'

const Layout = () => {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [sidebarOpen, setSidebarOpen] = useState(true)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const isActive = (path) => location.pathname === path

  return (
    <div className="layout">
      <aside className={`sidebar ${sidebarOpen ? 'open' : 'closed'}`}>
        <div className="sidebar-header">
          <h2>EduPath Insight</h2>
          <button className="toggle-btn" onClick={() => setSidebarOpen(!sidebarOpen)}>
            {sidebarOpen ? '←' : '→'}
          </button>
        </div>
        <nav className="sidebar-nav">
          <Link to="/dashboard" className={isActive('/dashboard') ? 'active' : ''}>
            <span>📊</span> {sidebarOpen && 'Dashboard'}
          </Link>
          <Link to="/users" className={isActive('/users') ? 'active' : ''}>
            <span>👥</span> {sidebarOpen && 'Utilisateurs'}
          </Link>
          <Link to="/modules" className={isActive('/modules') ? 'active' : ''}>
            <span>📚</span> {sidebarOpen && 'Modules'}
          </Link>
          <Link to="/notes" className={isActive('/notes') ? 'active' : ''}>
            <span>📝</span> {sidebarOpen && 'Notes'}
          </Link>
          <Link to="/activities" className={isActive('/activities') ? 'active' : ''}>
            <span>📅</span> {sidebarOpen && 'Activités'}
          </Link>
          <Link to="/statistics" className={isActive('/statistics') ? 'active' : ''}>
            <span>📈</span> {sidebarOpen && 'Statistiques'}
          </Link>
          <Link to="/predictions" className={isActive('/predictions') ? 'active' : ''}>
            <span>🔮</span> {sidebarOpen && 'Prédictions'}
          </Link>
        </nav>
        <div className="sidebar-footer">
          <div className="user-info">
            <span>👤</span>
            {sidebarOpen && (
              <div>
                <div className="username">{user?.username}</div>
                <div className="user-role">{user?.roles?.[0]?.name}</div>
              </div>
            )}
          </div>
          <button onClick={handleLogout} className="logout-btn">
            <span>🚪</span> {sidebarOpen && 'Déconnexion'}
          </button>
        </div>
      </aside>
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  )
}

export default Layout


