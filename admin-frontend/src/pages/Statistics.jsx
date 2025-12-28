import React, { useState, useEffect } from 'react'
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { notesService } from '../services/notesService'
import { modulesService } from '../services/modulesService'
import './Statistics.css'

const Statistics = () => {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadStatistics()
  }, [])

  const loadStatistics = async () => {
    try {
      // TODO: Implémenter l'endpoint de statistiques
      setLoading(false)
    } catch (error) {
      console.error('Erreur lors du chargement des statistiques:', error)
      setLoading(false)
    }
  }

  if (loading) {
    return <div className="page-container">Chargement...</div>
  }

  // Données de démonstration
  const sampleData = [
    { name: 'Jan', notes: 65, présences: 78 },
    { name: 'Fév', notes: 70, présences: 82 },
    { name: 'Mar', notes: 68, présences: 80 },
    { name: 'Avr', notes: 72, présences: 85 },
    { name: 'Mai', notes: 75, présences: 88 }
  ]

  return (
    <div className="page-container">
      <h1>Statistiques</h1>
      
      <div className="charts-grid">
        <div className="chart-card">
          <h2>Évolution des notes moyennes</h2>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={sampleData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Line type="monotone" dataKey="notes" stroke="#3498db" strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="chart-card">
          <h2>Taux de présence</h2>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={sampleData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Bar dataKey="présences" fill="#2ecc71" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  )
}

export default Statistics


