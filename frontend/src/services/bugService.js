import { apiClient } from './apiClient.js'

export const bugService = {
  create: (projectId, bug) => apiClient.post(`/projects/${projectId}/bugs`, bug),
  createFromExecution: (executionId, bug) => apiClient.post(`/test-case-executions/${executionId}/bugs`, bug),
  list: (projectId, params = {}) => {
    const query = new URLSearchParams(params).toString()
    return apiClient.get(`/projects/${projectId}/bugs${query ? `?${query}` : ''}`)
  },
  get: (id) => apiClient.get(`/bugs/${id}`),
  update: (id, bug) => apiClient.put(`/bugs/${id}`, bug),
  remove: (id) => apiClient.delete(`/bugs/${id}`),
  resolve: (id) => apiClient.post(`/bugs/${id}/resolve`, {}),
  reopen: (id) => apiClient.post(`/bugs/${id}/reopen`, {}),
}
