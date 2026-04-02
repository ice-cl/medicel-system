import request from '@/utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function getUserPage(params) {
  return request.get('/user/page', { params })
}

export function createUser(data, roleId) {
  return request.post(`/user?roleId=${roleId}`, data)
}

export function updateUser(data) {
  const roleParam = data.roleId ? `?roleId=${data.roleId}` : ''
  return request.put(`/user${roleParam}`, data)
}

export function deleteUser(id) {
  return request.delete(`/user/${id}`)
}
