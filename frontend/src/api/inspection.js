import http from './http'

// 送检台账查询：可按器材、状态筛选（参数为空即不过滤）
export const getInspectionList = (params) => http.get('/inspection', { params })
export const createInspection = (data) => http.post('/inspection', data)
export const markInspectionRepaired = (id) => http.put(`/inspection/${id}/repair`)
