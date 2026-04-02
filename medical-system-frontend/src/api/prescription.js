import request from '@/utils/request'

export function getPrescriptionPage(params) {
  return request.get('/prescription/page', { params })
}

export function getPrescriptionsByRecord(recordId) {
  return request.get(`/prescription/record/${recordId}`)
}

export function createPrescription(data) {
  return request.post('/prescription', data)
}

export function batchCreatePrescription(data) {
  return request.post('/prescription/batch', data)
}

export function updatePrescription(data) {
  return request.put('/prescription', data)
}

export function deletePrescription(id) {
  return request.delete(`/prescription/${id}`)
}
