import { useEffect, useRef, useState } from 'react'
import { Check, ChevronDown, ChevronUp, LoaderCircle, Pencil, Sparkles, X } from 'lucide-react'
import GlassCard from '../GlassCard.jsx'
import GradientButton from '../GradientButton.jsx'
import { acceptanceCriteriaService } from '../../services/acceptanceCriteriaService.js'
import { aiAnalysisService } from '../../services/aiAnalysisService.js'
import { humanReviewService } from '../../services/humanReviewService.js'
import { projectService } from '../../services/projectService.js'
import { requirementService } from '../../services/requirementService.js'
import { userStoryService } from '../../services/userStoryService.js'
import { generateSrsPdf } from '../../utils/srsPdf.js'

const analyses = [
  { key: 'classification', label: 'Classification', method: 'classifyRequirement' },
  { key: 'ambiguity', label: 'Ambiguity detection', method: 'detectAmbiguity' },
  { key: 'completeness', label: 'Completeness', method: 'detectIncompleteRequirement' },
  { key: 'quality', label: 'Quality analysis', method: 'analyzeRequirementQuality' },
  { key: 'duplicates', label: 'Duplicate detection', method: 'detectDuplicateRequirements', candidates: true },
  { key: 'conflicts', label: 'Conflict detection', method: 'detectRequirementConflicts', candidates: true },
]

const emptyTask = (status = 'idle') => ({ status, result: null, error: '' })
const errorText = (error) => error?.data?.message || (error?.status ? error.message : 'This request could not be completed. Please try again.')
const hasEmptyResult = (result) => {
  if (result == null) return true
  if (Array.isArray(result)) return result.length === 0
  if (typeof result === 'object') return Object.keys(result).length === 0
  return false
}

export default function AnalystAIStudio({ requirement, workspace, onRefresh }) {
  const [tasks, setTasks] = useState({})
  const [candidateRequirements, setCandidateRequirements] = useState([])
  const [candidateStatus, setCandidateStatus] = useState('loading')
  const [candidateError, setCandidateError] = useState('')
  const [candidateRetry, setCandidateRetry] = useState(0)
  const running = useRef(new Set())

  useEffect(() => {
    let active = true
    setCandidateRequirements([])
    setCandidateStatus('loading')
    setCandidateError('')
    requirementService.list(requirement.projectId, 'size=100')
      .then((response) => {
        if (!active) return
        const items = (response.data?.content || []).filter((item) => item.id !== requirement.id)
        setCandidateRequirements(items)
        setCandidateStatus(items.length ? 'success' : 'empty')
      })
      .catch((error) => {
        if (!active) return
        setCandidateError(errorText(error))
        setCandidateStatus('error')
      })
    return () => { active = false }
  }, [requirement.id, requirement.projectId, candidateRetry])

  const execute = async (key, action) => {
    if (running.current.has(key)) return
    running.current.add(key)
    setTasks((current) => ({ ...current, [key]: emptyTask('loading') }))
    try {
      const response = await action()
      const result = response?.data ?? null
      setTasks((current) => ({
        ...current,
        [key]: { status: hasEmptyResult(result) ? 'empty' : 'success', result, error: '' },
      }))
      return result
    } catch (error) {
      setTasks((current) => ({ ...current, [key]: { status: 'error', result: null, error: errorText(error) } }))
    } finally {
      running.current.delete(key)
    }
  }

  const candidateIds = candidateRequirements.map((item) => item.id)
  const projectRequirements = [requirement, ...candidateRequirements]

  return (
    <div className="space-y-6">
      <Section title="AI Requirement Analysis" description="Results are advisory. No analysis changes the requirement or resolves a conflict automatically.">
        <div className="grid gap-4 lg:grid-cols-2">
          {analyses.map((analysis) => {
            const task = tasks[analysis.key] || emptyTask()
            const blocked = analysis.candidates && ['loading', 'error'].includes(candidateStatus)
            const run = () => execute(analysis.key, () => aiAnalysisService[analysis.method](
              requirement.id,
              ...(analysis.candidates ? [candidateIds] : []),
            ))
            return (
              <GlassCard key={analysis.key} hover={false} className="p-4">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <h3 className="font-semibold text-white">{analysis.label}</h3>
                  <GradientButton
                    variant="outline"
                    icon={task.status === 'loading' ? LoaderCircle : Sparkles}
                    disabled={task.status === 'loading' || blocked}
                    onClick={run}
                    aria-label={`${task.status === 'error' ? 'Retry' : 'Run'} ${analysis.label}`}
                  >
                    {task.status === 'loading' ? 'Analyzing…' : task.status === 'error' ? 'Retry' : 'Run analysis'}
                  </GradientButton>
                </div>
                {analysis.candidates && <CandidatePicker
                  candidates={candidateRequirements}
                  status={candidateStatus}
                  error={candidateError}
                  onRetry={() => setCandidateRetry((value) => value + 1)}
                />}
                <TaskFeedback task={task} emptyMessage="The analysis returned no findings." />
                {task.status === 'success' && <AnalysisResult type={analysis.key} result={task.result} />}
              </GlassCard>
            )
          })}
        </div>
      </Section>

      <Section title="AI Generation" description="Generated content stays separate until you review and explicitly approve it.">
        <div className="grid gap-4 xl:grid-cols-2">
          <GlassCard hover={false} className="p-4">
            <OperationHeader
              title="Requirement improvement"
              task={tasks.improvement || emptyTask()}
              onRun={() => execute('improvement', () => aiAnalysisService.improveRequirement(requirement.id))}
            />
            <TaskFeedback task={tasks.improvement || emptyTask()} emptyMessage="No improvement proposal was returned." />
            {(tasks.improvement?.status === 'success') && <RequirementProposal
              requirement={requirement}
              proposal={tasks.improvement.result}
              onRefresh={onRefresh}
              onRejected={() => setTasks((current) => ({ ...current, improvement: emptyTask('empty') }))}
            />}
          </GlassCard>

          <GlassCard hover={false} className="p-4">
            <OperationHeader
              title="User story"
              task={tasks.story || emptyTask()}
              onRun={() => execute('story', () => userStoryService.generate(requirement.id))}
            />
            <TaskFeedback task={tasks.story || emptyTask()} emptyMessage="No user story was generated." />
            {tasks.story?.status === 'success' && <>
              <StoryPreview story={tasks.story.result} />
              <ReviewWorkflow
                key={`story-${tasks.story.result.id}`}
                artifactType="USER_STORY"
                artifactId={tasks.story.result.id}
                initialContent={storyContent(tasks.story.result)}
                onCompleted={(review) => {
                  setTasks((current) => ({ ...current, story: {
                    ...current.story,
                    result: { ...current.story.result, ...review.reviewedContent, status: review.artifactStatus },
                  } }))
                  return onRefresh()
                }}
              />
            </>}
          </GlassCard>

          <GlassCard hover={false} className="p-4">
            <OperationHeader
              title="Acceptance criteria"
              task={tasks.criteria || emptyTask()}
              onRun={() => execute('criteria', () => acceptanceCriteriaService.generate(requirement.id))}
            />
            <TaskFeedback task={tasks.criteria || emptyTask()} emptyMessage="No acceptance criteria were generated." />
            {tasks.criteria?.status === 'success' && <div className="mt-4 space-y-4">
              {tasks.criteria.result.map((criterion) => <div key={criterion.id} className="border-t border-white/10 pt-4">
                <CriteriaPreview criterion={criterion} />
                <ReviewWorkflow
                  key={`criteria-${criterion.id}`}
                  artifactType="ACCEPTANCE_CRITERIA"
                  artifactId={criterion.id}
                  initialContent={criteriaContent(criterion)}
                  onCompleted={(review) => {
                    setTasks((current) => ({ ...current, criteria: {
                      ...current.criteria,
                      result: current.criteria.result.map((item) => item.id === criterion.id
                        ? { ...item, ...review.reviewedContent, status: review.artifactStatus }
                        : item),
                    } }))
                    return onRefresh()
                  }}
                />
              </div>)}
            </div>}
          </GlassCard>

          <GlassCard hover={false} className="p-4">
            <OperationHeader
              title="Project SRS"
              task={tasks.srs || emptyTask()}
              onRun={() => execute('srs', () => projectService.generateSrs(requirement.projectId))}
            />
            <TaskFeedback task={tasks.srs || emptyTask()} emptyMessage="No SRS document was generated." />
            {tasks.srs?.status === 'success' && <SrsDocumentPanel
              document={tasks.srs.result}
              projectName={requirement.projectName}
              requirements={projectRequirements}
              stories={workspace.stories}
              criteria={workspace.criteria}
              traceability={workspace.links}
              onReviewed={(review) => setTasks((current) => ({ ...current, srs: {
                ...current.srs,
                result: {
                  ...current.srs.result,
                  title: review.reviewedContent?.title || current.srs.result.title,
                  content: review.reviewedContent?.content || current.srs.result.content,
                  status: review.artifactStatus,
                },
              } }))}
            />}
          </GlassCard>
        </div>
      </Section>
    </div>
  )
}

function Section({ title, description, children }) {
  return <section className="space-y-4">
    <div><h2 className="text-xl font-semibold text-white">{title}</h2><p className="mt-1 text-sm text-slate-400">{description}</p></div>
    {children}
  </section>
}

function OperationHeader({ title, task, onRun }) {
  return <div className="flex flex-wrap items-center justify-between gap-3">
    <h3 className="font-semibold text-white">{title}</h3>
    <GradientButton
      variant="outline"
      icon={task.status === 'loading' ? LoaderCircle : Sparkles}
      disabled={task.status === 'loading'}
      onClick={onRun}
    >
      {task.status === 'loading' ? 'Generating…' : task.status === 'error' ? 'Retry' : 'Generate'}
    </GradientButton>
  </div>
}

function TaskFeedback({ task, emptyMessage }) {
  if (task.status === 'loading') return <p role="status" className="mt-4 flex items-center gap-2 text-sm text-slate-300"><LoaderCircle size={16} className="animate-spin" />Working…</p>
  if (task.status === 'error') return <p role="alert" className="mt-4 rounded-lg border border-red-300/20 bg-red-400/10 p-3 text-sm text-red-100">{task.error}</p>
  if (task.status === 'empty') return <p className="mt-4 text-sm text-slate-400">{emptyMessage}</p>
  return null
}

function CandidatePicker({ candidates, status, error, onRetry }) {
  if (status === 'loading') return <p className="mt-3 text-xs text-slate-400">Loading project requirements…</p>
  if (status === 'error') return <div className="mt-3 text-xs text-red-200"><p role="alert">Candidate list unavailable: {error}</p><button onClick={onRetry} className="mt-1 underline">Retry loading requirements</button></div>
  if (status === 'empty') return <p className="mt-3 text-xs text-slate-400">No other requirements are available for comparison.</p>
  return <details className="mt-3">
    <summary className="cursor-pointer text-xs text-slate-400">Compare against all {candidates.length} other project requirements</summary>
    <ul className="mt-2 max-h-32 space-y-1 overflow-y-auto text-xs text-slate-300">
      {candidates.map((candidate) => <li key={candidate.id} className="flex gap-2">
        <span className="text-slate-500">{candidate.title}</span>
      </li>)}
    </ul>
  </details>
}

function AnalysisResult({ type, result }) {
  if (type === 'classification') return <div className="mt-4 grid gap-2 text-sm sm:grid-cols-2">
    <Fact label="Classification" value={result.classification} />
    <Fact label="Confidence" value={result.confidence} />
    <Fact label="Reason" value={result.reason} wide />
  </div>
  if (type === 'ambiguity') return <div className="mt-4 space-y-3 text-sm">
    <Fact label="Ambiguity" value={result.hasAmbiguity ? 'Ambiguity detected' : 'No ambiguity detected'} />
    <Fact label="Confidence" value={result.confidence} />
    <ResultList items={result.findings} empty="No ambiguous wording was identified." render={(item) => <>
      <p className="font-medium text-slate-100">{item.text}</p><p className="mt-1 text-slate-400">Reason: {item.reason}</p><p className="mt-1 text-emerald-200">Suggested wording: {item.suggestion}</p>
    </>} />
  </div>
  if (type === 'completeness') return <div className="mt-4 space-y-3 text-sm">
    <Fact label="Status" value={result.isComplete ? 'Complete' : 'Incomplete'} />
    <Fact label="Confidence" value={result.confidence} />
    <ResultList items={result.missingInformation} empty="No missing information was identified." render={(item) => <>
      <p className="font-medium text-slate-100">{item.aspect}</p><p className="mt-1 text-slate-400">{item.description}</p>
    </>} />
    <ResultList items={result.clarificationQuestions} empty="No clarification questions suggested." render={(item) => <p>{item}</p>} title="Clarification questions" />
  </div>
  if (type === 'quality') return <div className="mt-4 space-y-3 text-sm">
    <Fact label="Overall score" value={result.overallScore ?? '—'} />
    <Fact label="Confidence" value={result.confidence} />
    <ResultList items={result.dimensions} empty="No quality dimensions returned." render={(item) => <>
      <div className="flex flex-wrap justify-between gap-2"><p className="font-medium text-slate-100">{item.name}</p><p className="text-emerald-200">Score: {item.score ?? '—'}</p></div>
      <p className="mt-1 text-slate-400">Finding: {item.finding}</p><p className="mt-1 text-slate-300">Recommendation: {item.recommendation}</p>
    </>} />
  </div>
  if (type === 'duplicates') return <div className="mt-4 space-y-3 text-sm">
    <Fact label="Confidence" value={result.confidence} />
    <ResultList items={result.duplicates} empty="No potential duplicates found." render={(item) => <>
      <p className="font-medium text-slate-100">Requirement {item.requirementId}</p><p className="mt-1 text-slate-300">Similarity: {item.similarity} · Relationship: {item.relationship}</p><p className="mt-1 text-slate-400">{item.reason}</p>
    </>} />
  </div>
  return <div className="mt-4 space-y-3 text-sm">
    <Fact label="Confidence" value={result.confidence} />
    <ResultList items={result.conflicts} empty="No conflicts found." render={(item) => <>
      <p className="font-medium text-slate-100">Requirement {item.requirementId}</p><p className="mt-1 text-slate-300">{item.conflictType} · Severity: {item.severity}</p><p className="mt-1 text-slate-400">{item.reason}</p><p className="mt-1 text-emerald-200">Suggested action: {item.suggestion}</p>
    </>} />
  </div>
}

function Fact({ label, value, wide = false }) {
  return <div className={`rounded-lg border border-white/10 bg-black/10 p-3 ${wide ? 'sm:col-span-2' : ''}`}>
    <p className="text-xs text-slate-500">{label}</p><p className="mt-1 whitespace-pre-wrap text-slate-200">{value ?? '—'}</p>
  </div>
}

function ResultList({ items, empty, render, title }) {
  return <div>
    {title && <h4 className="mb-2 font-medium text-slate-200">{title}</h4>}
    {!items?.length ? <p className="text-slate-400">{empty}</p> : <ul className="space-y-2">{items.map((item, index) => <li key={item?.requirementId || item?.aspect || item?.text || item?.name || index} className="rounded-lg border border-white/10 bg-black/10 p-3">{render(item)}</li>)}</ul>}
  </div>
}

function StoryPreview({ story }) {
  return <div className="mt-4 rounded-lg border border-emerald-200/20 bg-emerald-300/[0.04] p-3">
    <p className="text-xs font-semibold uppercase text-emerald-200">AI generated · {story.status}</p>
    <h4 className="mt-2 font-medium text-white">{story.title}</h4>
    {story.description && <p className="mt-1 text-sm text-slate-300">{story.description}</p>}
    <p className="mt-2 whitespace-pre-wrap text-sm text-slate-300">{story.storyText}</p>
  </div>
}

function CriteriaPreview({ criterion }) {
  return <div className="rounded-lg border border-emerald-200/20 bg-emerald-300/[0.04] p-3">
    <p className="text-xs font-semibold uppercase text-emerald-200">AI generated · {criterion.status}</p>
    <h4 className="mt-2 font-medium text-white">{criterion.title}</h4>
    <p className="mt-1 text-xs text-slate-400">{criterion.criteriaType}</p>
    <p className="mt-2 whitespace-pre-wrap text-sm text-slate-300">{criterion.description}</p>
  </div>
}

const storyContent = (story) => ({ title: story.title, description: story.description, storyText: story.storyText, priority: story.priority })
const criteriaContent = (criterion) => ({ title: criterion.title, description: criterion.description, criteriaType: criterion.criteriaType })

function RequirementProposal({ requirement, proposal, onRefresh, onRejected }) {
  const submitting = useRef(false)
  const [reviewing, setReviewing] = useState(false)
  const [editing, setEditing] = useState(false)
  const [draft, setDraft] = useState({ title: proposal.proposedTitle, description: proposal.proposedDescription || '' })
  const [state, setState] = useState('idle')
  const [error, setError] = useState('')

  const accept = async () => {
    if (submitting.current) return
    submitting.current = true
    setState('loading')
    setError('')
    try {
      await requirementService.update(requirement.id, {
        title: draft.title,
        description: draft.description,
        requirementType: requirement.requirementType,
        priority: requirement.priority,
        status: requirement.status,
        source: requirement.source,
        assignedTo: requirement.assignedTo?.id ?? null,
      })
      setState('accepted')
      await onRefresh()
    } catch (requestError) {
      setState('error')
      setError(errorText(requestError))
    } finally {
      submitting.current = false
    }
  }

  if (state === 'accepted') return <p role="status" className="mt-4 text-sm text-emerald-200">Accepted and saved through the existing requirement workflow.</p>
  if (state === 'rejected') return <p role="status" className="mt-4 text-sm text-slate-400">Proposal rejected. The original requirement was not changed.</p>
  return <div className="mt-4 space-y-3">
    <div className="rounded-lg border border-emerald-200/20 bg-emerald-300/[0.04] p-3 text-sm">
      <p className="text-xs font-semibold uppercase text-emerald-200">AI proposal · confidence {proposal.confidence ?? '—'}</p>
      <h4 className="mt-3 text-xs uppercase text-slate-500">Original title</h4><p className="text-slate-200">{requirement.title}</p>
      <h4 className="mt-2 text-xs uppercase text-slate-500">Proposed title</h4><p className="text-white">{draft.title}</p>
      <h4 className="mt-2 text-xs uppercase text-slate-500">Original description</h4><p className="whitespace-pre-wrap text-slate-300">{requirement.description || 'No description provided.'}</p>
      <h4 className="mt-2 text-xs uppercase text-slate-500">Proposed description</h4><p className="whitespace-pre-wrap text-slate-200">{draft.description || 'No description provided.'}</p>
      <p className="mt-3 text-slate-400">Rationale: {proposal.rationale || 'No rationale provided.'}</p>
    </div>
    {!reviewing ? <GradientButton variant="outline" icon={ChevronDown} onClick={() => setReviewing(true)}>Review proposal</GradientButton> : <div className="space-y-3">
      {editing && <div className="space-y-3">
        <TextField label="Proposed title" value={draft.title} onChange={(title) => setDraft((value) => ({ ...value, title }))} />
        <TextField label="Proposed description" multiline value={draft.description} onChange={(description) => setDraft((value) => ({ ...value, description }))} />
      </div>}
      {error && <p role="alert" className="text-sm text-red-200">{error}</p>}
      {state === 'error' && <button onClick={accept} className="btn-outline !py-2">Retry acceptance</button>}
      <div className="flex flex-wrap gap-2">
        <button disabled={state === 'loading'} onClick={() => setEditing((value) => !value)} className="btn-outline !py-2"><Pencil size={15} aria-hidden="true" />{editing ? 'Done editing' : 'Edit proposal'}</button>
        <button disabled={state === 'loading' || !draft.title.trim()} onClick={accept} className="btn-gradient !py-2">{state === 'loading' ? 'Saving…' : 'Accept and update requirement'}</button>
        <button disabled={state === 'loading'} onClick={() => { setState('rejected'); onRejected() }} className="btn-outline !py-2"><X size={15} aria-hidden="true" />Reject proposal</button>
        <button disabled={state === 'loading'} onClick={() => setReviewing(false)} className="btn-outline !py-2"><ChevronUp size={15} aria-hidden="true" />Close review</button>
      </div>
      <p className="text-xs text-slate-500">Acceptance updates the existing requirement only after confirmation. This proposal does not have a backend review-history artifact.</p>
    </div>}
  </div>
}

function ReviewWorkflow({ artifactType, artifactId, initialContent, onCompleted }) {
  const submitting = useRef(false)
  const loadingHistory = useRef(false)
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(false)
  const [draft, setDraft] = useState(() => structuredClone(initialContent))
  const [reason, setReason] = useState('')
  const [status, setStatus] = useState('idle')
  const [error, setError] = useState('')
  const [historyState, setHistoryState] = useState('idle')
  const [historyError, setHistoryError] = useState('')
  const [history, setHistory] = useState([])
  const [pendingAction, setPendingAction] = useState('ACCEPT')

  const loadHistory = async () => {
    if (loadingHistory.current) return
    loadingHistory.current = true
    setHistoryState('loading')
    try {
      const response = await humanReviewService.getAIArtifactReviews(artifactType, artifactId)
      setHistory(response.data || [])
      setHistoryState('success')
    } catch (requestError) {
      setHistoryState('error')
      setHistoryError(errorText(requestError))
    } finally {
      loadingHistory.current = false
    }
  }

  const openReview = () => {
    setOpen(true)
    if (historyState === 'idle' || historyState === 'error') loadHistory()
  }

  const submit = async (action) => {
    if (submitting.current || (action === 'REJECT' && !reason.trim())) return
    submitting.current = true
    setPendingAction(action)
    setStatus('loading')
    setError('')
    try {
      const body = { action, reason: reason.trim() || null }
      if (action === 'MODIFY') body.editedContent = draft
      const response = await humanReviewService.reviewAIArtifact(artifactType, artifactId, body)
      const review = response.data
      setHistory((current) => [review, ...current])
      setStatus(review.artifactStatus === 'REJECTED' ? 'rejected' : 'approved')
      await onCompleted?.(review)
    } catch (requestError) {
      setStatus('error')
      setError(errorText(requestError))
    } finally {
      submitting.current = false
    }
  }

  const isSrs = artifactType === 'SRS_DOCUMENT'
  const fields = isSrs ? [] : artifactType === 'USER_STORY'
    ? [['title', 'Title', false], ['description', 'Description', true], ['storyText', 'Story text', true], ['priority', 'Priority', false]]
    : [['title', 'Title', false], ['description', 'Description', true], ['criteriaType', 'Criteria type', false]]
  const complete = status === 'approved' || status === 'rejected'

  return <div className="mt-4 rounded-lg border border-white/10 p-3">
    {!open ? <button onClick={openReview} className="btn-outline !py-2">Review generated content</button> : <div className="space-y-3">
      <h4 className="font-medium text-white">Human review · {artifactType.replaceAll('_', ' ').toLowerCase()}</h4>
      {historyState === 'loading' && <p role="status" className="text-xs text-slate-400">Loading review history…</p>}
      {historyState === 'error' && <div className="text-xs text-red-200"><p role="alert">{historyError}</p><button onClick={loadHistory} className="mt-1 underline">Retry loading review history</button></div>}
      {historyState === 'success' && history.length === 0 && <p className="text-xs text-slate-400">No previous reviews.</p>}
      {history.length > 0 && <div className="space-y-2">
        <p className="text-xs font-medium text-slate-400">Review history</p>
        {history.map((item) => <div key={item.id} className="rounded-md bg-white/[0.04] p-2 text-xs text-slate-300">
          <p>{item.action} · {item.artifactStatus} · {item.reviewer?.name || 'Reviewer'} · {item.reviewedAt ? new Date(item.reviewedAt).toLocaleString() : ''}</p>
          {item.reason && <p className="mt-1 text-slate-400">{item.reason}</p>}
        </div>)}
      </div>}
      {isSrs ? <SrsEditor draft={draft} setDraft={setDraft} editing={editing} /> : <div className="grid gap-3 sm:grid-cols-2">
        {fields.map(([field, label, multiline]) => <TextField
          key={field}
          label={label}
          value={draft[field] ?? ''}
          disabled={!editing || complete}
          multiline={multiline}
          options={field === 'priority' ? ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'] : field === 'criteriaType' ? ['FUNCTIONAL', 'BEHAVIORAL', 'VALIDATION'] : null}
          onChange={(value) => setDraft((current) => ({ ...current, [field]: value }))}
        />)}
      </div>}
      <TextField label="Review note (required to reject)" value={reason} onChange={setReason} disabled={complete} />
      {error && status === 'error' && <p role="alert" className="text-sm text-red-200">{error}</p>}
      {complete ? <p role="status" className="text-sm text-emerald-200">Artifact status: {status === 'rejected' ? 'REJECTED' : 'APPROVED'}</p> : <div className="flex flex-wrap gap-2">
        <button disabled={status === 'loading' || historyState === 'loading'} onClick={() => setEditing((value) => !value)} className="btn-outline !py-2"><Pencil size={15} aria-hidden="true" />{editing ? 'Stop editing' : 'Edit / Modify'}</button>
        {editing && <button disabled={status === 'loading'} onClick={() => submit('MODIFY')} className="btn-gradient !py-2">{status === 'loading' ? 'Submitting…' : 'Submit modification'}</button>}
        <button disabled={status === 'loading' || editing} onClick={() => submit('ACCEPT')} className="btn-gradient !py-2"><Check size={15} aria-hidden="true" />Accept</button>
        <button disabled={status === 'loading' || !reason.trim()} onClick={() => submit('REJECT')} className="btn-outline !py-2"><X size={15} aria-hidden="true" />Reject</button>
        {status === 'error' && <button onClick={() => submit(pendingAction)} className="btn-outline !py-2">Retry</button>}
      </div>}
      <button onClick={() => setOpen(false)} className="text-xs text-slate-400 underline">Close review</button>
    </div>}
  </div>
}

function SrsDocumentPanel({ document, projectName, requirements, stories, criteria, traceability, onReviewed }) {
  const content = document.content || {}
  const exportPdf = () => {
    const pdf = generateSrsPdf({
      projectName,
      requirements: requirements.map((item) => ({
        id: item.id,
        title: item.title,
        project: item.projectName || projectName,
        priority: item.priority,
        status: item.status,
      })),
      userStories: stories.map((story) => ({
        id: story.id,
        title: story.title,
        acceptanceCriteria: criteria.filter((criterion) => criterion.userStoryId === story.id).map((criterion) => criterion.title),
      })),
      traceability: traceability.map((item) => ({
        requirement: item.sourceId,
        userStory: item.targetType === 'USER_STORY' ? item.targetId : '—',
        testCase: item.targetType === 'TEST_CASE' ? item.targetId : '—',
        developer: '—',
      })),
      srsDocument: { ...document, content },
    })
    pdf.save(`${(document.title || 'IRES-SRS').replace(/[^a-z0-9-_]+/gi, '-').replace(/^-|-$/g, '')}.pdf`)
  }

  return <div className="mt-4 space-y-4">
    <div className="flex flex-wrap items-center justify-between gap-3 border-b border-white/10 pb-3">
      <div><p className="text-xs font-semibold uppercase text-emerald-200">AI-generated SRS · {document.status}</p><h4 className="mt-1 font-semibold text-white">{document.title}</h4></div>
      <GradientButton variant="outline" onClick={exportPdf}>{document.status === 'APPROVED' ? 'Export approved SRS PDF' : 'Export SRS preview PDF'}</GradientButton>
    </div>
    <SrsPreview content={content} />
    <ReviewWorkflow
      key={`srs-${document.id}`}
      artifactType="SRS_DOCUMENT"
      artifactId={document.id}
      initialContent={{ title: document.title, content }}
      onCompleted={onReviewed}
    />
  </div>
}

function SrsPreview({ content }) {
  const sections = [
    ['Overview', content.overview],
    ['Functional requirements', content.functionalRequirements],
    ['Non-functional requirements', content.nonFunctionalRequirements],
    ['Business requirements', content.businessRequirements],
    ['Technical requirements', content.technicalRequirements],
    ['Assumptions', content.assumptions],
    ['Constraints', content.constraints],
  ]
  return <div className="max-h-[34rem] space-y-4 overflow-y-auto rounded-lg border border-white/10 bg-black/10 p-4" aria-label="SRS document preview">
    {sections.map(([title, value]) => <section key={title}>
      <h5 className="font-medium text-white">{title}</h5>
      {Array.isArray(value) ? value.length ? <ol className="mt-2 list-inside list-decimal space-y-1 text-sm text-slate-300">{value.map((item, index) => <li key={`${title}-${index}`}>{item}</li>)}</ol> : <p className="mt-1 text-sm text-slate-500">No entries.</p>
        : <p className="mt-1 whitespace-pre-wrap text-sm text-slate-300">{value || 'No content.'}</p>}
    </section>)}
  </div>
}

function SrsEditor({ draft, setDraft, editing }) {
  const sections = ['functionalRequirements', 'nonFunctionalRequirements', 'businessRequirements', 'technicalRequirements', 'assumptions', 'constraints']
  return <div className="grid gap-3">
    <TextField label="SRS title" value={draft.title ?? ''} disabled={!editing} onChange={(title) => setDraft((current) => ({ ...current, title }))} />
    <TextField label="Overview" multiline value={draft.content?.overview ?? ''} disabled={!editing} onChange={(overview) => setDraft((current) => ({ ...current, content: { ...current.content, overview } }))} />
    {sections.map((section) => <TextField
      key={section}
      label={`${section.replace(/([A-Z])/g, ' $1')} (one item per line)`}
      multiline
      disabled={!editing}
      value={(draft.content?.[section] || []).join('\n')}
      onChange={(value) => setDraft((current) => ({ ...current, content: { ...current.content, [section]: value.split('\n').map((item) => item.trim()).filter(Boolean) } }))}
    />)}
  </div>
}

function TextField({ label, value, onChange, disabled = false, multiline = false, options }) {
  const className = 'mt-1 w-full rounded-lg border border-white/10 bg-slate-950/60 px-3 py-2 text-sm text-white outline-none focus:border-emerald-200/50 disabled:cursor-not-allowed disabled:opacity-60'
  return <label className="block text-xs text-slate-400">
    {label}
    {options ? <select disabled={disabled} value={value} onChange={(event) => onChange(event.target.value)} className={className}>{options.map((option) => <option key={option}>{option}</option>)}</select>
      : multiline ? <textarea disabled={disabled} rows={3} value={value} onChange={(event) => onChange(event.target.value)} className={className} />
        : <input disabled={disabled} value={value} onChange={(event) => onChange(event.target.value)} className={className} />}
  </label>
}
