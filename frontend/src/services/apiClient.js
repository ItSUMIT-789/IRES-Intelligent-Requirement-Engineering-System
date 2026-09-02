const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'
const TOKEN_KEY = 'ires_access_token'

async function request(path, options = {}) {
  const headers = new Headers(options.headers)
  headers.set('Accept', 'application/json')

  const token = localStorage.getItem(TOKEN_KEY)
  if (token) headers.set('Authorization', `Bearer ${token}`)

  if (options.body && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }

  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers })
  const contentType = response.headers.get('content-type') || ''
  const payload = contentType.includes('application/json') ? await response.json() : null

  if (!response.ok) {
    const error = new Error(payload?.message || `Request failed with status ${response.status}.`)
    error.status = response.status
    error.data = payload
    throw error
  }

  return payload
}

async function download(path) {
  const headers = new Headers()
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) headers.set('Authorization', `Bearer ${token}`)
  const response = await fetch(`${API_BASE_URL}${path}`, { headers })
  if (!response.ok) throw new Error(`Download failed with status ${response.status}.`)
  return response.blob()
}

export const apiClient = {
  get: (path) => request(path),
  post: (path, body) => request(path, { method: 'POST', body: JSON.stringify(body) }),
  postForm: (path, body) => request(path, { method: 'POST', body }),
  put: (path, body) => request(path, { method: 'PUT', body: JSON.stringify(body) }),
  delete: (path) => request(path, { method: 'DELETE' }),
  download,
}

export { API_BASE_URL, TOKEN_KEY }
