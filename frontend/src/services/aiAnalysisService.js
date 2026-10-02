import { apiClient } from './apiClient.js'
export const aiAnalysisService = {
  improveRequirement: (requirementId) => apiClient.post(`/requirements/${requirementId}/ai/improve`, {}),
  classifyRequirement: (requirementId) => apiClient.post(`/requirements/${requirementId}/ai/classify`, {}),
  detectAmbiguity: (requirementId) => apiClient.post(`/requirements/${requirementId}/ai/ambiguity`, {}),
  detectIncompleteRequirement: (requirementId) => apiClient.post(`/requirements/${requirementId}/ai/completeness`, {}),
  analyzeRequirementQuality: (requirementId) => apiClient.post(`/requirements/${requirementId}/ai/quality`, {}),
  detectDuplicateRequirements: (requirementId, candidateRequirementIds) => apiClient.post(
    `/requirements/${requirementId}/ai/duplicates`,
    { candidateRequirementIds },
  ),
  detectRequirementConflicts: (requirementId, candidateRequirementIds) => apiClient.post(
    `/requirements/${requirementId}/ai/conflicts`,
    { candidateRequirementIds },
  ),
}
