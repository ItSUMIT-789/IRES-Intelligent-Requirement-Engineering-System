import { apiClient } from './apiClient.js'

export const adminUserService = {
  getUsers: () => apiClient.get('/admin/users'),
}
