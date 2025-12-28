import React, { useState, useEffect } from 'react'
import { usersService } from '../services/usersService'
import { toast } from 'react-toastify'
import './UsersManagement.css'

const UsersManagement = () => {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [showAddModal, setShowAddModal] = useState(false)
  const [newUser, setNewUser] = useState({
    username: '',
    email: '',
    password: '',
    roles: ['ROLE_STUDENT']
  })
  const [importFile, setImportFile] = useState(null)

  useEffect(() => {
    loadUsers()
  }, [])

  const loadUsers = async () => {
    try {
      const response = await usersService.getAllUsers()
      setUsers(response.data || [])
      setLoading(false)
    } catch (error) {
      toast.error('Erreur lors du chargement des utilisateurs')
      setLoading(false)
    }
  }

  const handleAddUser = async (e) => {
    e.preventDefault()
    try {
      // S'assurer que roles est un tableau
      const userData = {
        username: newUser.username.trim(),
        email: newUser.email.trim(),
        password: newUser.password,
        roles: Array.isArray(newUser.roles) ? newUser.roles : [newUser.roles]
      }
      
      console.log('Création utilisateur avec:', { ...userData, password: '***' })
      
      const response = await usersService.createUser(userData)
      console.log('Réponse création:', response.data)
      
      toast.success('Utilisateur créé avec succès')
      setShowAddModal(false)
      setNewUser({ username: '', email: '', password: '', roles: ['ROLE_STUDENT'] })
      await loadUsers()
    } catch (error) {
      console.error('Erreur complète:', error)
      console.error('Réponse erreur:', error.response)
      console.error('Données erreur:', error.response?.data)
      
      let errorMessage = 'Erreur lors de la création de l\'utilisateur'
      
      if (error.response) {
        const status = error.response.status
        const data = error.response.data
        
        if (status === 401 || status === 403) {
          errorMessage = 'Vous n\'avez pas les permissions nécessaires. Veuillez vous reconnecter.'
        } else if (status === 400) {
          if (data?.errors) {
            // Erreurs de validation
            const validationErrors = Object.values(data.errors).join(', ')
            errorMessage = `Erreurs de validation: ${validationErrors}`
          } else if (data?.message) {
            errorMessage = data.message
          }
        } else if (status === 503) {
          errorMessage = 'Service temporairement indisponible. Veuillez réessayer dans quelques instants.'
        } else if (data?.message) {
          errorMessage = data.message
        }
      } else if (error.message) {
        errorMessage = error.message
      }
      
      toast.error(errorMessage)
    }
  }

  const handleImport = async () => {
    if (!importFile) {
      toast.error('Veuillez sélectionner un fichier')
      return
    }
    try {
      await usersService.importUsers(importFile, false)
      toast.success('Import en cours...')
      loadUsers()
    } catch (error) {
      toast.error('Erreur lors de l\'import')
    }
  }

  const handleExportCSV = async () => {
    try {
      const response = await usersService.exportUsersCSV()
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', 'users_export.csv')
      document.body.appendChild(link)
      link.click()
      toast.success('Export CSV réussi')
    } catch (error) {
      toast.error('Erreur lors de l\'export CSV')
    }
  }

  const handleExportExcel = async () => {
    try {
      const response = await usersService.exportUsersExcel()
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', 'users_export.xlsx')
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
        <h1>Gestion des Utilisateurs</h1>
        <div className="page-actions">
          <button onClick={() => setShowAddModal(true)} className="btn-primary">
            + Ajouter un utilisateur
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
              <th>Nom d'utilisateur</th>
              <th>Email</th>
              <th>Rôles</th>
              <th>Statut</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td>{user.id}</td>
                <td>{user.username}</td>
                <td>{user.email}</td>
                <td>{user.roles?.map(r => r.name).join(', ') || ''}</td>
                <td>
                  <span className={`status-badge ${user.enabled ? 'active' : 'inactive'}`}>
                    {user.enabled ? 'Actif' : 'Inactif'}
                  </span>
                </td>
                <td>
                  <button
                    onClick={() => {
                      // TODO: Implement block/unblock
                      toast.info('Fonctionnalité à implémenter')
                    }}
                    className="btn-small"
                  >
                    {user.enabled ? 'Bloquer' : 'Débloquer'}
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
            <h2>Ajouter un utilisateur</h2>
            <form onSubmit={handleAddUser}>
              <div className="form-group">
                <label>Nom d'utilisateur</label>
                <input
                  type="text"
                  value={newUser.username}
                  onChange={(e) => setNewUser({ ...newUser, username: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Email</label>
                <input
                  type="email"
                  value={newUser.email}
                  onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Mot de passe</label>
                <input
                  type="password"
                  value={newUser.password}
                  onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Rôle</label>
                <select
                  value={newUser.roles[0]}
                  onChange={(e) => setNewUser({ ...newUser, roles: [e.target.value] })}
                >
                  <option value="ROLE_STUDENT">Étudiant</option>
                  <option value="ROLE_ADMIN">Administrateur</option>
                </select>
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
    </div>
  )
}

export default UsersManagement


