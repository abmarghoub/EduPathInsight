import React, { useState, useEffect } from 'react'
import { modulesService } from '../services/modulesService'
import { toast } from 'react-toastify'
import './ModulesManagement.css'

const ModulesManagement = () => {
  const [modules, setModules] = useState([])
  const [loading, setLoading] = useState(true)
  const [showAddModal, setShowAddModal] = useState(false)
  const [selectedModule, setSelectedModule] = useState(null)
  const [newModule, setNewModule] = useState({
    code: '',
    name: '',
    description: '',
    credits: 0,
    active: true
  })
  const [importFile, setImportFile] = useState(null)

  useEffect(() => {
    loadModules()
  }, [])

  const loadModules = async () => {
    try {
      const response = await modulesService.getAllModules()
      setModules(response.data || [])
      setLoading(false)
    } catch (error) {
      toast.error('Erreur lors du chargement des modules')
      setLoading(false)
    }
  }

  const handleAddModule = async (e) => {
    e.preventDefault()
    try {
      await modulesService.createModule(newModule)
      toast.success('Module créé avec succès')
      setShowAddModal(false)
      setNewModule({ code: '', name: '', description: '', credits: 0, active: true })
      loadModules()
    } catch (error) {
      toast.error('Erreur lors de la création du module')
    }
  }

  const handleUpdateModule = async (id, moduleData) => {
    try {
      await modulesService.updateModule(id, moduleData)
      toast.success('Module mis à jour avec succès')
      loadModules()
      setSelectedModule(null)
    } catch (error) {
      toast.error('Erreur lors de la mise à jour du module')
    }
  }

  const handleDeleteModule = async (id) => {
    if (!window.confirm('Êtes-vous sûr de vouloir supprimer ce module ?')) {
      return
    }
    try {
      await modulesService.deleteModule(id)
      toast.success('Module supprimé avec succès')
      loadModules()
    } catch (error) {
      toast.error('Erreur lors de la suppression du module')
    }
  }

  const handleImport = async () => {
    if (!importFile) {
      toast.error('Veuillez sélectionner un fichier')
      return
    }
    try {
      await modulesService.importModules(importFile, false)
      toast.success('Import en cours...')
      loadModules()
    } catch (error) {
      toast.error('Erreur lors de l\'import')
    }
  }

  const handleExportCSV = async () => {
    try {
      const response = await modulesService.exportModulesCSV()
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', 'modules_export.csv')
      document.body.appendChild(link)
      link.click()
      toast.success('Export CSV réussi')
    } catch (error) {
      toast.error('Erreur lors de l\'export CSV')
    }
  }

  const handleExportExcel = async () => {
    try {
      const response = await modulesService.exportModulesExcel()
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', 'modules_export.xlsx')
      document.body.appendChild(link)
      link.click()
      toast.success('Export Excel réussi')
    } catch (error) {
      toast.error('Erreur lors de l\'export Excel')
    }
  }

  if (loading) {
    return <div className="page-container">Chargement...</div>
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>Gestion des Modules</h1>
        <div className="page-actions">
          <button onClick={() => setShowAddModal(true)} className="btn-primary">
            + Ajouter un module
          </button>
          <label className="btn-secondary">
            📥 Importer
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
          <button onClick={handleExportCSV} className="btn-secondary">
            📤 Exporter CSV
          </button>
          <button onClick={handleExportExcel} className="btn-secondary">
            📤 Exporter Excel
          </button>
        </div>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Code</th>
              <th>Nom</th>
              <th>Description</th>
              <th>Crédits</th>
              <th>Statut</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {modules.map((module) => (
              <tr key={module.id}>
                <td>{module.id}</td>
                <td>{module.code}</td>
                <td>{module.name}</td>
                <td>{module.description || '-'}</td>
                <td>{module.credits}</td>
                <td>
                  <span className={`status-badge ${module.active ? 'active' : 'inactive'}`}>
                    {module.active ? 'Actif' : 'Inactif'}
                  </span>
                </td>
                <td>
                  <button
                    onClick={() => setSelectedModule(module)}
                    className="btn-small"
                  >
                    Modifier
                  </button>
                  <button
                    onClick={() => handleDeleteModule(module.id)}
                    className="btn-small btn-danger"
                  >
                    Supprimer
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showAddModal && (
        <div className="modal-overlay" onClick={() => setShowAddModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Ajouter un module</h2>
            <form onSubmit={handleAddModule}>
              <div className="form-group">
                <label>Code</label>
                <input
                  type="text"
                  value={newModule.code}
                  onChange={(e) => setNewModule({ ...newModule, code: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Nom</label>
                <input
                  type="text"
                  value={newModule.name}
                  onChange={(e) => setNewModule({ ...newModule, name: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea
                  value={newModule.description}
                  onChange={(e) => setNewModule({ ...newModule, description: e.target.value })}
                  rows="3"
                />
              </div>
              <div className="form-group">
                <label>Crédits</label>
                <input
                  type="number"
                  value={newModule.credits}
                  onChange={(e) => setNewModule({ ...newModule, credits: parseInt(e.target.value) })}
                  required
                  min="0"
                />
              </div>
              <div className="modal-actions">
                <button type="button" onClick={() => setShowAddModal(false)} className="btn-secondary">
                  Annuler
                </button>
                <button type="submit" className="btn-primary">
                  Créer
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {selectedModule && (
        <div className="modal-overlay" onClick={() => setSelectedModule(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Modifier le module</h2>
            <form onSubmit={(e) => {
              e.preventDefault()
              handleUpdateModule(selectedModule.id, selectedModule)
            }}>
              <div className="form-group">
                <label>Code</label>
                <input
                  type="text"
                  value={selectedModule.code}
                  onChange={(e) => setSelectedModule({ ...selectedModule, code: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Nom</label>
                <input
                  type="text"
                  value={selectedModule.name}
                  onChange={(e) => setSelectedModule({ ...selectedModule, name: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea
                  value={selectedModule.description || ''}
                  onChange={(e) => setSelectedModule({ ...selectedModule, description: e.target.value })}
                  rows="3"
                />
              </div>
              <div className="form-group">
                <label>Crédits</label>
                <input
                  type="number"
                  value={selectedModule.credits}
                  onChange={(e) => setSelectedModule({ ...selectedModule, credits: parseInt(e.target.value) })}
                  required
                  min="0"
                />
              </div>
              <div className="modal-actions">
                <button type="button" onClick={() => setSelectedModule(null)} className="btn-secondary">
                  Annuler
                </button>
                <button type="submit" className="btn-primary">
                  Enregistrer
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

export default ModulesManagement


