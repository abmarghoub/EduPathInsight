import api from './api'

export const activitiesService = {
  getAllPresences: () => api.get('/api/activities/admin/presences'),
  
  getPresencesByModule: (moduleId) => 
    api.get(`/api/activities/admin/presences/module/${moduleId}`),
  
  getPresencesByStudent: (studentId) => 
    api.get(`/api/activities/admin/presences/student/${studentId}`),
  
  createPresence: (presenceData) => 
    api.post('/api/activities/admin/presences', presenceData),
  
  updatePresence: (id, presenceData) => 
    api.put(`/api/activities/admin/presences/${id}`, presenceData),
  
  deletePresence: (id) => api.delete(`/api/activities/admin/presences/${id}`),
  
  getAllActivities: () => api.get('/api/activities/admin/activities'),
  
  getActivitiesByModule: (moduleId) => 
    api.get(`/api/activities/admin/activities/module/${moduleId}`),
  
  getActivitiesByStudent: (studentId) => 
    api.get(`/api/activities/admin/activities/student/${studentId}`),
  
  createActivity: (activityData) => 
    api.post('/api/activities/admin/activities', activityData),
  
  updateActivity: (id, activityData) => 
    api.put(`/api/activities/admin/activities/${id}`, activityData),
  
  deleteActivity: (id) => api.delete(`/api/activities/admin/activities/${id}`),
  
  getAnomalies: (studentId) => 
    api.get(`/api/activities/admin/anomalies/student/${studentId}`),
  
  checkAnomalies: (studentId, moduleId) => 
    api.post('/api/activities/admin/anomalies/check', { studentId, moduleId }),
  
  importPresences: (file, async = false) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('async', async)
    return api.post('/api/activities/admin/presences/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  exportPresencesTemplate: (templateData) => 
    api.post('/api/activities/admin/presences/export/template', templateData, {
      responseType: 'blob'
    }),
  
  exportPresencesTemplateExcel: (templateData) => 
    api.post('/api/activities/admin/presences/export/template/excel', templateData, {
      responseType: 'blob'
    }),
  
  exportPresencesCSV: (moduleId) => 
    api.get('/api/activities/admin/presences/export/csv', {
      params: moduleId ? { module_id: moduleId } : {},
      responseType: 'blob'
    }),
  
  exportPresencesExcel: (moduleId) => 
    api.get('/api/activities/admin/presences/export/excel', {
      params: moduleId ? { module_id: moduleId } : {},
      responseType: 'blob'
    }),
  
  importActivities: (file, async = false) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('async', async)
    return api.post('/api/activities/admin/activities/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  exportActivitiesCSV: (moduleId) => 
    api.get('/api/activities/admin/activities/export/csv', {
      params: moduleId ? { module_id: moduleId } : {},
      responseType: 'blob'
    }),
  
  exportActivitiesExcel: (moduleId) => 
    api.get('/api/activities/admin/activities/export/excel', {
      params: moduleId ? { module_id: moduleId } : {},
      responseType: 'blob'
    })
}


