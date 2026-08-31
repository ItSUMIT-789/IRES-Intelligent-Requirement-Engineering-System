export const ROLE_CONFIG = Object.freeze({
  ADMIN: Object.freeze({ label: 'Admin', dashboard: '/admin/dashboard', publicRegistration: false }),
  BUSINESS_ANALYST: Object.freeze({ label: 'Business Analyst', dashboard: '/analyst/dashboard', publicRegistration: true }),
  CLIENT: Object.freeze({ label: 'Client', dashboard: '/client/dashboard', publicRegistration: true }),
  DEVELOPER: Object.freeze({ label: 'Developer', dashboard: '/developer/dashboard', publicRegistration: true }),
  TESTER: Object.freeze({ label: 'Tester', dashboard: '/tester/dashboard', publicRegistration: true }),
})

export const ROLE_LABELS = Object.freeze(Object.fromEntries(
  Object.entries(ROLE_CONFIG).map(([role, config]) => [role, config.label]),
))

export const roleRoutes = Object.freeze(Object.fromEntries(
  Object.entries(ROLE_CONFIG).map(([role, config]) => [role, config.dashboard]),
))

export function isCanonicalRole(role) {
  return Object.hasOwn(ROLE_CONFIG, role)
}

export function getRoleLabel(role) {
  return ROLE_CONFIG[role]?.label || ''
}

export function getDashboardRoute(role) {
  const route = ROLE_CONFIG[role]?.dashboard
  if (!route) throw new Error(`Unsupported account role: ${role || 'missing'}.`)
  return route
}

export const registrationRoles = Object.entries(ROLE_CONFIG)
  .filter(([, config]) => config.publicRegistration)
  .map(([value, config]) => ({ label: config.label, value }))
