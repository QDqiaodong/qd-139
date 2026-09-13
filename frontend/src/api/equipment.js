import http from './http'

export const getEquipmentList = (params) => http.get('/equipment', { params })
export const getEquipmentById = (id) => http.get(`/equipment/${id}`)
export const createEquipment = (data) => http.post('/equipment', data)
export const updateEquipment = (id, data) => http.put(`/equipment/${id}`, data)
export const deleteEquipment = (id) => http.delete(`/equipment/${id}`)