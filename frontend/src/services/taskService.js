import { apiClient } from './apiClient.js'

export const taskService = {
  create: (projectId, task) => apiClient.post(`/projects/${projectId}/tasks`, task),
  list: (projectId, params = {}) => {
    const query = new URLSearchParams(params).toString()
    return apiClient.get(`/projects/${projectId}/tasks${query ? `?${query}` : ''}`)
  },
  get: (id) => apiClient.get(`/tasks/${id}`),
  update: (id, task) => apiClient.put(`/tasks/${id}`, task),
  remove: (id) => apiClient.delete(`/tasks/${id}`),
}
