import { Bell, Search, Menu } from 'lucide-react'
import { useAuth } from '../context/AuthContext.jsx'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { notificationService } from '../services/notificationService.js'

export default function Topbar({ onMenuClick }) {
  const { user } = useAuth()
  const navigate = useNavigate(); const [unread, setUnread] = useState(0)
  useEffect(() => { notificationService.unreadCount().then((r) => setUnread(r.data || 0)).catch(() => setUnread(0)) }, [user?.id])
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
    <header className="flex items-center justify-between gap-4 border-b border-slate-200 bg-white px-5 py-4 lg:px-8">
      <div className="flex items-center gap-3">
        <button
          onClick={onMenuClick}
          className="rounded-lg p-2 text-slate-600 hover:bg-green-50 lg:hidden"
          aria-label="Open menu"
        >
          <Menu size={20} />
        </button>
        <div className="relative hidden sm:block">
          <Search size={15} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
          <input
            type="text"
            placeholder="Search requirements, stories, reports…"
            className="w-72 rounded-lg border border-slate-200 bg-slate-50 py-2 pl-9 pr-3 text-sm text-slate-800 placeholder:text-slate-400 outline-none focus:border-green-600 focus:ring-2 focus:ring-green-100"
          />
        </div>
      </div>

      <div className="flex items-center gap-4">
        <button onClick={() => navigate(`/${user?.role === 'BUSINESS_ANALYST' ? 'analyst' : user?.role?.toLowerCase()}/notifications`)} className="relative rounded-lg p-2 text-slate-600 hover:bg-green-50 hover:text-green-700" aria-label={`${unread} unread notifications`}>
          <Bell size={19} />
          {unread > 0 && <span className="absolute -right-1 -top-1 min-w-5 rounded-full bg-yellow-400 px-1 text-center text-[10px] font-bold leading-5 text-slate-900">{unread > 99 ? '99+' : unread}</span>}
        </button>
        <div className="flex items-center gap-2.5">
          <div className="flex h-9 w-9 items-center justify-center rounded-full bg-green-700 text-xs font-semibold text-white">
            {initials || 'U'}
          </div>
          <div className="hidden sm:block">
            <p className="text-sm font-medium leading-tight text-slate-900">{name}</p>
            <p className="text-xs leading-tight text-slate-500">{roleLabel}</p>
          </div>
        </div>
      </div>
    </header>
  )
}
