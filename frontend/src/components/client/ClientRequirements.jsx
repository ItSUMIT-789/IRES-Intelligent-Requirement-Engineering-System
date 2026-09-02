import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import GlassCard from '../GlassCard.jsx'
import { projectService } from '../../services/projectService.js'
import { requirementService } from '../../services/requirementService.js'
import { getRequirementStatusLabel, REQUIREMENT_STATUS_LABELS } from '../../config/requirementWorkflow.js'
import { ClientHeader } from '../../pages/dashboards/ClientDashboard.jsx'
import { date, Empty, Error, Status } from './ClientProjects.jsx'

export default function ClientRequirements() {
  const [requirements, setRequirements] = useState([]); const [projects, setProjects] = useState([]); const [filters, setFilters] = useState({ projectId: '', status: '', priority: '' }); const [error, setError] = useState('')
  const load = () => { const query = { size: 100, sort: 'createdAt,desc', ...Object.fromEntries(Object.entries(filters).filter(([, v]) => v)) }; requirementService.listAccessible(query).then((r) => setRequirements(r.data.content || [])).catch((e) => setError(e.message)) }
  useEffect(() => { projectService.getProjects({ size: 100 }).then((r) => setProjects(r.data.content || [])).catch((e) => setError(e.message)) }, [])
  useEffect(load, [filters.projectId, filters.status, filters.priority])
  return <><ClientHeader title="My Requirements" subtitle="Requirements from projects accessible to your account." action={<Link className="btn-gradient !px-4 !py-2.5" to="/client/requirements/new">New Requirement</Link>} />
    <GlassCard hover={false} className="mb-5 p-4"><div className="grid gap-3 sm:grid-cols-3"><Filter value={filters.projectId} onChange={(projectId) => setFilters({ ...filters, projectId })}><option value="">All projects</option>{projects.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}</Filter><Filter value={filters.status} onChange={(status) => setFilters({ ...filters, status })}><option value="">All statuses</option>{Object.entries(REQUIREMENT_STATUS_LABELS).map(([v, l]) => <option key={v} value={v}>{l}</option>)}</Filter><Filter value={filters.priority} onChange={(priority) => setFilters({ ...filters, priority })}><option value="">All priorities</option>{['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].map((v) => <option key={v}>{v}</option>)}</Filter></div></GlassCard>
    {error && <Error message={error} />}{!error && requirements.length === 0 && <Empty message="No requirements match these filters." />}
    <div className="overflow-x-auto rounded-2xl border border-white/10"><table className="w-full min-w-[820px] text-left text-sm"><thead className="bg-white/[0.06] text-xs uppercase tracking-wide text-slate-400"><tr>{['Requirement', 'Project', 'Priority', 'Status', 'Created', 'Updated', 'Action'].map((h) => <th key={h} className="px-4 py-3">{h}</th>)}</tr></thead><tbody>{requirements.map((r) => <tr key={r.id} className="border-t border-white/10 text-slate-300"><td className="px-4 py-4 font-medium text-white">{r.title}</td><td className="px-4 py-4">{r.projectName}</td><td className="px-4 py-4">{r.priority}</td><td className="px-4 py-4"><Status value={getRequirementStatusLabel(r.status)} /></td><td className="px-4 py-4">{date(r.createdAt)}</td><td className="px-4 py-4">{date(r.updatedAt)}</td><td className="px-4 py-4"><Link className="text-blue-300" to={`/client/requirements/${r.id}`}>View</Link></td></tr>)}</tbody></table></div></>
}
function Filter({ value, onChange, children }) { return <select value={value} onChange={(e) => onChange(e.target.value)} className="rounded-xl border border-white/10 bg-space-900 px-3 py-2.5 text-sm">{children}</select> }
