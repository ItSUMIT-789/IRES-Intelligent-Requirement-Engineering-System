import { useEffect, useState } from 'react'
import GlassCard from '../GlassCard.jsx'
import { projectService } from '../../services/projectService.js'
import { requirementService } from '../../services/requirementService.js'
import { getRequirementStatusLabel, getWorkflowActions } from '../../config/requirementWorkflow.js'
import { useAuth } from '../../context/AuthContext.jsx'

export default function RequirementWorkflowPanel({ title = 'Requirements' }) {
  const { user } = useAuth()
  const [requirements, setRequirements] = useState([])
  const [projects, setProjects] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = async () => {
    setLoading(true); setError('')
    try {
      const projectPage = (await projectService.getProjects({ size: 100 })).data
      const availableProjects = projectPage.content || []
      setProjects(availableProjects)
      const pages = await Promise.all(availableProjects.map((project) => requirementService.list(project.id, 'size=100')))
      setRequirements(pages.flatMap((response) => response.data.content || []))
    } catch (requestError) { setError(requestError.message || 'Requirements could not be loaded.') }
    finally { setLoading(false) }
  }
  useEffect(() => { load() }, [user?.id])

  const act = async (requirement, action) => {
    let body
    if (action.includes('clarification')) {
      const message = window.prompt(action.startsWith('request') ? 'Clarification question' : 'Clarification response')
      if (!message) return
      body = { message }
      if (action === 'respond-clarification') action = 'clarifications'
    }
    try { await requirementService.action(requirement.id, action, body); await load() }
    catch (requestError) { setError(requestError.message) }
  }

  return <><div className="mb-8"><span className="text-xs font-semibold uppercase tracking-widest text-blue-300">{user?.roleLabel}</span>
    <h1 className="font-display text-3xl font-bold text-white">{title}</h1><p className="text-sm text-slate-400">Live requirements from your accessible projects.</p></div>
    {loading && <GlassCard className="p-6"><p className="text-slate-400">Loading requirements…</p></GlassCard>}
    {error && <GlassCard className="mb-4 p-5"><p className="text-red-300">{error}</p></GlassCard>}
    {!loading && requirements.length === 0 && <GlassCard className="p-6"><p className="text-slate-400">No accessible requirements yet.</p></GlassCard>}
    <div className="space-y-4">{requirements.map((requirement) => <GlassCard key={requirement.id} hover={false} className="p-5">
      <div className="flex flex-wrap items-start justify-between gap-3"><div><h2 className="font-display font-semibold text-white">{requirement.title}</h2>
        <p className="mt-1 text-sm text-slate-400">{requirement.description || 'No description provided.'}</p></div>
        <span className="rounded-full border border-blue-400/20 bg-blue-500/10 px-3 py-1 text-xs text-blue-200">{getRequirementStatusLabel(requirement.status)}</span></div>
      <div className="mt-4 flex flex-wrap gap-2">{getWorkflowActions(user?.role, requirement.status).map(([action, label]) =>
        <button key={action} type="button" onClick={() => act(requirement, action)} className="rounded-lg bg-blue-500/20 px-3 py-2 text-xs font-medium text-blue-200 hover:bg-blue-500/30">{label}</button>)}</div>
    </GlassCard>)}</div></>
}
