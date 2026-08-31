import { Link, useNavigate } from 'react-router-dom'
import { LogOut, Sparkles } from 'lucide-react'
import { useAuth } from '../context/AuthContext.jsx'

export default function Sidebar({ items, active, onSelect }) {
  const { logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <aside className="flex h-full w-64 shrink-0 flex-col border-r border-white/10 p-5">
      <Link to="/" className="mb-8 flex items-center gap-2">
        <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-purple-500 shadow-glow">
          <Sparkles size={18} className="text-white" />
        </span>
        <span className="font-display text-lg font-semibold text-white">
          IRES <span className="gradient-text">AI</span>
        </span>
      </Link>

      <nav className="flex-1 space-y-1.5 overflow-y-auto">
        {items.map(({ key, icon: Icon, label }) => (
          <button
            key={key}
            type="button"
            onClick={() => onSelect(key)}
            className={`flex w-full items-center gap-3 rounded-xl px-3.5 py-2.5 text-left text-sm font-medium transition-all ${
              active === key
                ? 'glass text-white shadow-glow'
                : 'text-slate-400 hover:bg-white/5 hover:text-slate-200'
            }`}
          >
            <Icon size={17} />
            {label}
          </button>
        ))}
      </nav>

      <div className="space-y-1.5 border-t border-white/10 pt-4">
        <button
          type="button"
          onClick={handleLogout}
          className="flex w-full items-center gap-3 rounded-xl px-3.5 py-2.5 text-left text-sm font-medium text-slate-400 hover:bg-white/5 hover:text-red-300"
        >
          <LogOut size={17} />
          Log out
        </button>
      </div>
    </aside>
  )
}
