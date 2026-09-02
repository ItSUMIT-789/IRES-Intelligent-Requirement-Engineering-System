import { apiClient } from './apiClient.js'
export const deliveryUserService={eligibleTesters:(projectId)=>apiClient.get(`/users/eligible-testers/${projectId}`),eligibleBusinessAnalysts:(projectId)=>apiClient.get(`/users/eligible-business-analysts/${projectId}`)}
