import { apiClient } from './apiClient.js'

export const requirementService = {
  list: (projectId, params = '') => apiClient.get(`/projects/${projectId}/requirements${params ? `?${params}` : ''}`),
  listAccessible: (params = {}) => {
    const query = new URLSearchParams(params).toString()
    return apiClient.get(`/requirements${query ? `?${query}` : ''}`)
  },
  listAnalystQueue: (params = {}) => {
    const query = new URLSearchParams(params).toString()
    return apiClient.get(`/requirements/analyst-queue${query ? `?${query}` : ''}`)
  },
  get: (id) => apiClient.get(`/requirements/${id}`),
  create: (projectId, value) => apiClient.post(`/projects/${projectId}/requirements`, value),
  update: (id, value) => apiClient.put(`/requirements/${id}`, value),
  action: (id, action, body) => apiClient.post(`/requirements/${id}/${action}`, body),
  clarifications: (id) => apiClient.get(`/requirements/${id}/clarifications`),
  comments: (id) => apiClient.get(`/requirements/${id}/comments?size=100`),
  addComment: (id, comment) => apiClient.post(`/requirements/${id}/comments`, { comment }),
  attachments: (id) => apiClient.get(`/requirements/${id}/attachments?size=100`),
  uploadAttachment: (id, file) => {
    const body = new FormData()
    body.append('file', file)
    return apiClient.postForm(`/requirements/${id}/attachments`, body)
  },
  downloadAttachment: (id) => apiClient.download(`/attachments/${id}/download`),
}
