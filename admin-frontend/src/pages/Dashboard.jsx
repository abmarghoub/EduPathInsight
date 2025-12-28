import React, { useState, useEffect } from 'react'
import { modulesService } from '../services/modulesService'
import { notesService } from '../services/notesService'
import { activitiesService } from '../services/activitiesService'
import { predictionsService } from '../services/predictionsService'
import './Dashboard.css'

const Dashboard = () => {
  const [stats, setStats] = useState({
    totalModules: 0,
    totalStudents: 0,
    totalNotes: 0,
    riskModules: 0
  })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadStats()
  }, [])

  const loadStats = async () => {
    try {
      const [modules, riskModules] = await Promise.all([
        modulesService.getAllModules(),
        predictionsService.getRiskModules()
      ])

      setStats({
        totalModules: modules.data?.length || 0,
        totalStudents: 0, // TODO: Get from users service
        totalNotes: 0, // TODO: Get from notes service
        riskModules: riskModules.data?.length || 0
      })
      setLoading(false)
    } catch (error) {
      console.error('Error loading stats:', error)
      setLoading(false)
    }
  }

  if (loading) {
    return <div className="dashboard">Chargement...</div>
  }

  return (
    <div className="dashboard">
      <h1>Dashboard Administrateur</h1>
      
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon">📚</div>
          <div className="stat-info">
            <div className="stat-value">{stats.totalModules}</div>
            <div className="stat-label">Modules</div>
          </div>
        </div>
        
        <div className="stat-card">
          <div className="stat-icon">👥</div>
          <div className="stat-info">
            <div className="stat-value">{stats.totalStudents}</div>
            <div className="stat-label">Étudiants</div>
          </div>
        </div>
        
        <div className="stat-card">
          <div className="stat-icon">📝</div>
          <div className="stat-info">
            <div className="stat-value">{stats.totalNotes}</div>
            <div className="stat-label">Notes</div>
          </div>
        </div>
        
        <div className="stat-card warning">
          <div className="stat-icon">⚠️</div>
          <div className="stat-info">
            <div className="stat-value">{stats.riskModules}</div>
            <div className="stat-label">Modules à risque</div>
          </div>
        </div>
      </div>

      <div className="dashboard-content">
        <div className="dashboard-section">
          <h2>Actions rapides</h2>
          <div className="quick-actions">
            <button className="action-btn">Ajouter un module</button>
            <button className="action-btn">Ajouter un étudiant</button>
            <button className="action-btn">Importer des notes</button>
            <button className="action-btn">Voir les prédictions</button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Dashboard


