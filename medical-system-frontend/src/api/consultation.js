import request from '@/utils/request'

export function getConsultationPage(params) {
  return request.get('/consultation/page', { params })
}

export function getConsultationById(id) {
  return request.get(`/consultation/${id}`)
}

export function createConsultation(data) {
  return request.post('/consultation', data)
}

export function endConsultation(id) {
  return request.put(`/consultation/end/${id}`)
}

export function getMessagesByConsultation(consultationId) {
  return request.get(`/consult-message/consultation/${consultationId}`)
}

export function sendMessage(data) {
  return request.post('/consult-message', data)
}
