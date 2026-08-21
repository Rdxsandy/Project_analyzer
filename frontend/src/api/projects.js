import api from './axios'

export const getProjects = () =>
  api.get('/api/projects').then((r) => r.data)

export const getProject = (id) =>
  api.get(`/api/projects/${id}`).then((r) => r.data)

export const createProject = (data) =>
  api.post('/api/projects', data).then((r) => r.data)

export const updateProject = (id, data) =>
  api.put(`/api/projects/${id}`, data).then((r) => r.data)

export const deleteProject = (id) =>
  api.delete(`/api/projects/${id}`).then((r) => r.data)
