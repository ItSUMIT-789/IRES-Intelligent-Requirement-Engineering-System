export const testerStats = [
  { label: 'Assigned Projects', value: 3, delta: '1 in final QA' },
  { label: 'Pending Testing', value: 14, delta: '5 blocked on build' },
  { label: 'Passed Tests', value: 132, delta: '+9 this week' },
  { label: 'Failed Tests', value: 18, delta: '4 critical' },
  { label: 'Reported Bugs', value: 22, delta: '6 open' },
]

export const testerProjects = [
  { id: 'PRJ-101', name: 'Retail Checkout Revamp', developer: 'Daniel Osei', requirementCount: 42, deadline: 'Aug 22, 2026' },
  { id: 'PRJ-104', name: 'Vendor Payments Portal', developer: 'Wei Zhang', requirementCount: 31, deadline: 'Sep 5, 2026' },
]

export const testCases = [
  { id: 'TC-018', requirement: 'REQ-142', title: 'Guest can complete checkout without an account', status: 'Passed' },
  { id: 'TC-041', requirement: 'REQ-204', title: 'Currency switches correctly based on locale', status: 'Passed' },
  { id: 'TC-052', requirement: 'REQ-118', title: 'Loyalty discount applies above threshold', status: 'Failed' },
  { id: 'TC-063', requirement: 'REQ-142', title: 'Guest email is captured for receipt only', status: 'Pending' },
]

export const testingColumns = [
  { key: 'pending', label: 'Pending' },
  { key: 'testing', label: 'Testing' },
  { key: 'passed', label: 'Passed' },
  { key: 'failed', label: 'Failed' },
]

export const testingItems = [
  { id: 'x1', title: 'TC-018 Guest checkout', subtitle: 'REQ-142', priority: 'High', meta: 'PRJ-101', columnKey: 'passed' },
  { id: 'x2', title: 'TC-041 Currency switch', subtitle: 'REQ-204', priority: 'High', meta: 'PRJ-101', columnKey: 'passed' },
  { id: 'x3', title: 'TC-052 Loyalty threshold', subtitle: 'REQ-118', priority: 'Medium', meta: 'PRJ-104', columnKey: 'failed' },
  { id: 'x4', title: 'TC-063 Guest email capture', subtitle: 'REQ-142', priority: 'Low', meta: 'PRJ-101', columnKey: 'pending' },
  { id: 'x5', title: 'TC-071 Vendor payout retry', subtitle: 'REQ-211', priority: 'Critical', meta: 'PRJ-104', columnKey: 'testing' },
]

export const bugs = [
  { id: 'BUG-201', title: 'Discount not applied above $75 threshold', severity: 'High', status: 'Open', developer: 'Daniel Osei' },
  { id: 'BUG-198', title: 'Currency symbol missing on order summary', severity: 'Medium', status: 'In Review', developer: 'Wei Zhang' },
  { id: 'BUG-192', title: 'Guest checkout email validation too strict', severity: 'Low', status: 'Resolved', developer: 'Daniel Osei' },
]

export const testingAnalytics = {
  passVsFail: [
    { name: 'Passed', value: 132, color: '#3b82f6' },
    { name: 'Failed', value: 18, color: '#f87171' },
  ],
  bugSeverity: [
    { name: 'Low', value: 8, color: '#22d3ee' },
    { name: 'Medium', value: 9, color: '#a855f7' },
    { name: 'High', value: 4, color: '#f97316' },
    { name: 'Critical', value: 1, color: '#ef4444' },
  ],
}

export const testerNotifications = [
  { type: 'ai', title: 'AI analysis completed', description: 'Test coverage report generated for "Vendor Payments Portal".', time: '45 min ago' },
  { type: 'comment', title: 'Developer replied', description: 'Daniel Osei commented on BUG-201.', time: '3 hr ago' },
  { type: 'approved', title: 'Bug resolved', description: 'BUG-192 was marked resolved and verified.', time: 'Yesterday' },
]

export const testerReports = [
  { name: 'QA Summary — Retail Checkout Revamp', type: 'PDF', date: 'Aug 2, 2026', size: '1.6 MB' },
  { name: 'Bug Log Export', type: 'CSV', date: 'Jul 30, 2026', size: '56 KB' },
]
