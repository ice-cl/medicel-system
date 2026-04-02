import request from '@/utils/request'

export function getSchedulePage(params) {
  return request.get('/schedule/page', { params })
}

export function getAvailableSchedules(params) {
  return request.get('/schedule/available', { params })
}

export function createSchedule(data) {
  return request.post('/schedule', data)
}

export function batchCreateSchedule(data) {
  return request.post('/schedule/batch', data)
}

export function updateSchedule(data) {
  return request.put('/schedule', data)
}

export function deleteSchedule(id) {
  return request.delete(`/schedule/${id}`)
}
