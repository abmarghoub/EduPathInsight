import api from './api';

export const notesService = {
  getMyNotes: () => api.get('/notes/notes/my'),
  
  getNotesByModule: (moduleId) => api.get(`/notes/notes/module/${moduleId}`),
  
  getMyKPIs: () => api.get('/notes/kpis/my'),
  
  getKPIsByModule: (studentId, moduleId) =>
    api.get(`/notes/kpis/student/${studentId}/module/${moduleId}`),
};


