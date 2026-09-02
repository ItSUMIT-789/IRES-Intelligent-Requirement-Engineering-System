export const REQUIREMENT_STATUS_LABELS = {
  DRAFT: 'Draft', SUBMITTED: 'Submitted', IN_ANALYSIS: 'In Analysis', NEEDS_CLARIFICATION: 'Needs Clarification',
  ANALYSIS_COMPLETED: 'Analysis Completed', WAITING_FOR_ADMIN_ASSIGNMENT: 'Waiting for Admin Assignment',
  ASSIGNED_TO_DEVELOPER: 'Assigned to Developer', IN_DEVELOPMENT: 'In Development',
  READY_FOR_TESTING: 'Ready for Testing', ASSIGNED_TO_TESTER: 'Assigned to Tester', IN_TESTING: 'In Testing',
  TEST_FAILED: 'Test Failed', TEST_PASSED: 'Test Passed', WAITING_FOR_ADMIN_APPROVAL: 'Waiting for Admin Approval', COMPLETED: 'Completed',
}

export const WORKFLOW_ACTIONS = {
  CLIENT: { DRAFT: [['submit', 'Submit']], NEEDS_CLARIFICATION: [['respond-clarification', 'Respond to Clarification']] },
  BUSINESS_ANALYST: { SUBMITTED: [['start-analysis', 'Start Analysis']], NEEDS_CLARIFICATION: [['start-analysis', 'Mark Clarified / Resume Analysis']], IN_ANALYSIS: [['request-clarification', 'Request Clarification'], ['analysis-complete', 'Complete Analysis']], ANALYSIS_COMPLETED: [['send-to-admin', 'Send to Admin']] },
  ADMIN: { WAITING_FOR_ADMIN_ASSIGNMENT: [['assign-developer', 'Assign Developer']], WAITING_FOR_ADMIN_APPROVAL: [['complete', 'Complete Requirement']] },
  DEVELOPER: { ASSIGNED_TO_DEVELOPER: [['start-development', 'Start Development']], IN_DEVELOPMENT: [['ready-for-testing', 'Mark Ready for Testing']], READY_FOR_TESTING: [['assign-tester', 'Assign Tester']], TEST_FAILED: [['start-development', 'Start Rework']] },
  TESTER: { ASSIGNED_TO_TESTER: [['start-testing', 'Start Testing']], IN_TESTING: [['pass', 'Mark Passed'], ['fail', 'Mark Failed']], TEST_PASSED: [['send-for-approval', 'Send for Admin Approval']] },
}

export const getWorkflowActions = (role, status) => WORKFLOW_ACTIONS[role]?.[status] || []
export const getRequirementStatusLabel = (status) => REQUIREMENT_STATUS_LABELS[status] || status
