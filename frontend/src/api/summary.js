import http from './http'

export const getSummaryByAllAgeGroups = () => http.get('/summary')
export const getSummaryByAgeGroup = (ageGroup) => http.get(`/summary/${ageGroup}`)