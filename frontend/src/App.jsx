import { lazy, Suspense } from 'react'
import { Routes, Route, Navigate, useLocation } from 'react-router-dom'
import { AnimatePresence } from 'framer-motion'
import Landing from './pages/Landing.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import Unauthorized from './pages/Unauthorized.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import AuroraBackground from './components/AuroraBackground.jsx'

const AdminDashboard = lazy(() => import('./pages/dashboards/AdminDashboard.jsx'))
const AnalystDashboard = lazy(() => import('./pages/dashboards/AnalystDashboard.jsx'))
const ClientDashboard = lazy(() => import('./pages/dashboards/ClientDashboard.jsx'))
const DeveloperDashboard = lazy(() => import('./pages/dashboards/DeveloperDashboard.jsx'))
const TesterDashboard = lazy(() => import('./pages/dashboards/TesterDashboard.jsx'))

function RouteFallback() {
  return (
    <div className="flex min-h-screen items-center justify-center" role="status" aria-live="polite">
      <div className="flex items-center gap-3 text-sm text-slate-300">
        <span className="h-5 w-5 animate-spin rounded-full border-2 border-white/20 border-t-purple-400" />
        Loading dashboard…
      </div>
    </div>
  )
}

export default function App() {
  const location = useLocation()

  return (
    <div className="relative min-h-screen overflow-x-hidden">
      <AuroraBackground />
      <AnimatePresence mode="wait">
        <Suspense fallback={<RouteFallback />}>
          <Routes location={location} key={location.pathname}>
            <Route path="/" element={<Landing />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/unauthorized" element={<Unauthorized />} />

          <Route
            path="/admin/dashboard"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <AdminDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/analyst/dashboard"
            element={
              <ProtectedRoute allowedRoles={['BUSINESS_ANALYST']}>
                <AnalystDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/client/dashboard"
            element={
              <ProtectedRoute allowedRoles={['CLIENT']}>
                <ClientDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/developer/dashboard"
            element={
              <ProtectedRoute allowedRoles={['DEVELOPER']}>
                <DeveloperDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/tester/dashboard"
            element={
              <ProtectedRoute allowedRoles={['TESTER']}>
                <TesterDashboard />
              </ProtectedRoute>
            }
          />

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Suspense>
      </AnimatePresence>
    </div>
  )
}
