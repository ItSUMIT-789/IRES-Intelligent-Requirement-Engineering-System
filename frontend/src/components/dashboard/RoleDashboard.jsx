import { useEffect, useMemo, useState } from 'react'
import { motion } from 'framer-motion'
import { Activity, Bug, ClipboardCheck, FileText, FolderKanban, ListChecks, Users } from 'lucide-react'
import DashboardLayout from '../../layouts/DashboardLayout.jsx'
import GlassCard from '../GlassCard.jsx'
import StatCard from '../StatCard.jsx'
import ProfilePanel from './ProfilePanel.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import { getRoleNavigation } from '../../config/roleNavigation.js'
import { dashboardService } from '../../services/dashboardService.js'
import RequirementWorkflowPanel from './RequirementWorkflowPanel.jsx'
import AdminUsersPanel from './AdminUsersPanel.jsx'

const metricIcons = [Users, FolderKanban, FileText, ListChecks, Activity, Bug, ClipboardCheck]
const descriptions = {
  ADMIN: 'System-wide delivery and requirement engineering health.',
  CLIENT: 'The latest status of your projects and requirements.',
  BUSINESS_ANALYST: 'Requirements awaiting analysis, refinement, and approval.',
  DEVELOPER: 'Your assigned development work, testing handoff, and bugs.',
  TESTER: 'Your assigned test cases, executions, and reported bugs.',
}

export default function RoleDashboard() {
  const { user } = useAuth()
  const [active, setActive] = useState('overview')
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const items = useMemo(() => getRoleNavigation(user?.role), [user?.role])

  const load = async () => {
    setLoading(true); setError('')
    try {
      const responseSummary = (await dashboardService.getSummary()).data
      if (responseSummary?.role !== user?.role) {
        throw new Error('Dashboard role does not match the authenticated account. Please sign out and sign in again.')
      }
      setSummary(responseSummary)
    }
    catch (requestError) { setError(requestError.message || 'Dashboard data could not be loaded.') }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [user?.role])

  return (
    <DashboardLayout items={items} active={active} onSelect={setActive}>
      <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }}>
        {active === 'overview' ? <Overview user={user} summary={summary} loading={loading} error={error} onRetry={load} />
          : active === 'users' && user?.role === 'ADMIN' ? <AdminUsersPanel />
          : active === 'profile' ? <ProfilePanel />
          : ['requirements', 'queue', 'analysis'].includes(active) ? <RequirementWorkflowPanel title={items.find((item) => item.key === active)?.label} />
          : <SectionState item={items.find((item) => item.key === active)} />}
      </motion.div>
    </DashboardLayout>
  )
}

function Overview({ user, summary, loading, error, onRetry }) {
  const fullName = [user?.firstName, user?.lastName].filter(Boolean).join(' ') || user?.name || 'User'
  return <>
    <Header role={user?.roleLabel} title={`Welcome back, ${fullName}`} subtitle={descriptions[user?.role]} />
    {loading && <StateCard message="Loading dashboard data…" />}
    {error && <StateCard message={error} action="Try again" onAction={onRetry} tone="error" />}
    {!loading && !error && summary && <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-3">
      {summary.metrics.map((metric, index) => <StatCard key={metric.key} icon={metricIcons[index % metricIcons.length]}
        label={metric.label} value={metric.available ? metric.value : 'Unavailable'}
        delta={metric.available && metric.value === 0 ? metric.emptyMessage : (!metric.available ? metric.emptyMessage : undefined)} index={index} />)}
    </div>}
  </>
}

function SectionState({ item }) {
  return <><Header title={item?.label || 'Dashboard'} subtitle="This section is waiting for its dedicated backend workflow API." />
    <StateCard message="No live data endpoint is available for this section yet. No demo data is being shown." /></>
}

function StateCard({ message, action, onAction, tone }) {
  return <GlassCard hover={false} className={`p-6 ${tone === 'error' ? 'border-red-400/20' : ''}`}>
    <p className={tone === 'error' ? 'text-red-300' : 'text-slate-400'}>{message}</p>
    {action && <button type="button" onClick={onAction} className="mt-4 rounded-lg bg-blue-500/20 px-4 py-2 text-sm text-blue-200 hover:bg-blue-500/30">{action}</button>}
  </GlassCard>
}

function Header({ role, title, subtitle }) {
  return <div className="mb-8 flex flex-col gap-1">{role && <span className="text-xs font-semibold uppercase tracking-widest text-blue-300">{role}</span>}
    <h1 className="font-display text-2xl font-bold text-white sm:text-3xl">{title}</h1><p className="text-sm text-slate-400">{subtitle}</p></div>
}
