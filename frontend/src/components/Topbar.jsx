import { Bell, Search, Menu } from 'lucide-react'
import { useAuth } from '../context/AuthContext.jsx'

export default function Topbar({ onMenuClick }) {
  const { user } = useAuth()
  const name = user?.name || 'User'
  const roleLabel = user?.roleLabel || ''
  const initials = name
    .split(' ')
    .filter(Boolean)
    .map((n) => n[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()

  return (
    <header className="flex items-center justify-between gap-4 border-b border-white/10 px-5 py-4 lg:px-8">
      <div className="flex items-center gap-3">
        <button
          onClick={onMenuClick}
          className="rounded-lg p-2 text-slate-300 hover:bg-white/5 lg:hidden"
          aria-label="Open menu"
        >
          <Menu size={20} />
        </button>
        <div className="relative hidden sm:block">
          <Search size={15} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
          <input
            type="text"
            placeholder="Search requirements, stories, reports…"
            className="w-72 rounded-xl border border-white/10 bg-white/[0.04] py-2 pl-9 pr-3 text-sm text-slate-200 placeholder:text-slate-500 outline-none focus:border-blue-400/60 focus:ring-2 focus:ring-blue-500/20"
          />
        </div>
      </div>

      <div className="flex items-center gap-4">
        <button className="relative rounded-lg p-2 text-slate-300 hover:bg-white/5" aria-label="Notifications">
          <Bell size={19} />
          <span className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-purple-400" />
        </button>
        <div className="flex items-center gap-2.5">
          <div className="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br from-blue-500 to-purple-500 text-xs font-semibold text-white">
            {initials || 'U'}
          </div>
          <div className="hidden sm:block">
            <p className="text-sm font-medium leading-tight text-white">{name}</p>
            <p className="text-xs leading-tight text-slate-400">{roleLabel}</p>
          </div>
        </div>
      </div>
    </header>
  )
}
