import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import GlassCard from '../GlassCard.jsx'
import { projectService } from '../../services/projectService.js'
import { requirementService } from '../../services/requirementService.js'
import { getRequirementStatusLabel } from '../../config/requirementWorkflow.js'
import { AnalystHeader } from '../../pages/dashboards/AnalystDashboard.jsx'
import { date, Empty, Error, Status } from '../client/ClientProjects.jsx'

const config = {
  queue: { title: 'Requirement Queue', subtitle: 'Shared incoming and active analyst-stage requirements.', statuses: ['SUBMITTED', 'IN_ANALYSIS', 'NEEDS_CLARIFICATION', 'ANALYSIS_COMPLETED'] },
  analysis: { title: 'Requirement Analysis', subtitle: 'Requirements currently in analysis or awaiting clarification.', statuses: ['IN_ANALYSIS', 'NEEDS_CLARIFICATION'] },
  ai: { title: 'AI Suggestions', subtitle: 'Select an accessible requirement to run or review advisory analysis.', statuses: ['SUBMITTED', 'IN_ANALYSIS', 'NEEDS_CLARIFICATION'] },
  criteria: { title: 'Acceptance Criteria', subtitle: 'Select a requirement to manage its real acceptance criteria.', statuses: ['IN_ANALYSIS', 'NEEDS_CLARIFICATION', 'ANALYSIS_COMPLETED'] },
  stories: { title: 'User Stories', subtitle: 'Select a requirement to manage its user stories.', statuses: ['IN_ANALYSIS', 'NEEDS_CLARIFICATION', 'ANALYSIS_COMPLETED'] },
  traceability: { title: 'Traceability', subtitle: 'Select a requirement to manage validated traceability links.', statuses: ['IN_ANALYSIS', 'NEEDS_CLARIFICATION', 'ANALYSIS_COMPLETED'] },
}
export default function AnalystRequirements({ mode }) {
  const details = config[mode]; const [requirements, setRequirements] = useState([]); const [projects, setProjects] = useState([]); const [filters, setFilters] = useState({ projectId: '', status: '', priority: '', search: '' }); const [error, setError] = useState(''); const [starting,setStarting]=useState('')
  useEffect(() => { projectService.getProjects({ size: 100 }).then((r) => setProjects(r.data.content || [])).catch((e) => setError(e.message)) }, [])
  useEffect(() => {
    const query = { size: 100, sort: 'createdAt,asc', ...Object.fromEntries(Object.entries(filters).filter(([, v]) => v)) }
    const request = mode === 'queue' ? requirementService.listAnalystQueue(query) : requirementService.listAccessible(query)
    request
      .then((response) => { const items=Array.isArray(response?.data)?response.data:(response?.data?.content||[]);setRequirements(items.filter((item) => details.statuses.includes(item.status)));if(mode==='queue')setProjects((existing)=>{const incoming=items.map((item)=>({id:item.projectId,name:item.projectName})).filter((item)=>item.id);return [...new Map([...existing,...incoming].map((item)=>[item.id,item])).values()]}) })
      .catch((requestError) => setError(requestError.message))
  }, [mode, filters.projectId, filters.status, filters.priority, filters.search])
  const startAnalysis=async(id)=>{setStarting(id);setError('');try{await requirementService.action(id,'start-analysis',{});setRequirements((items)=>items.map((item)=>item.id===id?{...item,status:'IN_ANALYSIS'}:item))}catch(e){setError(e.message)}finally{setStarting('')}}
  return <><AnalystHeader title={details.title} subtitle={details.subtitle} /><GlassCard hover={false} className="mb-5 p-4"><div className="grid gap-3 md:grid-cols-4"><Filter value={filters.projectId} onChange={(projectId) => setFilters({ ...filters, projectId })}><option value="">All projects</option>{projects.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}</Filter><Filter value={filters.status} onChange={(status) => setFilters({ ...filters, status })}><option value="">Relevant statuses</option>{details.statuses.map((v) => <option key={v}>{v}</option>)}</Filter><Filter value={filters.priority} onChange={(priority) => setFilters({ ...filters, priority })}><option value="">All priorities</option>{['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].map((v) => <option key={v}>{v}</option>)}</Filter><input value={filters.search} onChange={(e) => setFilters({ ...filters, search: e.target.value })} placeholder="Search requirements" className="rounded-xl border border-white/10 bg-white/[0.04] px-3 py-2.5 text-sm" /></div></GlassCard>{error && <Error message={error} />}{!error && requirements.length === 0 && <Empty message="No accessible requirements match this analyst view." />}
    <div className="overflow-x-auto rounded-2xl border border-white/10"><table className="w-full min-w-[900px] text-left text-sm"><thead className="bg-white/[0.06] text-xs uppercase tracking-wide text-slate-400"><tr>{['Requirement', 'Project', 'Client', 'Priority', 'Status', 'Created', 'Updated', 'Action'].map((h) => <th key={h} className="px-4 py-3">{h}</th>)}</tr></thead><tbody>{requirements.map((r) => <tr key={r.id} className="border-t border-white/10 text-slate-300"><td className="px-4 py-4 font-medium text-white">{r.title}</td><td className="px-4 py-4">{r.projectName}</td><td className="px-4 py-4">{r.projectClient?.name}</td><td className="px-4 py-4">{r.priority}</td><td className="px-4 py-4"><Status value={getRequirementStatusLabel(r.status)} /></td><td className="px-4 py-4">{date(r.createdAt)}</td><td className="px-4 py-4">{date(r.updatedAt)}</td><td className="px-4 py-4">{mode==='queue'&&r.status==='SUBMITTED'?<button disabled={starting===r.id} onClick={()=>startAnalysis(r.id)} className="text-blue-300 disabled:opacity-50">{starting===r.id?'Starting…':'Start Analysis'}</button>:<Link className="text-blue-300" to={`/analyst/requirements/${r.id}`}>Open workspace</Link>}</td></tr>)}</tbody></table></div></>
}
function Filter({ value, onChange, children }) { return <select value={value} onChange={(e) => onChange(e.target.value)} className="rounded-xl border border-white/10 bg-space-900 px-3 py-2.5 text-sm">{children}</select> }
