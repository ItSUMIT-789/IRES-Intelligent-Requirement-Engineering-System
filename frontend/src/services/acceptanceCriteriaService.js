import { apiClient } from './apiClient.js'

export const acceptanceCriteriaService = {
  create: (requirementId, criteria) => apiClient.post(`/requirements/${requirementId}/acceptance-criteria`, criteria),
  list: (requirementId, params = {}) => {
    const query = new URLSearchParams(params).toString()
    return apiClient.get(`/requirements/${requirementId}/acceptance-criteria${query ? `?${query}` : ''}`)
  },
  update: (id, criteria) => apiClient.put(`/acceptance-criteria/${id}`, criteria),
  remove: (id) => apiClient.delete(`/acceptance-criteria/${id}`),
}
