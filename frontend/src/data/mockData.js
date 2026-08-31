// Static mock data used purely to give the dashboard something to render.
// Replace with real API data once a backend is connected.

export const roleDistribution = [
  { role: 'Admin', count: 3, color: '#22d3ee' },
  { role: 'Business Analyst', count: 18, color: '#3b82f6' },
  { role: 'Client', count: 42, color: '#6366f1' },
  { role: 'Developer', count: 27, color: '#8b5cf6' },
  { role: 'Tester', count: 15, color: '#a855f7' },
]

export const totalUsers = roleDistribution.reduce((sum, r) => sum + r.count, 0)
export const totalRoles = roleDistribution.length

export const adminStats = [
  { label: 'Total Users', value: totalUsers, delta: '+8 this week' },
  { label: 'Total Roles', value: totalRoles, delta: 'Admin, BA, Client, Dev, Tester' },
  { label: 'Requirements Analyzed', value: 1284, delta: '+64 this week' },
  { label: 'Ambiguities Flagged', value: 96, delta: '12 unresolved' },
  { label: 'User Stories Generated', value: 512, delta: '+31 this week' },
  { label: 'SRS Reports Generated', value: 74, delta: '+3 this week' },
]

export const memberStats = [
  { label: 'My Requirements', value: 24, delta: '4 pending review' },
  { label: 'User Stories Assigned', value: 12, delta: '3 due this sprint' },
  { label: 'Ambiguities to Resolve', value: 5, delta: '2 high priority' },
  { label: 'Traceability Coverage', value: '92%', delta: '+4% this week' },
]

export const recentActivity = [
  {
    actor: 'Priya Sharma',
    role: 'Business Analyst',
    action: 'uploaded Requirement_Checkout_v3.docx',
    time: '12 min ago',
  },
  {
    actor: 'IRES AI',
    role: 'System',
    action: 'flagged 3 ambiguous statements in "Checkout Flow"',
    time: '38 min ago',
  },
  {
    actor: 'Daniel Osei',
    role: 'Developer',
    action: 'marked user story US-114 as Done',
    time: '1 hr ago',
  },
  {
    actor: 'IRES AI',
    role: 'System',
    action: 'generated SRS report "Payments Module v2"',
    time: '2 hr ago',
  },
  {
    actor: 'Meera Nair',
    role: 'Tester',
    action: 'linked 6 test cases to REQ-208',
    time: '3 hr ago',
  },
  {
    actor: 'Alex Chen',
    role: 'Client',
    action: 'approved requirement "Multi-currency support"',
    time: '5 hr ago',
  },
]
