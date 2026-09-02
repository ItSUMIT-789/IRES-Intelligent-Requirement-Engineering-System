import { useEffect, useState } from 'react'
import { ClipboardCheck, FileText, FolderKanban, ListChecks, Activity } from 'lucide-react'
import { useAuth } from '../../context/AuthContext.jsx'
import { dashboardService } from '../../services/dashboardService.js'
import StatCard from '../StatCard.jsx'
import GlassCard from '../GlassCard.jsx'
import { ClientHeader } from '../../pages/dashboards/ClientDashboard.jsx'
const icons = [FolderKanban, FileText, ListChecks, Activity, ClipboardCheck]
export default function ClientOverview() {
  const { user } = useAuth(); const [summary, setSummary] = useState(); const [error, setError] = useState('')
  useEffect(() => { dashboardService.getSummary().then((r) => setSummary(r.data)).catch((e) => setError(e.message)) }, [])
  return <><ClientHeader title={`Welcome back, ${user?.name || 'Client'}`} subtitle="Live project and requirement delivery status." />{error && <GlassCard className="p-5"><p className="text-red-300">{error}</p></GlassCard>}{!summary && !error && <GlassCard className="p-5"><p className="text-slate-400">Loading dashboard…</p></GlassCard>}{summary && <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-3">{summary.metrics.map((metric, index) => <StatCard key={metric.key} icon={icons[index]} label={metric.label} value={metric.value} delta={metric.value === 0 ? metric.emptyMessage : undefined} index={index} />)}</div>}</>
}
