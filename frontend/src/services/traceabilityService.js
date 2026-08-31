import { apiClient } from './apiClient.js'

export const traceabilityService = {
  list: (requirementId, params = {}) => {
    const query = new URLSearchParams(params).toString()
    return apiClient.get(`/requirements/${requirementId}/traceability${query ? `?${query}` : ''}`)
  },
  createLink: (requirementId, link) => apiClient.post(`/requirements/${requirementId}/traceability/links`, link),
  removeLink: (id) => apiClient.delete(`/traceability-links/${id}`),
}
