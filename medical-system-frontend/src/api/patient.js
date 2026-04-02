import request from '@/utils/request'

export function getPatientPage(params) {
  return request.get('/patient/page', { params })
}

export function getPatientList() {
  return request.get('/patient/list')
}

export function getPatientById(id) {
  return request.get(`/patient/${id}`)
}

export function getPatientByUserId(userId) {
  return request.get(`/patient/user/${userId}`)
}

export function createPatient(data) {
  return request.post('/patient', data)
}

export function updatePatient(data) {
  return request.put('/patient', data)
}
