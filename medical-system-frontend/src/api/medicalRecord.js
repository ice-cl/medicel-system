import request from '@/utils/request'

export function getMedicalRecordPage(params) {
  return request.get('/medical-record/page', { params })
}

export function getMedicalRecordById(id) {
  return request.get(`/medical-record/${id}`)
}

export function createMedicalRecord(data) {
  return request.post('/medical-record', data)
}

export function updateMedicalRecord(data) {
  return request.put('/medical-record', data)
}
