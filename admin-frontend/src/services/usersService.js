import api from './api'

export const usersService = {
  getAllUsers: () => api.get('/api/auth/admin/users'),
  
  createUser: (userData) => api.post('/api/auth/admin/create-user', userData),
  
  blockUser: (userId) => api.put(`/api/auth/admin/users/${userId}/block`),
  
  unblockUser: (userId) => api.put(`/api/auth/admin/users/${userId}/unblock`),
  
  importUsers: (file, async = false) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('async', async)
    return api.post('/api/auth/admin/users/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  exportUsersCSV: () => api.get('/api/auth/admin/users/export/csv', {
    responseType: 'blob'
  }),
  
  exportUsersExcel: () => api.get('/api/auth/admin/users/export/excel', {
    responseType: 'blob'
  })
}


