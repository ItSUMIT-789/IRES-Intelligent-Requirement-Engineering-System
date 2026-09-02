import { apiClient, TOKEN_KEY } from './apiClient.js'
import { getRoleLabel, isCanonicalRole } from '../utils/roleRoutes.js'

const OBSOLETE_AUTH_KEYS = ['ires_user', 'ires_users', 'user', 'role', 'currentUser', 'mockUser']

function clearObsoleteAuthState() {
  OBSOLETE_AUTH_KEYS.forEach((key) => localStorage.removeItem(key))
}

function normalizeUser(user) {
  const role = typeof user?.role === 'string' ? user.role.trim().toUpperCase() : ''
  if (!isCanonicalRole(role)) throw new Error(`Authenticated user has an unsupported primary role: ${role || 'missing'}.`)
  const name = user.name || [user.firstName, user.lastName].filter(Boolean).join(' ')

  return {
    id: user.id,
    firstName: user.firstName,
    lastName: user.lastName,
    name,
    email: user.email,
    username: user.username || '',
    active: user.active,
    role,
    roleLabel: getRoleLabel(role),
  }
}

function unwrapUser(response) {
  return normalizeUser(response.data)
}

export const authService = {
  async register({ firstName, lastName, email, password, role }) {
    const response = await apiClient.post('/auth/register', { firstName, lastName, email, password, role })
    return unwrapUser(response)
  },

  async login({ email, password }) {
    const response = await apiClient.post('/auth/login', { email, password })
    return { token: response.data.token, user: normalizeUser(response.data.user) }
  },

  async getCurrentUser() {
    const response = await apiClient.get('/auth/me')
    return unwrapUser(response)
  },

  logout() {
    localStorage.removeItem(TOKEN_KEY)
    clearObsoleteAuthState()
  },

  hasToken() {
    return Boolean(localStorage.getItem(TOKEN_KEY))
  },

  storeToken(token) {
    clearObsoleteAuthState()
    localStorage.setItem(TOKEN_KEY, token)
  },

  clearObsoleteAuthState,
}
