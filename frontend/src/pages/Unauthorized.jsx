import { useNavigate } from 'react-router-dom'
import { ShieldAlert } from 'lucide-react'
import { useAuth } from '../context/AuthContext.jsx'
import { getDashboardRoute, getRoleLabel } from '../utils/roleRoutes.js'
import GradientButton from '../components/GradientButton.jsx'
import GlassCard from '../components/GlassCard.jsx'

export default function Unauthorized() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="flex min-h-screen items-center justify-center px-6">
      <GlassCard hover={false} className="w-full max-w-md p-10 text-center">
        <div className="mx-auto mb-5 flex h-14 w-14 items-center justify-center rounded-2xl border border-white/10 bg-gradient-to-br from-red-500/20 to-orange-500/20">
          <ShieldAlert size={26} className="text-red-300" />
        </div>
        <h1 className="font-display text-2xl font-bold text-white">403 — Access denied</h1>
        <p className="mt-3 text-sm leading-relaxed text-slate-400">
          {user
            ? `Your account is registered as ${getRoleLabel(user.role)}, so this dashboard isn't available to you.`
            : 'You need to be logged in to view this page.'}
        </p>
        <div className="mt-7 flex flex-col gap-3 sm:flex-row sm:justify-center">
          {user ? (
            <>
              <GradientButton to={getDashboardRoute(user.role)}>Go to my dashboard</GradientButton>
              <button onClick={handleLogout} className="btn-outline">
                Log out
              </button>
            </>
          ) : (
            <GradientButton to="/login">Go to login</GradientButton>
          )}
        </div>
      </GlassCard>
    </div>
  )
}
