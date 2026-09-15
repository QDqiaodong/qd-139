import http from './http'

// 体验感受名单查询：可按场次、登记日期筛选（参数为空即不过滤）
export const getFeedbackList = (params) => http.get('/feedback', { params })
export const createFeedback = (data) => http.post('/feedback', data)
