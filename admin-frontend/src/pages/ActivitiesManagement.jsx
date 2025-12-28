import React, { useState, useEffect } from 'react'
import { activitiesService } from '../services/activitiesService'
import { modulesService } from '../services/modulesService'
import { toast } from 'react-toastify'
import './ActivitiesManagement.css'

const ActivitiesManagement = () => {
  const [presences, setPresences] = useState([])
  const [modules, setModules] = useState([])
  const [loading, setLoading] = useState(true)
  const [selectedModule, setSelectedModule] = useState(null)
  const [importFile, setImportFile] = useState(null)
  const [showExportModal, setShowExportModal] = useState(false)
  const [exportTemplateData, setExportTemplateData] = useState({
    module_id: null,
    session_date: '',
    session_time: ''
  })

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [modulesRes] = await Promise.all([
        modulesService.getAllModules()
      ])
      setModules(modulesRes.data || [])
      setLoading(false)
    } catch (error) {
      toast.error('Erreur lors du chargement des données')
      setLoading(false)
    }
  }

  const loadPresences = async (moduleId) => {
    try {
      const response = await activitiesService.getPresencesByModule(moduleId)
      setPresences(response.data || [])
    } catch (error) {
      toast.error('Erreur lors du chargement des présences')
    }
  }

  useEffect(() => {
    if (selectedModule) {
      loadPresences(selectedModule)
    }
  }, [selectedModule])

  const handleExportTemplate = async () => {
    if (!exportTemplateData.module_id || !exportTemplateData.session_date) {
      toast.error('Veuillez remplir tous les champs requis')
      return
    }
    try {
      const response = await activitiesService.exportPresencesTemplate(exportTemplateData)
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `presence_template_${exportTemplateData.module_id}.csv`)
      document.body.appendChild(link)
      link.click()
      toast.success('Template exporté avec succès')
    } catch (error) {
      toast.error('Erreur lors de l\'export du template')
    }
  }

  const handleImport = async () => {
    if (!importFile) {
      toast.error('Veuillez sélectionner un fichier')
      return
    }
    try {
      await activitiesService.importPresences(importFile, false)
      toast.success('Import en cours...')
      if (selectedModule) {
        loadPresences(selectedModule)
      }
    } catch (error) {
      toast.error('Erreur lors de l\'import')
    }
  }

  if (loading) {
    return <div className="page-container">Chargement...</div>
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>Gestion des Activités</h1>
        <div className="page-actions">
          <button onClick={() => setShowExportModal(true)} className="btn-secondary">
            📥 Exporter template présences
          </button>
          <label className="btn-secondary">
            📥 Importer présences
            <input
              type="file"
              accept=".csv,.xlsx,.xls"
              onChange={(e) => setImportFile(e.target.files[0])}
              style={{ display: 'none' }}
            />
          </label>
          <button onClick={handleImport} className="btn-secondary" disabled={!importFile}>
            Valider l'import
          </button>
        </div>
      </div>

      <div className="filters">
        <label>
          Module:
          <select
            value={selectedModule || ''}
            onChange={(e) => setSelectedModule(e.target.value ? parseInt(e.target.value) : null)}
          >
            <option value="">Sélectionner un module</option>
            {modules.map(m => (
              <option key={m.id} value={m.id}>{m.code} - {m.name}</option>
            ))}
          </select>
        </label>
      </div>

      {selectedModule && (
        <div className="table-container">
          <h2>Présences</h2>
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Étudiant</th>
                <th>Date</th>
                <th>Heure</th>
                <th>Statut</th>
                <th>Notes</th>
              </tr>
            </thead>
            <tbody>
              {presences.map((presence) => (
                <tr key={presence.id}>
                  <td>{presence.id}</td>
                  <td>{presence.studentUsername}</td>
                  <td>{presence.sessionDate ? new Date(presence.sessionDate).toLocaleDateString() : '-'}</td>
                  <td>{presence.sessionTime || '-'}</td>
                  <td>
                    <span className={`status-badge ${presence.status === 'PRESENT' ? 'active' : 'inactive'}`}>
                      {presence.status}
                    </span>
                  </td>
                  <td>{presence.notes || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showExportModal && (
        <div className="modal-overlay" onClick={() => setShowExportModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Exporter template de présences</h2>
            <div className="form-group">
              <label>Module</label>
              <select
                value={exportTemplateData.module_id || ''}
                onChange={(e) => setExportTemplateData({ ...exportTemplateData, module_id: parseInt(e.target.value) })}
                required
              >
                <option value="">Sélectionner un module</option>
                {modules.map(m => (
                  <option key={m.id} value={m.id}>{m.code} - {m.name}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>Date de session</label>
              <input
                type="date"
                value={exportTemplateData.session_date}
                onChange={(e) => setExportTemplateData({ ...exportTemplateData, session_date: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Heure de session (optionnel)</label>
              <input
                type="time"
                value={exportTemplateData.session_time}
                onChange={(e) => setExportTemplateData({ ...exportTemplateData, session_time: e.target.value })}
              />
            </div>
            <div className="modal-actions">
              <button type="button" onClick={() => setShowExportModal(false)} className="btn-secondary">
                Annuler
              </button>
              <button onClick={() => {
                handleExportTemplate()
                setShowExportModal(false)
              }} className="btn-primary">
                Exporter
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default ActivitiesManagement

