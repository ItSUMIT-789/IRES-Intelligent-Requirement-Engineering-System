import GlassCard from '../GlassCard.jsx'
import { ClientHeader } from '../../pages/dashboards/ClientDashboard.jsx'
export default function ClientPlaceholder({ title, message }) { return <><ClientHeader title={title} /><GlassCard hover={false} className="p-6"><p className="text-slate-400">{message}</p></GlassCard></> }
