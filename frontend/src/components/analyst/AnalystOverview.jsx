import { useEffect, useState } from 'react'
import { Activity, BrainCircuit, ClipboardCheck, ListChecks, Send } from 'lucide-react'
import GlassCard from '../GlassCard.jsx'
import StatCard from '../StatCard.jsx'
import { dashboardService } from '../../services/dashboardService.js'
import { AnalystHeader } from '../../pages/dashboards/AnalystDashboard.jsx'
const icons = [ListChecks, Activity, ClipboardCheck, Send, BrainCircuit]
export default function AnalystOverview() {
  const [summary, setSummary] = useState(); const [error, setError] = useState('')
  useEffect(() => { dashboardService.getSummary().then((r) => setSummary(r.data)).catch((e) => setError(e.message)) }, [])
  return <><AnalystHeader title="Analysis Dashboard" subtitle="Live requirements awaiting refinement and handoff." />{error && <GlassCard className="p-5"><p className="text-red-300">{error}</p></GlassCard>}{!summary && !error && <GlassCard className="p-5"><p className="text-slate-400">Loading dashboard…</p></GlassCard>}{summary && <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-3">{summary.metrics.map((metric, index) => <StatCard key={metric.key} icon={icons[index]} label={metric.label} value={metric.value} delta={metric.value === 0 ? metric.emptyMessage : undefined} index={index} />)}</div>}</>
}
