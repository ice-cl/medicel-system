import request from '@/utils/request'

export function getHealthDataPage(params) {
  return request.get('/health-data/page', { params })
}

export function getHealthDataTrend(params) {
  return request.get('/health-data/trend', { params })
}

export function createHealthData(data) {
  return request.post('/health-data', data)
}

export function deleteHealthData(id) {
  return request.delete(`/health-data/${id}`)
}
