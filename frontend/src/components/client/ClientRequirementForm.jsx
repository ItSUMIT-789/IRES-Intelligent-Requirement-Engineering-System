import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import GlassCard from '../GlassCard.jsx'
import FileDropzone from '../FileDropzone.jsx'
import { projectService } from '../../services/projectService.js'
import { requirementService } from '../../services/requirementService.js'
import { ClientHeader } from '../../pages/dashboards/ClientDashboard.jsx'
import { Area, Error, Field } from './ClientProjects.jsx'

const initial = { title: '', description: '', priority: 'MEDIUM', requirementType: 'FUNCTIONAL', source: '' }
export default function ClientRequirementForm() {
  const navigate = useNavigate(); const [params] = useSearchParams(); const [projects, setProjects] = useState([]); const [projectId, setProjectId] = useState(params.get('projectId') || ''); const [form, setForm] = useState(initial); const [files, setFiles] = useState([]); const [error, setError] = useState(''); const [saving, setSaving] = useState(false)
  useEffect(() => { projectService.getProjects({ size: 100, sort: 'name,asc' }).then((r) => setProjects(r.data.content || [])).catch((e) => setError(e.message)) }, [])
  const save = async (submit) => { setSaving(true); setError(''); try { const created = (await requirementService.create(projectId, form)).data; for (const file of files) await requirementService.uploadAttachment(created.id, file); if (submit) await requirementService.action(created.id, 'submit'); navigate(`/client/requirements/${created.id}`) } catch (e) { setError(e.message) } finally { setSaving(false) } }
  return <><ClientHeader title="Submit Requirement" subtitle="Select one of your real projects, then save a draft or submit it to analysis." />{error && <Error message={error} />}
    {projects.length === 0 ? <GlassCard className="p-6"><p className="text-slate-400">Create a project before adding a requirement.</p></GlassCard> : <GlassCard hover={false} className="max-w-3xl p-6"><div className="space-y-5"><label className="block text-sm text-slate-300">Select Project<select value={projectId} onChange={(e) => setProjectId(e.target.value)} className="mt-1.5 w-full rounded-xl border border-white/10 bg-space-900 px-4 py-2.5" required><option value="">Choose a project</option>{projects.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}</select></label>
      {projectId && <><Field label="Title" value={form.title} onChange={(title) => setForm({ ...form, title })} required maxLength={300} /><Area label="Description" value={form.description} onChange={(description) => setForm({ ...form, description })} maxLength={10000} /><div className="grid gap-4 sm:grid-cols-3"><Select label="Priority" value={form.priority} values={['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']} onChange={(priority) => setForm({ ...form, priority })} /><Select label="Type" value={form.requirementType} values={['FUNCTIONAL', 'NON_FUNCTIONAL', 'BUSINESS', 'TECHNICAL']} onChange={(requirementType) => setForm({ ...form, requirementType })} /><Field label="Source" value={form.source} onChange={(source) => setForm({ ...form, source })} maxLength={100} /></div><div><p className="mb-2 text-sm text-slate-300">Attachments (optional)</p><FileDropzone files={files} onChange={setFiles} /></div><div className="flex flex-wrap gap-3"><button disabled={saving || !form.title.trim()} onClick={() => save(false)} className="btn-outline">{saving ? 'Saving…' : 'Save Draft'}</button><button disabled={saving || !form.title.trim()} onClick={() => save(true)} className="btn-gradient">Save and Submit</button></div></>}</div></GlassCard>}</>
}
function Select({ label, value, values, onChange }) { return <label className="block text-sm text-slate-300">{label}<select value={value} onChange={(e) => onChange(e.target.value)} className="mt-1.5 w-full rounded-xl border border-white/10 bg-space-900 px-4 py-2.5">{values.map((v) => <option key={v}>{v}</option>)}</select></label> }
export { Select }
