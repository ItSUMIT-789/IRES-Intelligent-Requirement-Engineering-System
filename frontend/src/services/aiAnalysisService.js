import { apiClient } from './apiClient.js'
export const aiAnalysisService = {
  get: (requirementId) => apiClient.get(`/requirements/${requirementId}/ai-analysis`),
  run: (requirementId) => apiClient.post(`/requirements/${requirementId}/ai-analysis`, {}),
}
