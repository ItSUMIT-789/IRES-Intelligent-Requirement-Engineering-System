export const analystStats = [
  { label: 'Active Projects', value: 8, delta: '2 kicking off this week' },
  { label: 'Total Requirements', value: 214, delta: '+18 this week' },
  { label: 'Pending Reviews', value: 23, delta: '6 flagged by AI' },
  { label: 'AI Analysis Completed', value: 189, delta: '88% coverage' },
  { label: 'Quality Score', value: '86%', delta: '+3% this week' },
]

export const analystProjects = [
  { id: 'PRJ-101', name: 'Retail Checkout Revamp', client: 'Northwind Retail', requirements: 42, status: 'Active' },
  { id: 'PRJ-102', name: 'Loyalty Points Engine', client: 'Northwind Retail', requirements: 27, status: 'Review' },
  { id: 'PRJ-104', name: 'Vendor Payments Portal', client: 'Meridian Supply Co.', requirements: 31, status: 'Active' },
]

export const requirements = [
  {
    id: 'REQ-204',
    title: 'Support multi-currency checkout',
    project: 'Loyalty Points Engine',
    priority: 'High',
    status: 'Approved',
  },
  {
    id: 'REQ-197',
    title: 'Auto-apply the best available coupon',
    project: 'Retail Checkout Revamp',
    priority: 'Medium',
    status: 'Rejected',
  },
  {
    id: 'REQ-188',
    title: 'Refund window configurable per region',
    project: 'Retail Checkout Revamp',
    priority: 'Medium',
    status: 'Pending',
  },
  {
    id: 'REQ-142',
    title: 'Guest checkout must not require an account',
    project: 'Retail Checkout Revamp',
    priority: 'High',
    status: 'Pending',
  },
  {
    id: 'REQ-118',
    title: 'Apply loyalty discount above a threshold',
    project: 'Loyalty Points Engine',
    priority: 'Low',
    status: 'Pending',
  },
]

export const aiAnalysis = {
  functional: [
    'System shall let a user apply one coupon per order.',
    'System shall calculate loyalty points at checkout completion.',
  ],
  nonFunctional: [
    'Checkout page shall load within 2 seconds on 4G.',
    'Payment data shall be encrypted using TLS 1.3 in transit.',
  ],
  ambiguous: [
    '"Users should be notified appropriately" — channel and timing unspecified.',
    '"The system should be fast" — no measurable threshold.',
  ],
  duplicate: ['REQ-142 overlaps ~90% with REQ-097.'],
  conflicting: ['REQ-118 (points threshold: $50) conflicts with REQ-096 (threshold: $75).'],
  qualityScore: 86,
}

export const userStories = [
  {
    id: 'US-114',
    title: 'As a shopper, I want to check out as a guest so that I can buy without creating an account.',
    acceptanceCriteria: [
      'Given I have items in my cart, when I choose "Checkout as guest", I can complete payment without registering.',
      'My email is captured for the order receipt only.',
    ],
  },
  {
    id: 'US-121',
    title: 'As a shopper, I want the best coupon auto-applied so that I always get the maximum discount.',
    acceptanceCriteria: [
      'Given multiple valid coupons exist, the system applies the one with the highest discount value.',
      'The applied coupon code is visible in the order summary.',
    ],
  },
]

export const traceabilityMatrix = [
  { requirement: 'REQ-204', userStory: 'US-130', testCase: 'TC-041', developer: 'Daniel Osei' },
  { requirement: 'REQ-142', userStory: 'US-114', testCase: 'TC-018', developer: 'Wei Zhang' },
  { requirement: 'REQ-118', userStory: 'US-121', testCase: 'TC-052', developer: 'Daniel Osei' },
]

export const analystNotifications = [
  { type: 'ai', title: 'AI analysis completed', description: 'Ambiguity scan finished for "Vendor Payments Portal".', time: '20 min ago' },
  { type: 'comment', title: 'Client comment received', description: 'Alex Chen replied on REQ-188.', time: '2 hr ago' },
  { type: 'approved', title: 'Requirement approved', description: 'You approved REQ-204.', time: 'Yesterday' },
]

export const analystReports = [
  { name: 'SRS — Retail Checkout Revamp v2', type: 'PDF', date: 'Aug 2, 2026', size: '2.8 MB' },
  { name: 'Traceability Matrix Export', type: 'CSV', date: 'Jul 29, 2026', size: '112 KB' },
]
