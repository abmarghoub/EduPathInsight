import api from './api';

export const predictionsService = {
  getMyPredictions: (studentId) => api.get(`/predictions/student/${studentId}`),
  
  getPrediction: (studentId, moduleId) =>
    api.get(`/predictions/student/${studentId}/module/${moduleId}`),
  
  getTrajectory: (studentId, moduleId) =>
    api.get(`/trajectories/student/${studentId}/module/${moduleId}`),
  
  getExplanation: (studentId, moduleId) =>
    api.get(`/explainability/explain/${studentId}/${moduleId}`),
};


