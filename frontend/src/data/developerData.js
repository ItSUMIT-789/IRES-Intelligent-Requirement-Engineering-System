export const developerStats = [
  { label: 'Assigned Projects', value: 3, delta: '1 nearing deadline' },
  { label: 'Pending Tasks', value: 11, delta: '4 due this week' },
  { label: 'Completed Tasks', value: 47, delta: '+6 this week' },
  { label: 'Active Requirements', value: 19, delta: '5 high priority' },
]

export const developerProjects = [
  {
    id: 'PRJ-101',
    name: 'Retail Checkout Revamp',
    ba: 'Priya Sharma',
    client: 'Northwind Retail',
    deadline: 'Aug 22, 2026',
    progress: 68,
  },
  {
    id: 'PRJ-104',
    name: 'Vendor Payments Portal',
    ba: 'Meera Nair',
    client: 'Meridian Supply Co.',
    deadline: 'Sep 5, 2026',
    progress: 22,
  },
]

export const requirementDetails = [
  {
    id: 'REQ-142',
    priority: 'High',
    description: 'Guest checkout must not require account creation.',
    userStory: 'US-114',
    acceptanceCriteria: 'Guest can complete payment without registering; email captured for receipt only.',
  },
  {
    id: 'REQ-204',
    priority: 'High',
    description: 'Support multi-currency checkout for international customers.',
    userStory: 'US-130',
    acceptanceCriteria: 'Currency is detected from locale and can be manually overridden at checkout.',
  },
  {
    id: 'REQ-118',
    priority: 'Low',
    description: 'Apply loyalty discount automatically above a spend threshold.',
    userStory: 'US-121',
    acceptanceCriteria: 'Discount applies automatically once cart subtotal passes the configured threshold.',
  },
]

export const taskColumns = [
  { key: 'todo', label: 'To Do' },
  { key: 'in-progress', label: 'In Progress' },
  { key: 'completed', label: 'Completed' },
]

export const tasks = [
  { id: 't1', title: 'Implement guest checkout flow', subtitle: 'REQ-142 · US-114', priority: 'High', meta: 'Due Aug 10', columnKey: 'in-progress' },
  { id: 't2', title: 'Add currency selector component', subtitle: 'REQ-204 · US-130', priority: 'High', meta: 'Due Aug 14', columnKey: 'todo' },
  { id: 't3', title: 'Wire loyalty discount rule engine', subtitle: 'REQ-118 · US-121', priority: 'Low', meta: 'Due Aug 18', columnKey: 'todo' },
  { id: 't4', title: 'Unit tests for payment gateway', subtitle: 'PRJ-104', priority: 'Medium', meta: 'Due Aug 9', columnKey: 'in-progress' },
  { id: 't5', title: 'Fix cart total rounding bug', subtitle: 'PRJ-101', priority: 'Medium', meta: 'Reviewed', columnKey: 'completed' },
  { id: 't6', title: 'Refactor checkout API client', subtitle: 'PRJ-101', priority: 'Low', meta: 'Merged', columnKey: 'completed' },
]

export const milestones = [
  { title: 'Requirements sign-off', date: 'Jul 15, 2026', status: 'done' },
  { title: 'Guest checkout implementation', date: 'Aug 10, 2026', status: 'active' },
  { title: 'Multi-currency support', date: 'Aug 20, 2026', status: 'upcoming' },
  { title: 'UAT with client', date: 'Sep 1, 2026', status: 'upcoming' },
  { title: 'Production release', date: 'Sep 10, 2026', status: 'upcoming' },
]

export const moduleStatus = [
  { name: 'Completed', value: 47, color: '#3b82f6' },
  { name: 'In Progress', value: 24, color: '#8b5cf6' },
  { name: 'Pending', value: 15, color: '#334155' },
]

export const developerNotifications = [
  { type: 'ai', title: 'AI analysis completed', description: 'New requirements ready for REQ-204 implementation.', time: '1 hr ago' },
  { type: 'comment', title: 'Comment from Business Analyst', description: 'Priya Sharma clarified refund window on REQ-188.', time: '4 hr ago' },
  { type: 'approved', title: 'Task reviewed', description: 'Your PR for "Fix cart total rounding bug" was approved.', time: 'Yesterday' },
]

export const developerReports = [
  { name: 'Sprint Velocity — Jul', type: 'PDF', date: 'Aug 1, 2026', size: '640 KB' },
  { name: 'My Task History Export', type: 'CSV', date: 'Jul 27, 2026', size: '48 KB' },
]
