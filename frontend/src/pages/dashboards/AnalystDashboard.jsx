import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import DashboardLayout from '../../layouts/DashboardLayout.jsx'
import { getRoleNavigation } from '../../config/roleNavigation.js'
import AnalystOverview from '../../components/analyst/AnalystOverview.jsx'
import AnalystRequirements from '../../components/analyst/AnalystRequirements.jsx'
import AnalystWorkspace from '../../components/analyst/AnalystWorkspace.jsx'
import ProfilePanel from '../../components/dashboard/ProfilePanel.jsx'
import GlassCard from '../../components/GlassCard.jsx'
import NotificationPage from '../../components/dashboard/NotificationPage.jsx'

const paths = { overview: '/analyst/dashboard', queue: '/analyst/requirements', analysis: '/analyst/analysis', ai: '/analyst/ai', criteria: '/analyst/acceptance-criteria', stories: '/analyst/user-stories', traceability: '/analyst/traceability', reports: '/analyst/reports', notifications: '/analyst/notifications', profile: '/analyst/profile' }
export default function AnalystDashboard() {
  const navigate = useNavigate(); const location = useLocation(); const items = getRoleNavigation('BUSINESS_ANALYST')
  const active = location.pathname.startsWith('/analyst/requirements/') ? 'analysis' : Object.entries(paths).find(([, value]) => value === location.pathname)?.[0] || 'overview'
  return <DashboardLayout items={items} active={active} onSelect={(key) => navigate(paths[key])}><Routes>
    <Route path="dashboard" element={<AnalystOverview />} /><Route path="requirements" element={<AnalystRequirements mode="queue" />} />
    <Route path="analysis" element={<AnalystRequirements mode="analysis" />} /><Route path="ai" element={<AnalystRequirements mode="ai" />} />
    <Route path="acceptance-criteria" element={<AnalystRequirements mode="criteria" />} /><Route path="user-stories" element={<AnalystRequirements mode="stories" />} />
    <Route path="traceability" element={<AnalystRequirements mode="traceability" />} /><Route path="requirements/:id" element={<AnalystWorkspace />} />
    <Route path="reports" element={<AnalystPlaceholder title="Reports" message="Analyst reports will appear here when the reporting backend is enabled." />} />
    <Route path="notifications" element={<NotificationPage />} />
    <Route path="profile" element={<><AnalystHeader title="Profile" subtitle="Your authenticated analyst account." /><ProfilePanel /></>} />
    <Route path="*" element={<Navigate to="dashboard" replace />} />
  </Routes></DashboardLayout>
}
export function AnalystHeader({ title, subtitle, action }) { return <div className="mb-8 flex flex-wrap items-end justify-between gap-4"><div><span className="text-xs font-semibold uppercase tracking-widest text-purple-300">Business Analyst</span><h1 className="text-2xl font-bold text-white sm:text-3xl">{title}</h1>{subtitle && <p className="mt-1 text-sm text-slate-400">{subtitle}</p>}</div>{action}</div> }
function AnalystPlaceholder({ title, message }) { return <><AnalystHeader title={title} /><GlassCard hover={false} className="p-6"><p className="text-slate-400">{message}</p></GlassCard></> }
