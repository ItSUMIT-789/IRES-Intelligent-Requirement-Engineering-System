import { CheckCircle2, XCircle, MessageSquare, Sparkles } from 'lucide-react'
import GlassCard from '../GlassCard.jsx'

const iconByType = {
  approved: { icon: CheckCircle2, className: 'text-emerald-300 bg-emerald-500/15' },
  rejected: { icon: XCircle, className: 'text-red-300 bg-red-500/15' },
  comment: { icon: MessageSquare, className: 'text-blue-300 bg-blue-500/15' },
  ai: { icon: Sparkles, className: 'text-purple-300 bg-purple-500/15' },
}

// notifications: [{ type: 'approved'|'rejected'|'comment'|'ai', title, description, time }]
export default function NotificationsPanel({ notifications }) {
  return (
    <GlassCard hover={false} className="p-6">
      <h3 className="mb-5 font-display text-lg font-semibold text-white">Notifications</h3>
      <ul className="space-y-3">
        {notifications.map((n, i) => {
          const cfg = iconByType[n.type] || iconByType.comment
          const Icon = cfg.icon
          return (
            <li
              key={i}
              className="flex items-start gap-3 rounded-xl border border-white/10 bg-white/[0.02] px-4 py-3.5"
            >
              <span className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-xl ${cfg.className}`}>
                <Icon size={16} />
              </span>
              <div className="min-w-0">
                <p className="text-sm font-medium text-white">{n.title}</p>
                <p className="mt-0.5 text-xs text-slate-400">{n.description}</p>
                <p className="mt-1 text-[11px] text-slate-500">{n.time}</p>
              </div>
            </li>
          )
        })}
      </ul>
    </GlassCard>
  )
}
