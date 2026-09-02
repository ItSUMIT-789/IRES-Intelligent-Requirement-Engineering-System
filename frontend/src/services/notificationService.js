import { apiClient } from './apiClient.js'

export const notificationService = {
  list: (params = {}) => {
    const query = new URLSearchParams(params).toString()
    return apiClient.get(`/notifications${query ? `?${query}` : ''}`)
  },
  unreadCount: () => apiClient.get('/notifications/unread-count'),
  markRead: (id) => apiClient.post(`/notifications/${id}/read`, {}),
  markAllRead: () => apiClient.post('/notifications/read-all', {}),
}
