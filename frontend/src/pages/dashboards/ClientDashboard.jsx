import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import DashboardLayout from '../../layouts/DashboardLayout.jsx'
import { getRoleNavigation } from '../../config/roleNavigation.js'
import ClientOverview from '../../components/client/ClientOverview.jsx'
import ClientProjects from '../../components/client/ClientProjects.jsx'
import ClientProjectDetail from '../../components/client/ClientProjectDetail.jsx'
import ClientRequirementForm from '../../components/client/ClientRequirementForm.jsx'
import ClientRequirements from '../../components/client/ClientRequirements.jsx'
import ClientRequirementDetail from '../../components/client/ClientRequirementDetail.jsx'
import ProfilePanel from '../../components/dashboard/ProfilePanel.jsx'
import ClientPlaceholder from '../../components/client/ClientPlaceholder.jsx'
import NotificationPage from '../../components/dashboard/NotificationPage.jsx'

const paths = { overview: '/client/dashboard', projects: '/client/projects', submit: '/client/requirements/new', requirements: '/client/requirements', notifications: '/client/notifications', reports: '/client/reports', profile: '/client/profile' }

export default function ClientDashboard() {
  const navigate = useNavigate(); const location = useLocation(); const items = getRoleNavigation('CLIENT')
  const active = location.pathname.includes('/requirements/new') ? 'submit' : location.pathname.startsWith('/client/requirements') ? 'requirements' : location.pathname.startsWith('/client/projects') ? 'projects' : Object.entries(paths).find(([, path]) => path === location.pathname)?.[0] || 'overview'
  return <DashboardLayout items={items} active={active} onSelect={(key) => navigate(paths[key])}><Routes>
    <Route path="dashboard" element={<ClientOverview />} /><Route path="projects" element={<ClientProjects />} />
    <Route path="projects/:id" element={<ClientProjectDetail />} /><Route path="requirements/new" element={<ClientRequirementForm />} />
    <Route path="requirements" element={<ClientRequirements />} /><Route path="requirements/:id" element={<ClientRequirementDetail />} />
    <Route path="notifications" element={<NotificationPage />} />
    <Route path="reports" element={<ClientPlaceholder title="Reports" message="Client reports will appear here when the reporting backend is enabled." />} />
    <Route path="profile" element={<><ClientHeader title="Profile" subtitle="Your authenticated account information." /><ProfilePanel /></>} />
    <Route path="*" element={<Navigate to="dashboard" replace />} />
  </Routes></DashboardLayout>
}

export function ClientHeader({ title, subtitle, action }) {
  return <div className="mb-8 flex flex-wrap items-end justify-between gap-4"><div><span className="text-xs font-semibold uppercase tracking-widest text-blue-300">Client</span><h1 className="font-display text-2xl font-bold text-white sm:text-3xl">{title}</h1>{subtitle && <p className="mt-1 text-sm text-slate-400">{subtitle}</p>}</div>{action}</div>
}
