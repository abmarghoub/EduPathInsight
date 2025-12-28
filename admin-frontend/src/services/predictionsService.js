import api from './api'

export const predictionsService = {
  predictStudentModule: (studentId, moduleId) => 
    api.post('/api/predictions/student-module', { studentId, moduleId }),
  
  getPrediction: (studentId, moduleId) => 
    api.get(`/api/predictions/student/${studentId}/module/${moduleId}`),
  
  getStudentPredictions: (studentId) => 
    api.get(`/api/predictions/student/${studentId}`),
  
  getTrajectory: (studentId, moduleId) => 
    api.get(`/api/trajectories/student/${studentId}/module/${moduleId}`),
  
  getRiskModules: () => api.get('/api/trajectories/risk-modules'),
  
  getExplanation: (studentId, moduleId) => 
    api.get(`/api/explainability/explain/${studentId}/${moduleId}`),
  
  getFeatureImportance: (studentId, moduleId) => 
    api.get(`/api/explainability/features/${studentId}/${moduleId}`),
  
  getReport: (studentId, moduleId, type = 'DASHBOARD') => 
    api.get(`/api/explainability/report/${studentId}/${moduleId}`, {
      params: { type }
    })
}


