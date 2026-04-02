import request from '@/utils/request'

export function getDepartmentPage(params) {
  return request.get('/department/page', { params })
}

export function getDepartmentList() {
  return request.get('/department/list')
}

export function createDepartment(data) {
  return request.post('/department', data)
}

export function updateDepartment(data) {
  return request.put('/department', data)
}

export function deleteDepartment(id) {
  return request.delete(`/department/${id}`)
}
