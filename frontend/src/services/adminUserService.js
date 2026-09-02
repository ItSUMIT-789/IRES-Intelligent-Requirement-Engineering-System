import { apiClient } from './apiClient.js'

export const adminUserService = {
  getUsers: () => apiClient.get('/admin/users'),
  getEligibleDevelopers: (projectId) => apiClient.get(`/admin/users/eligible-developers/${projectId}`),
}
