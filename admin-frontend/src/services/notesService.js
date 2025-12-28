import api from './api'

export const notesService = {
  getAllNotes: () => api.get('/api/notes/admin/notes'),
  
  getNoteById: (id) => api.get(`/api/notes/admin/notes/${id}`),
  
  getNotesByStudent: (studentId) => api.get(`/api/notes/admin/notes/student/${studentId}`),
  
  getNotesByModule: (moduleId) => api.get(`/api/notes/admin/notes/module/${moduleId}`),
  
  createNote: (noteData) => api.post('/api/notes/admin/notes', noteData),
  
  updateNote: (id, noteData) => api.put(`/api/notes/admin/notes/${id}`, noteData),
  
  deleteNote: (id) => api.delete(`/api/notes/admin/notes/${id}`),
  
  getKPI: (studentId, moduleId) => 
    api.get(`/api/notes/admin/kpis/student/${studentId}/module/${moduleId}`),
  
  calculateKPI: (studentId, moduleId) => 
    api.post(`/api/notes/admin/kpis/student/${studentId}/module/${moduleId}/calculate`),
  
  getAlerts: (studentId) => api.get(`/api/notes/admin/alerts/student/${studentId}/active`),
  
  importNotes: (file, async = false) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('async', async)
    return api.post('/api/notes/admin/notes/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  exportNotesTemplate: (templateData) => 
    api.post('/api/notes/admin/notes/export/template', templateData, {
      responseType: 'blob'
    }),
  
  exportNotesTemplateExcel: (templateData) => 
    api.post('/api/notes/admin/notes/export/template/excel', templateData, {
      responseType: 'blob'
    }),
  
  exportNotesCSV: () => api.get('/api/notes/admin/notes/export/csv', {
    responseType: 'blob'
  }),
  
  exportNotesExcel: () => api.get('/api/notes/admin/notes/export/excel', {
    responseType: 'blob'
  })
}


