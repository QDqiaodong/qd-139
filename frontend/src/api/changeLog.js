import http from './http'

export const changeAgeGroup = (data) => http.post('/change-log', data)
export const getLogsBySession = (sessionId) => http.get(`/change-log/session/${sessionId}`)
export const getLogsByEquipment = (equipmentId) => http.get(`/change-log/equipment/${equipmentId}`)
export const getLogsByAgeGroup = (ageGroup) => http.get(`/change-log/age-group/${ageGroup}`)