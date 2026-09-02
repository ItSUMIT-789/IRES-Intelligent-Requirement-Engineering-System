import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import GlassCard from '../GlassCard.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import { notificationService } from '../../services/notificationService.js'

const requirementRoutes = {
  ADMIN: '/admin/requirements/', CLIENT: '/client/requirements/',
  BUSINESS_ANALYST: '/analyst/requirements/', DEVELOPER: '/developer/requirements/',
}

export default function NotificationPage() {
  const { user } = useAuth(); const [items, setItems] = useState([]); const [error, setError] = useState(''); const [loading, setLoading] = useState(true)
  const load = () => notificationService.list({ size: 100, sort: 'createdAt,desc' }).then((r) => setItems(r.data.content || [])).catch((e) => setError(e.message)).finally(() => setLoading(false))
  useEffect(() => { load() }, [])
  const read = async (id) => { try { await notificationService.markRead(id); setItems((values) => values.map((x) => x.id === id ? { ...x, read: true } : x)) } catch (e) { setError(e.message) } }
  const readAll = async () => { try { await notificationService.markAllRead(); setItems((values) => values.map((x) => ({ ...x, read: true }))) } catch (e) { setError(e.message) } }
  return <><div className="mb-8 flex flex-wrap items-end justify-between gap-4"><div><span className="text-xs font-semibold uppercase tracking-widest text-blue-300">Workflow</span><h1 className="text-2xl font-bold text-white sm:text-3xl">Notifications</h1><p className="mt-1 text-sm text-slate-400">Persisted events addressed to your authenticated account.</p></div><button className="btn-outline" onClick={readAll} disabled={!items.some((x) => !x.read)}>Mark all read</button></div>
    {error && <GlassCard hover={false} className="mb-4 p-4 text-red-300">{error}</GlassCard>}{loading && <GlassCard hover={false} className="p-6 text-slate-400">Loading notifications…</GlassCard>}{!loading && items.length === 0 && <GlassCard hover={false} className="p-6 text-slate-400">No notifications yet.</GlassCard>}
    <div className="space-y-3">{items.map((item) => { const route = item.relatedRequirementId && requirementRoutes[user?.role]; return <GlassCard key={item.id} hover={false} className={`p-5 ${item.read ? 'opacity-70' : 'border-blue-400/30'}`}><div className="flex flex-wrap justify-between gap-4"><div className="min-w-0"><p className="font-semibold text-white">{item.title}</p><p className="mt-1 text-sm text-slate-300">{item.message}</p><p className="mt-2 text-xs text-slate-500">{new Date(item.createdAt).toLocaleString()} · {item.type}</p></div><div className="flex items-center gap-3">{route && <Link className="text-sm text-blue-300" to={`${route}${item.relatedRequirementId}`} onClick={() => !item.read && read(item.id)}>Open</Link>}{!item.read && <button className="text-sm text-slate-300 hover:text-white" onClick={() => read(item.id)}>Mark read</button>}</div></div></GlassCard> })}</div></>
}
