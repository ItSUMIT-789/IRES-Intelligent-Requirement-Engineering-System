import { apiClient } from './apiClient.js'

export const projectService = {
  createProject: (project) => apiClient.post('/projects', project),
  getProjects: (params = {}) => {
    const query = new URLSearchParams(params).toString()
    return apiClient.get(`/projects${query ? `?${query}` : ''}`)
  },
  getProject: (id) => apiClient.get(`/projects/${id}`),
  updateProject: (id, project) => apiClient.put(`/projects/${id}`, project),
  deleteProject: (id) => apiClient.delete(`/projects/${id}`),
  addProjectMember: (projectId, member) => apiClient.post(`/projects/${projectId}/members`, member),
  getProjectMembers: (projectId) => apiClient.get(`/projects/${projectId}/members`),
  removeProjectMember: (projectId, userId) => apiClient.delete(`/projects/${projectId}/members/${userId}`),
}