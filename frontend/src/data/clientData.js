export const clientStats = [
  { label: 'Total Projects', value: 6, delta: '2 active this sprint' },
  { label: 'Pending Requirements', value: 9, delta: 'Awaiting BA review' },
  { label: 'Approved Requirements', value: 34, delta: '+5 this week' },
  { label: 'AI Suggestions Available', value: 7, delta: '3 marked high priority' },
]

export const clientProjects = [
  {
    id: 'PRJ-101',
    name: 'Retail Checkout Revamp',
    status: 'In Progress',
    ba: 'Priya Sharma',
    progress: 68,
    updated: '2 days ago',
  },
  {
    id: 'PRJ-102',
    name: 'Loyalty Points Engine',
    status: 'Under Review',
    ba: 'Farhan Ali',
    progress: 40,
    updated: '5 hours ago',
  },
  {
    id: 'PRJ-103',
    name: 'Mobile Onboarding Flow',
    status: 'Completed',
    ba: 'Priya Sharma',
    progress: 100,
    updated: '1 week ago',
  },
  {
    id: 'PRJ-104',
    name: 'Vendor Payments Portal',
    status: 'In Progress',
    ba: 'Meera Nair',
    progress: 22,
    updated: 'Yesterday',
  },
]

export const requirementCategories = ['Functional', 'Non-Functional', 'Business Rule', 'UI/UX', 'Security']
export const clientProjectOptions = clientProjects.map((p) => p.name)

export const aiSuggestions = [
  {
    type: 'improvement',
    title: 'Improved requirement wording',
    original: '"The system should be fast."',
    suggestion:
      '"The system shall return search results within 2 seconds for 95% of requests under normal load."',
  },
  {
    type: 'ambiguity',
    title: 'Ambiguity warning',
    description: '"Users should be notified appropriately" does not specify channel, timing, or condition.',
  },
  {
    type: 'missing',
    title: 'Missing information',
    description: 'REQ-118 references a "discount threshold" without defining its value or currency.',
  },
  {
    type: 'duplicate',
    title: 'Duplicate requirement warning',
    description: 'REQ-142 overlaps ~90% with REQ-097 ("Guest checkout must not require an account").',
  },
]

export const requirementQualityScore = 82

export const clientNotifications = [
  {
    type: 'approved',
    title: 'Requirement approved',
    description: 'REQ-204 "Multi-currency support" was approved by Priya Sharma.',
    time: '10 min ago',
  },
  {
    type: 'rejected',
    title: 'Requirement rejected',
    description: 'REQ-197 "Auto-apply best coupon" needs more detail before resubmission.',
    time: '1 hr ago',
  },
  {
    type: 'comment',
    title: 'New comment from Business Analyst',
    description: 'Farhan Ali commented on REQ-188: "Can you clarify the refund window?"',
    time: '3 hr ago',
  },
  {
    type: 'ai',
    title: 'AI analysis completed',
    description: 'Ambiguity and duplication scan finished for "Loyalty Points Engine".',
    time: 'Yesterday',
  },
]

export const clientReports = [
  { name: 'Requirement Summary — Q3', type: 'PDF', date: 'Aug 1, 2026', size: '1.2 MB' },
  { name: 'Loyalty Points Engine — SRS', type: 'PDF', date: 'Jul 26, 2026', size: '3.4 MB' },
  { name: 'Approved Requirements Export', type: 'CSV', date: 'Jul 20, 2026', size: '84 KB' },
]
