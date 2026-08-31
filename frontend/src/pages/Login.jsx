import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Mail, Lock, LogIn, AlertCircle, Info } from 'lucide-react'
import AuthLayout from '../layouts/AuthLayout.jsx'
import InputField from '../components/InputField.jsx'
import GoogleButton from '../components/GoogleButton.jsx'
import GradientButton from '../components/GradientButton.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { authService } from '../services/authService.js'
import { getDashboardRoute } from '../utils/roleRoutes.js'
import { validateRequired } from '../utils/validators.js'

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' })
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [remember, setRemember] = useState(false)
  const [resetSent, setResetSent] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const update = (field) => (e) => {
    setForm((f) => ({ ...f, [field]: e.target.value }))
    setErrors((err) => ({ ...err, [field]: '' }))
    setFormError('')
  }

  const validate = () => {
    const next = {
      email: validateRequired(form.email, 'Email or username'),
      password: validateRequired(form.password, 'Password'),
    }
    setErrors(next)
    return Object.values(next).every((v) => !v)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setFormError('')
    if (!validate()) return

    setSubmitting(true)
    try {
      const session = await authService.login(form)
      login(session.user, session.token)
      navigate(getDashboardRoute(session.user.role), { replace: true })
    } catch (error) {
      setFormError(error.message)
    } finally {
      setSubmitting(false)
    }
  }

  const handleForgotPassword = (e) => {
    e.preventDefault()
    if (!form.email) {
      setErrors((err) => ({ ...err, email: 'Enter your email above first.' }))
      return
    }
    setResetSent(true)
  }

  return (
    <AuthLayout title="Welcome back" subtitle="Log in to continue analyzing your requirements.">
      <form className="space-y-5" onSubmit={handleSubmit} noValidate>
        <div>
          <InputField
            id="email"
            name="email"
            type="text"
            label="Email or username"
            icon={Mail}
            placeholder="you@company.com or username"
            value={form.email}
            onChange={update('email')}
          />
          {errors.email && <p className="mt-1.5 text-xs text-red-300">{errors.email}</p>}
        </div>

        <div>
          <InputField
            id="password"
            name="password"
            type="password"
            label="Password"
            icon={Lock}
            placeholder="••••••••"
            value={form.password}
            onChange={update('password')}
          />
          {errors.password && <p className="mt-1.5 text-xs text-red-300">{errors.password}</p>}
        </div>

        <div className="flex items-center justify-between text-sm">
          <label htmlFor="remember" className="flex cursor-pointer items-center gap-2 text-slate-400">
            <input
              id="remember"
              type="checkbox"
              checked={remember}
              onChange={() => setRemember((v) => !v)}
              className="h-4 w-4 rounded border-white/20 bg-white/5 accent-purple-500"
            />
            Remember me
          </label>
          <a href="#forgot-password" onClick={handleForgotPassword} className="font-medium text-blue-300 hover:text-blue-200">
            Forgot password?
          </a>
        </div>

        {resetSent && (
          <p className="flex items-center gap-2 rounded-xl border border-blue-500/20 bg-blue-500/10 px-4 py-3 text-sm text-blue-200">
            <Info size={15} className="shrink-0" />
            If an account exists for {form.email}, a reset link has been sent.
          </p>
        )}

        {formError && (
          <p className="flex items-center gap-2 rounded-xl border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-300">
            <AlertCircle size={15} className="shrink-0" />
            {formError}
          </p>
        )}

        <GradientButton type="submit" icon={LogIn} className="w-full" disabled={submitting}>
          {submitting ? 'Logging in…' : 'Login'}
        </GradientButton>

        <div className="relative py-2 text-center">
          <span className="relative z-10 bg-transparent px-3 text-xs text-slate-500">
            <span className="glass-strong rounded-full px-3 py-1">or</span>
          </span>
          <div className="absolute inset-x-0 top-1/2 -z-0 h-px bg-white/10" />
        </div>

        <GoogleButton label="Sign in with Google" />

        <p className="pt-2 text-center text-sm text-slate-400">
          Don&apos;t have an account?{' '}
          <Link to="/register" className="font-semibold text-purple-300 hover:text-purple-200">
            Register
          </Link>
        </p>

      </form>
    </AuthLayout>
  )
}
