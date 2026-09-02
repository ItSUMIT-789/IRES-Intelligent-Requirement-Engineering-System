import { Link, useNavigate } from 'react-router-dom'
import { LogOut, Leaf } from 'lucide-react'
import { useAuth } from '../context/AuthContext.jsx'

export default function Sidebar({ items, active, onSelect }) {
  const { logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <aside className="flex h-full w-64 shrink-0 flex-col border-r border-slate-200 bg-white p-5">
      <Link to="/" className="mb-8 flex items-center gap-2">
        <span className="relative flex h-9 w-9 items-center justify-center rounded-lg bg-green-700">
          <Leaf size={18} className="text-white" />
          <span className="absolute -right-1 -top-1 h-3 w-3 rounded-full border-2 border-white bg-yellow-400" />
        </span>
        <span className="font-display text-lg font-semibold text-[#17211B]">
          IRES <span className="text-green-700">AI</span>
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
                ? 'border-l-4 border-yellow-400 bg-green-50 pl-2.5 font-semibold text-green-800'
                : 'border-l-4 border-transparent text-slate-600 hover:bg-green-50 hover:text-green-700'
            }`}
          >
            <Icon size={17} />
            {label}
          </button>
        ))}
      </nav>

      <div className="space-y-1.5 border-t border-slate-200 pt-4">
        <button
          type="button"
          onClick={handleLogout}
          className="flex w-full items-center gap-3 rounded-xl px-3.5 py-2.5 text-left text-sm font-medium text-slate-600 hover:bg-red-50 hover:text-red-700"
        >
          <LogOut size={17} />
          Log out
        </button>
      </div>
    </aside>
  )
}
