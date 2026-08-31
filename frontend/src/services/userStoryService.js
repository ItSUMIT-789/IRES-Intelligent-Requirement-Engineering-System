import { apiClient } from './apiClient.js'

export const userStoryService = {
  create: (requirementId, story) => apiClient.post(`/requirements/${requirementId}/user-stories`, story),
  list: (requirementId, params = {}) => {
    const query = new URLSearchParams(params).toString()
    return apiClient.get(`/requirements/${requirementId}/user-stories${query ? `?${query}` : ''}`)
  },
  get: (id) => apiClient.get(`/user-stories/${id}`),
  update: (id, story) => apiClient.put(`/user-stories/${id}`, story),
  remove: (id) => apiClient.delete(`/user-stories/${id}`),
  generate: (requirementId) => apiClient.post(`/requirements/${requirementId}/user-stories/generate`, {}),
}
