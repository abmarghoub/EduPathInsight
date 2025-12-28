import React, { useState, useEffect } from 'react'
import { predictionsService } from '../services/predictionsService'
import { modulesService } from '../services/modulesService'
import { toast } from 'react-toastify'
import './Predictions.css'

const Predictions = () => {
  const [riskModules, setRiskModules] = useState([])
  const [loading, setLoading] = useState(true)
  const [selectedStudent, setSelectedStudent] = useState('')
  const [selectedModule, setSelectedModule] = useState(null)
  const [prediction, setPrediction] = useState(null)
  const [modules, setModules] = useState([])

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [riskModulesRes, modulesRes] = await Promise.all([
        predictionsService.getRiskModules(),
        modulesService.getAllModules()
      ])
      setRiskModules(riskModulesRes.data || [])
      setModules(modulesRes.data || [])
      setLoading(false)
    } catch (error) {
      toast.error('Erreur lors du chargement des prédictions')
      setLoading(false)
    }
  }

  const handlePredict = async () => {
    if (!selectedStudent || !selectedModule) {
      toast.error('Veuillez sélectionner un étudiant et un module')
      return
    }
    try {
      const response = await predictionsService.predictStudentModule(selectedStudent, selectedModule)
      setPrediction(response.data)
    } catch (error) {
      toast.error('Erreur lors de la prédiction')
    }
  }

  if (loading) {
    return <div className="page-container">Chargement...</div>
  }

  return (
    <div className="page-container">
      <h1>Prédictions IA</h1>

      <div className="predictions-grid">
        <div className="prediction-card">
          <h2>Modules à risque</h2>
          <div className="risk-modules-list">
            {riskModules.map((module, index) => (
              <div key={index} className="risk-module-item">
                <div className="risk-module-info">
                  <strong>{module.moduleCode} - {module.moduleName}</strong>
                  <div className="risk-score">
                    Score de risque: {(module.riskScore * 100).toFixed(1)}%
                  </div>
                  <div className="at-risk-count">
                    {module.at_risk_students_count} étudiants à risque
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="prediction-card">
          <h2>Prédiction pour un étudiant</h2>
          <div className="prediction-form">
            <div className="form-group">
              <label>ID Étudiant</label>
              <input
                type="text"
                value={selectedStudent}
                onChange={(e) => setSelectedStudent(e.target.value)}
                placeholder="student-1"
              />
            </div>
            <div className="form-group">
              <label>Module</label>
              <select
                value={selectedModule || ''}
                onChange={(e) => setSelectedModule(parseInt(e.target.value))}
              >
                <option value="">Sélectionner un module</option>
                {modules.map(m => (
                  <option key={m.id} value={m.id}>{m.code} - {m.name}</option>
                ))}
              </select>
            </div>
            <button onClick={handlePredict} className="btn-primary">
              Prédire
            </button>
          </div>

          {prediction && (
            <div className="prediction-result">
              <h3>Résultat de la prédiction</h3>
              <div className="prediction-metrics">
                <div className="metric">
                  <label>Probabilité de réussite:</label>
                  <span className="metric-value success">
                    {(prediction.successProbability * 100).toFixed(1)}%
                  </span>
                </div>
                <div className="metric">
                  <label>Probabilité d'abandon:</label>
                  <span className="metric-value danger">
                    {(prediction.dropoutProbability * 100).toFixed(1)}%
                  </span>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default Predictions


