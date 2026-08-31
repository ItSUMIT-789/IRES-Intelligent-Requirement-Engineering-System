import { apiClient } from './apiClient.js'

export const requirementService = {
  list: (projectId, params = '') => apiClient.get(`/projects/${projectId}/requirements${params ? `?${params}` : ''}`),
  get: (id) => apiClient.get(`/requirements/${id}`),
  create: (projectId, value) => apiClient.post(`/projects/${projectId}/requirements`, value),
  update: (id, value) => apiClient.put(`/requirements/${id}`, value),
  action: (id, action, body) => apiClient.post(`/requirements/${id}/${action}`, body),
  clarifications: (id) => apiClient.get(`/requirements/${id}/clarifications`),
}
