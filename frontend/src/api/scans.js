import api from './axios'

export const createScan = (data) =>
  api.post('/api/scans', data).then((r) => r.data)

export const getScan = (id) =>
  api.get(`/api/scans/${id}`).then((r) => r.data)

export const getProjectScans = (projectId) =>
  api.get(`/api/scans/project/${projectId}`).then((r) => r.data)

export const getScanIssues = (id) =>
  api.get(`/api/scans/${id}/issues`).then((r) => r.data)

export const getScanMetrics = (id) =>
  api.get(`/api/scans/${id}/metrics`).then((r) => r.data)

export const getScanAIReviews = (id) =>
  api.get(`/api/scans/${id}/ai-reviews`).then((r) => r.data)
