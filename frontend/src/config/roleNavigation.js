import { Activity, Bell, BookOpenText, BrainCircuit, Bug, ClipboardCheck, FileOutput, FileText, FolderKanban, GitBranch, LayoutDashboard, ListChecks, Network, UserCircle, Users } from 'lucide-react'

export const ROLE_NAVIGATION = {
  ADMIN: [['overview', 'Dashboard', LayoutDashboard], ['users', 'Users', Users], ['projects', 'Projects', FolderKanban], ['requirements', 'Requirements', FileText], ['workflow', 'Workflow / Assignments', Network], ['tasks', 'Developer Tasks', ListChecks], ['testing', 'Final Approval', ClipboardCheck], ['bugs', 'Bugs', Bug], ['reports', 'Reports', FileOutput], ['notifications', 'Notifications', Bell], ['profile', 'Profile', UserCircle]],
  CLIENT: [['overview', 'Dashboard', LayoutDashboard], ['projects', 'My Projects', FolderKanban], ['submit', 'Submit Requirement', FileText], ['requirements', 'My Requirements', ListChecks], ['notifications', 'Notifications', Bell], ['reports', 'Reports', FileOutput], ['profile', 'Profile', UserCircle]],
  BUSINESS_ANALYST: [['overview', 'Dashboard', LayoutDashboard], ['queue', 'Requirement Queue', ListChecks], ['analysis', 'Requirement Analysis', Activity], ['ai', 'AI Suggestions', BrainCircuit], ['criteria', 'Acceptance Criteria', ClipboardCheck], ['stories', 'User Stories', BookOpenText], ['traceability', 'Traceability', GitBranch], ['reports', 'Reports', FileOutput], ['notifications', 'Notifications', Bell], ['profile', 'Profile', UserCircle]],
  DEVELOPER: [['overview', 'Dashboard', LayoutDashboard], ['requirements', 'Assigned Requirements', FileText], ['tasks', 'Developer Tasks', ListChecks], ['testCases', 'Test Cases', ClipboardCheck], ['testerAssignment', 'Tester Assignment', Users], ['bugs', 'Bugs', Bug], ['notifications', 'Notifications', Bell], ['profile', 'Profile', UserCircle]],
  TESTER: [['overview', 'Dashboard', LayoutDashboard], ['testCases', 'My Test Cases', ClipboardCheck], ['executions', 'Test Executions', Activity], ['bugs', 'Bugs', Bug], ['notifications', 'Notifications', Bell], ['profile', 'Profile', UserCircle]],
}

export function getRoleNavigation(role) {
  return (ROLE_NAVIGATION[role] || []).map(([key, label, icon]) => ({ key, label, icon }))
}
