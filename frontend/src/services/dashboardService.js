import { apiClient } from './apiClient.js'

export const dashboardService = { getSummary: () => apiClient.get('/dashboard') }
