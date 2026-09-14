import http from './http'

export const getNotificationList = () => http.get('/notification')
export const getUnreadCount = () => http.get('/notification/unread-count')
export const markNotificationRead = (id) => http.put(`/notification/${id}/read`)
