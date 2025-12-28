import React, { useState, useEffect } from 'react'
import { notesService } from '../services/notesService'
import { modulesService } from '../services/modulesService'
import { toast } from 'react-toastify'
import './NotesManagement.css'

const NotesManagement = () => {
  const [notes, setNotes] = useState([])
  const [modules, setModules] = useState([])
  const [loading, setLoading] = useState(true)
  const [showAddModal, setShowAddModal] = useState(false)
  const [selectedModule, setSelectedModule] = useState(null)
  const [newNote, setNewNote] = useState({
    studentId: '',
    moduleId: null,
    evaluationType: 'Exam',
    evaluationTitle: '',
    score: 0,
    maxScore: 20,
    comments: '',
    evaluationDate: new Date().toISOString().split('T')[0]
  })
  const [importFile, setImportFile] = useState(null)
  const [exportTemplateData, setExportTemplateData] = useState({
    moduleId: null,
    moduleCode: '',
    moduleName: '',
    evaluationType: 'Exam',
    evaluationTitle: '',
    maxScore: 20
  })

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [notesRes, modulesRes] = await Promise.all([
        notesService.getAllNotes(),
        modulesService.getAllModules()
      ])
      setNotes(notesRes.data || [])
      setModules(modulesRes.data || [])
      setLoading(false)
    } catch (error) {
      toast.error('Erreur lors du chargement des données')
      setLoading(false)
    }
  }

  const handleAddNote = async (e) => {
    e.preventDefault()
    try {
      await notesService.createNote(newNote)
      toast.success('Note ajoutée avec succès')
      setShowAddModal(false)
      setNewNote({
        studentId: '',
        moduleId: null,
        evaluationType: 'Exam',
        evaluationTitle: '',
        score: 0,
        maxScore: 20,
        comments: '',
        evaluationDate: new Date().toISOString().split('T')[0]
      })
      loadData()
    } catch (error) {
      toast.error('Erreur lors de l\'ajout de la note')
    }
  }

  const handleExportTemplate = async () => {
    if (!exportTemplateData.moduleId || !exportTemplateData.evaluationTitle) {
      toast.error('Veuillez remplir tous les champs requis')
      return
    }
    try {
      const module = modules.find(m => m.id === exportTemplateData.moduleId)
      const templateData = {
        moduleId: exportTemplateData.moduleId,
        moduleCode: module.code,
        moduleName: module.name,
        evaluationType: exportTemplateData.evaluationType,
        evaluationTitle: exportTemplateData.evaluationTitle,
        maxScore: exportTemplateData.maxScore
      }
      const response = await notesService.exportNotesTemplate(templateData)
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `notes_template_${module.code}.csv`)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
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
      await notesService.importNotes(importFile, false)
      toast.success('Import en cours...')
      loadData()
    } catch (error) {
      toast.error('Erreur lors de l\'import')
    }
  }

  const handleExportCSV = async () => {
    try {
      const response = await notesService.exportNotesCSV()
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', 'notes_export.csv')
      document.body.appendChild(link)
      link.click()
      toast.success('Export CSV réussi')
    } catch (error) {
      toast.error('Erreur lors de l\'export CSV')
    }
  }

  if (loading) {
    return <div className="page-container">Chargement...</div>
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>Gestion des Notes</h1>
        <div className="page-actions">
          <button onClick={() => setShowAddModal(true)} className="btn-primary">
            + Ajouter une note
          </button>
          <button onClick={() => setShowExportModal(true)} className="btn-secondary">
            📥 Exporter template
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
        </div>
      </div>

      <div className="filters">
        <select
          value={selectedModule || ''}
          onChange={(e) => setSelectedModule(e.target.value ? parseInt(e.target.value) : null)}
        >
          <option value="">Tous les modules</option>
          {modules.map(m => (
            <option key={m.id} value={m.id}>{m.code} - {m.name}</option>
          ))}
        </select>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Étudiant</th>
              <th>Module</th>
              <th>Type</th>
              <th>Titre</th>
              <th>Note</th>
              <th>Max</th>
              <th>%</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            {notes
              .filter(note => !selectedModule || note.moduleId === selectedModule)
              .map((note) => (
                <tr key={note.id}>
                  <td>{note.id}</td>
                  <td>{note.studentUsername}</td>
                  <td>{note.moduleCode}</td>
                  <td>{note.evaluationType}</td>
                  <td>{note.evaluationTitle}</td>
                  <td>{note.score}</td>
                  <td>{note.maxScore}</td>
                  <td>{note.percentage?.toFixed(1)}%</td>
                  <td>{note.evaluationDate ? new Date(note.evaluationDate).toLocaleDateString() : '-'}</td>
                </tr>
              ))}
          </tbody>
        </table>
      </div>

      {showAddModal && (
        <div className="modal-overlay" onClick={() => setShowAddModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Ajouter une note</h2>
            <form onSubmit={handleAddNote}>
              <div className="form-group">
                <label>ID Étudiant</label>
                <input
                  type="text"
                  value={newNote.studentId}
                  onChange={(e) => setNewNote({ ...newNote, studentId: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Module</label>
                <select
                  value={newNote.moduleId || ''}
                  onChange={(e) => setNewNote({ ...newNote, moduleId: parseInt(e.target.value) })}
                  required
                >
                  <option value="">Sélectionner un module</option>
                  {modules.map(m => (
                    <option key={m.id} value={m.id}>{m.code} - {m.name}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Type d'évaluation</label>
                <select
                  value={newNote.evaluationType}
                  onChange={(e) => setNewNote({ ...newNote, evaluationType: e.target.value })}
                >
                  <option value="Exam">Examen</option>
                  <option value="Assignment">Devoir</option>
                  <option value="Project">Projet</option>
                  <option value="Quiz">Quiz</option>
                </select>
              </div>
              <div className="form-group">
                <label>Titre</label>
                <input
                  type="text"
                  value={newNote.evaluationTitle}
                  onChange={(e) => setNewNote({ ...newNote, evaluationTitle: e.target.value })}
                  required
                />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Note</label>
                  <input
                    type="number"
                    step="0.01"
                    value={newNote.score}
                    onChange={(e) => setNewNote({ ...newNote, score: parseFloat(e.target.value) })}
                    required
                    min="0"
                  />
                </div>
                <div className="form-group">
                  <label>Note max</label>
                  <input
                    type="number"
                    step="0.01"
                    value={newNote.maxScore}
                    onChange={(e) => setNewNote({ ...newNote, maxScore: parseFloat(e.target.value) })}
                    required
                    min="0"
                  />
                </div>
              </div>
              <div className="form-group">
                <label>Date</label>
                <input
                  type="date"
                  value={newNote.evaluationDate}
                  onChange={(e) => setNewNote({ ...newNote, evaluationDate: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Commentaires</label>
                <textarea
                  value={newNote.comments || ''}
                  onChange={(e) => setNewNote({ ...newNote, comments: e.target.value })}
                  rows="3"
                />
              </div>
              <div className="modal-actions">
                <button type="button" onClick={() => setShowAddModal(false)} className="btn-secondary">
                  Annuler
                </button>
                <button type="submit" className="btn-primary">
                  Ajouter
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showExportModal && (
        <div className="modal-overlay" onClick={() => setShowExportModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Exporter template de notes</h2>
            <div className="form-group">
              <label>Module</label>
              <select
                value={exportTemplateData.moduleId || ''}
                onChange={(e) => {
                  const module = modules.find(m => m.id === parseInt(e.target.value))
                  setExportTemplateData({
                    ...exportTemplateData,
                    moduleId: parseInt(e.target.value),
                    moduleCode: module?.code || '',
                    moduleName: module?.name || ''
                  })
                }}
                required
              >
                <option value="">Sélectionner un module</option>
                {modules.map(m => (
                  <option key={m.id} value={m.id}>{m.code} - {m.name}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>Type d'évaluation</label>
              <select
                value={exportTemplateData.evaluationType}
                onChange={(e) => setExportTemplateData({ ...exportTemplateData, evaluationType: e.target.value })}
              >
                <option value="Exam">Examen</option>
                <option value="Assignment">Devoir</option>
                <option value="Project">Projet</option>
                <option value="Quiz">Quiz</option>
              </select>
            </div>
            <div className="form-group">
              <label>Titre de l'évaluation</label>
              <input
                type="text"
                value={exportTemplateData.evaluationTitle}
                onChange={(e) => setExportTemplateData({ ...exportTemplateData, evaluationTitle: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Note maximale</label>
              <input
                type="number"
                value={exportTemplateData.maxScore}
                onChange={(e) => setExportTemplateData({ ...exportTemplateData, maxScore: parseInt(e.target.value) })}
                required
                min="0"
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

export default NotesManagement

