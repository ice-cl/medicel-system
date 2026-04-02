import request from '@/utils/request'

export function getRegistrationPage(params) {
  return request.get('/registration/page', { params })
}

export function createRegistration(data) {
  return request.post('/registration', data)
}

export function cancelRegistration(id) {
  return request.put(`/registration/cancel/${id}`)
}

export function updateRegistrationStatus(id, status) {
  return request.put(`/registration/status/${id}?status=${status}`)
}
