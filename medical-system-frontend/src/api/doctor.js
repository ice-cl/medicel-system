import request from '@/utils/request'

export function getDoctorPage(params) {
  return request.get('/doctor/page', { params })
}

export function getDoctorList(deptId) {
  const params = deptId ? { deptId } : {}
  return request.get('/doctor/list', { params })
}

export function createDoctor(data) {
  return request.post('/doctor', data)
}

export function updateDoctor(data) {
  return request.put('/doctor', data)
}

export function deleteDoctor(id) {
  return request.delete(`/doctor/${id}`)
}

export function getPendingDoctors(params) {
  return request.get('/doctor/pending', { params })
}

export function verifyDoctor(id, status) {
  return request.put(`/doctor/verify/${id}?status=${status}`)
}

export function getDoctorByUserId(userId) {
  return request.get(`/doctor/user/${userId}`)
}
