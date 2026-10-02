import { apiClient } from './apiClient.js'

export const humanReviewService = {
  reviewAIArtifact: (artifactType, artifactId, review) => apiClient.post(
    `/ai-artifacts/${artifactType}/${artifactId}/review`,
    review,
  ),
  getAIArtifactReviews: (artifactType, artifactId) => apiClient.get(
    `/ai-artifacts/${artifactType}/${artifactId}/reviews`,
  ),
}
