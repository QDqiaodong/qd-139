import http from './http'

export const getSessionList = () => http.get('/session')
export const getSessionById = (id) => http.get(`/session/${id}`)
export const createSession = (data) => http.post('/session', data)
export const updateSession = (id, data) => http.put(`/session/${id}`, data)
export const deleteSession = (id) => http.delete(`/session/${id}`)
export const bindEquipments = (sessionId, equipmentIds) => http.post(`/session/${sessionId}/bind`, equipmentIds)