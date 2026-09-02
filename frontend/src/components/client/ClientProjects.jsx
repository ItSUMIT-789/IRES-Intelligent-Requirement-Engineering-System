import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Plus } from 'lucide-react'
import GlassCard from '../GlassCard.jsx'
import { projectService } from '../../services/projectService.js'
import { ClientHeader } from '../../pages/dashboards/ClientDashboard.jsx'

export default function ClientProjects() {
  const [projects, setProjects] = useState([]); const [creating, setCreating] = useState(false); const [loading, setLoading] = useState(true); const [error, setError] = useState(''); const [form, setForm] = useState({ name: '', description: '' })
  const load = async () => {
    setError('')
    setLoading(true)
    try {
      const response = await projectService.getProjects({ size: 100, sort: 'createdAt,desc' })
      const payload = response?.data
      setProjects(Array.isArray(payload) ? payload : Array.isArray(payload?.content) ? payload.content : [])
    } catch (e) {
      setProjects([])
      setError(e.message || 'Unable to load projects.')
    } finally {
      setLoading(false)
    }
  }
  useEffect(() => { load() }, [])
  const create = async (event) => { event.preventDefault(); setError(''); try { await projectService.createProject({ name: form.name, description: form.description }); setForm({ name: '', description: '' }); setCreating(false); await load() } catch (e) { setError(e.message) } }
  return <><ClientHeader title="My Projects" subtitle="Projects owned by or assigned to your authenticated account." action={<button onClick={() => setCreating((v) => !v)} className="btn-gradient !px-4 !py-2.5"><Plus size={17} />Create Project</button>} />
    {creating && <GlassCard hover={false} className="mb-6 p-6"><form onSubmit={create} className="space-y-4"><Field label="Project name" value={form.name} onChange={(name) => setForm({ ...form, name })} required /><Area label="Description" value={form.description} onChange={(description) => setForm({ ...form, description })} /><button className="btn-gradient" type="submit">Create Project</button></form></GlassCard>}
    {loading && <GlassCard hover={false} className="p-6"><p className="text-slate-400">Loading projects…</p></GlassCard>}
    {!loading && error && <Error message={error} />}{!loading && !error && projects.length === 0 && <Empty message="You do not have any projects yet." />}
    {!loading && !error && <div className="grid gap-5 lg:grid-cols-2">{projects.filter(Boolean).map((p) => <GlassCard key={p.id} hover={false} className="p-6"><div className="flex items-start justify-between gap-3"><div><h2 className="text-lg font-semibold text-white">{p.name || 'Untitled project'}</h2><p className="mt-2 text-sm text-slate-400">{p.description || 'No description provided.'}</p></div><Status value={p.status} /></div><dl className="mt-5 grid grid-cols-2 gap-3 text-sm"><Meta label="Created" value={date(p.createdAt)} /><Meta label="Requirements" value={p.requirementCount ?? 0} /></dl>{p.id && <Link className="mt-5 inline-flex text-sm font-medium text-blue-300 hover:text-blue-200" to={`/client/projects/${p.id}`}>Open project →</Link>}</GlassCard>)}</div>}</>
}

export const date = (value) => value ? new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(value)) : '—'
export function Field({ label, value, onChange, ...props }) { return <label className="block text-sm text-slate-300">{label}<input className="mt-1.5 w-full rounded-xl border border-white/10 bg-white/[0.04] px-4 py-2.5 outline-none focus:border-blue-400/60" value={value} onChange={(e) => onChange(e.target.value)} {...props} /></label> }
export function Area({ label, value, onChange, ...props }) { return <label className="block text-sm text-slate-300">{label}<textarea className="mt-1.5 min-h-28 w-full rounded-xl border border-white/10 bg-white/[0.04] px-4 py-2.5 outline-none focus:border-blue-400/60" value={value} onChange={(e) => onChange(e.target.value)} {...props} /></label> }
const statusTone = (value) => {
  const status = String(value || '').toUpperCase().replaceAll(' ', '_')
  if (status.includes('FAILED') || status === 'REJECTED') return 'border-red-200 bg-red-50 text-red-700'
  if (['COMPLETED', 'TEST_PASSED', 'ANALYSIS_COMPLETED', 'ACTIVE'].includes(status)) return 'border-green-200 bg-green-100 text-green-800'
  if (['ASSIGNED_TO_DEVELOPER', 'IN_DEVELOPMENT', 'ASSIGNED_TO_TESTER'].includes(status)) return 'border-green-200 bg-green-50 text-green-700'
  if (['SUBMITTED', 'IN_ANALYSIS', 'NEEDS_CLARIFICATION', 'WAITING_FOR_ADMIN_ASSIGNMENT', 'READY_FOR_TESTING', 'IN_TESTING', 'WAITING_FOR_ADMIN_APPROVAL'].includes(status)) return 'border-yellow-200 bg-yellow-50 text-yellow-800'
  return 'border-slate-200 bg-slate-100 text-slate-700'
}
export function Status({ value }) { return <span className={`shrink-0 rounded-full border px-3 py-1 text-xs font-semibold ${statusTone(value)}`}>{String(value || '').replaceAll('_', ' ')}</span> }
export function Meta({ label, value }) { return <div><dt className="text-xs font-semibold uppercase tracking-wide text-slate-500">{label}</dt><dd className="mt-1 text-slate-700">{value}</dd></div> }
export function Error({ message }) { return <div className="mb-5 rounded-xl border border-red-200 bg-red-50 p-5"><p className="text-sm text-red-700">{message}</p></div> }
export function Empty({ message }) { return <div className="rounded-xl border border-dashed border-slate-300 bg-white p-8 text-center"><p className="font-medium text-slate-700">Nothing here yet</p><p className="mt-1 text-sm text-slate-500">{message}</p></div> }
