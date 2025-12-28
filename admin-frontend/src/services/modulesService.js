import api from './api'

export const modulesService = {
  getAllModules: () => api.get('/api/modules/admin/modules'),
  
  getModuleById: (id) => api.get(`/api/modules/admin/modules/${id}`),
  
  createModule: (moduleData) => api.post('/api/modules/admin/modules', moduleData),
  
  updateModule: (id, moduleData) => api.put(`/api/modules/admin/modules/${id}`, moduleData),
  
  deleteModule: (id) => api.delete(`/api/modules/admin/modules/${id}`),
  
  setEnrollmentPeriod: (moduleId, periodData) => 
    api.post(`/api/modules/admin/modules/${moduleId}/enrollment-period`, periodData),
  
  getEnrollmentPeriod: (moduleId) => 
    api.get(`/api/modules/admin/modules/${moduleId}/enrollment-period`),
  
  getModuleEnrollments: (moduleId) => 
    api.get(`/api/modules/admin/modules/${moduleId}/enrollments`),
  
  enrollStudent: (moduleId, studentId, studentUsername, studentEmail) => 
    api.post('/api/modules/admin/enrollments', null, {
      params: { moduleId, studentId, studentUsername, studentEmail }
    }),
  
  approveEnrollment: (enrollmentId) => 
    api.post(`/api/modules/admin/enrollments/${enrollmentId}/approve`),
  
  rejectEnrollment: (enrollmentId) => 
    api.post(`/api/modules/admin/enrollments/${enrollmentId}/reject`),
  
  importModules: (file, async = false) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('async', async)
    return api.post('/api/modules/admin/modules/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  exportModulesCSV: () => api.get('/api/modules/admin/modules/export/csv', {
    responseType: 'blob'
  }),
  
  exportModulesExcel: () => api.get('/api/modules/admin/modules/export/excel', {
    responseType: 'blob'
  })
}


