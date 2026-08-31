export const REQUIREMENT_STATUS_LABELS = {
  DRAFT: 'Draft', SUBMITTED: 'Submitted', IN_ANALYSIS: 'In Analysis', NEEDS_CLARIFICATION: 'Needs Clarification',
  APPROVED_FOR_DEVELOPMENT: 'Approved for Development', IN_DEVELOPMENT: 'In Development',
  READY_FOR_TESTING: 'Ready for Testing', IN_TESTING: 'In Testing', FAILED: 'Failed', PASSED: 'Passed', COMPLETED: 'Completed', REJECTED: 'Rejected',
}

export const WORKFLOW_ACTIONS = {
  CLIENT: { DRAFT: [['submit', 'Submit']], NEEDS_CLARIFICATION: [['respond-clarification', 'Respond to Clarification']] },
  BUSINESS_ANALYST: { SUBMITTED: [['start-analysis', 'Start Analysis']], IN_ANALYSIS: [['request-clarification', 'Request Clarification'], ['approve', 'Approve for Development']] },
  DEVELOPER: { APPROVED_FOR_DEVELOPMENT: [['start-development', 'Start Development']], IN_DEVELOPMENT: [['ready-for-testing', 'Mark Ready for Testing']], FAILED: [['start-development', 'Start Rework']] },
  TESTER: { READY_FOR_TESTING: [['start-testing', 'Start Testing']], IN_TESTING: [['pass', 'Mark Passed'], ['fail', 'Mark Failed']], PASSED: [['complete', 'Complete']] },
}

export const getWorkflowActions = (role, status) => WORKFLOW_ACTIONS[role]?.[status] || []
export const getRequirementStatusLabel = (status) => REQUIREMENT_STATUS_LABELS[status] || status
