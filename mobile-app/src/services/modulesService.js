import api from './api';

export const modulesService = {
  getAllModules: () => api.get('/modules/modules'),
  
  getModuleById: (id) => api.get(`/modules/modules/${id}`),
  
  enrollInModule: (moduleId, studentId, studentUsername, studentEmail) =>
    api.post('/modules/enrollments', null, {
      params: { moduleId, studentId, studentUsername, studentEmail },
    }),
  
  getMyEnrollments: () => api.get('/modules/enrollments/my'),
  
  cancelEnrollment: (enrollmentId) =>
    api.post(`/modules/enrollments/${enrollmentId}/cancel`),
};


