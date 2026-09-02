import { AtSign, CheckCircle2, Mail, Shield } from 'lucide-react'
import { useAuth } from '../../context/AuthContext.jsx'
import GlassCard from '../GlassCard.jsx'

export default function ProfilePanel() {
  const { user } = useAuth()
  const name = user?.name || 'User'
  const email = user?.email || 'unknown@example.com'
  const roleLabel = user?.roleLabel || '—'
  const initials = name
    .split(' ')
    .filter(Boolean)
    .map((n) => n[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()

  return (
    <GlassCard hover={false} className="max-w-xl p-8">
      <div className="flex items-center gap-4">
        <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br from-blue-500 to-purple-500 text-lg font-semibold text-white shadow-glow">
          {initials || 'U'}
        </div>
        <div>
          <h3 className="font-display text-xl font-semibold text-white">{name}</h3>
          <p className="text-sm text-slate-400">{roleLabel}</p>
        </div>
      </div>

      <div className="mt-6 space-y-3">
        <div className="flex items-center gap-3 rounded-xl border border-white/10 bg-white/[0.03] px-4 py-3">
          <Mail size={16} className="text-blue-300" />
          <span className="text-sm text-slate-200">{email}</span>
        </div>
        <div className="flex items-center gap-3 rounded-xl border border-white/10 bg-white/[0.03] px-4 py-3">
          <Shield size={16} className="text-purple-300" />
          <span className="text-sm text-slate-200">Signed in as {roleLabel}</span>
        </div>
        <div className="flex items-center gap-3 rounded-xl border border-white/10 bg-white/[0.03] px-4 py-3">
          <AtSign size={16} className="text-blue-300" />
          <span className="text-sm text-slate-200">{user?.username || 'No username configured'}</span>
        </div>
        <div className="flex items-center gap-3 rounded-xl border border-white/10 bg-white/[0.03] px-4 py-3">
          <CheckCircle2 size={16} className={user?.active === false ? 'text-red-300' : 'text-emerald-300'} />
          <span className="text-sm text-slate-200">Account {user?.active === false ? 'inactive' : 'active'}</span>
        </div>
      </div>
    </GlassCard>
  )
}
