import request from '@/utils/request'

export function analyzeMyHealth() {
  return request.get('/ai-health/me', {
    timeout: 60000
  })
}

export function analyzePatientHealth(patientId) {
  return request.get(`/ai-health/patient/${patientId}`, {
    timeout: 60000
  })
}
