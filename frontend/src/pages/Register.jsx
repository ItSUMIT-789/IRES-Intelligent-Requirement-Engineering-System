import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { User, Mail, Lock, KeyRound, UserPlus, ChevronDown, AlertCircle } from 'lucide-react'
import AuthLayout from '../layouts/AuthLayout.jsx'
import InputField from '../components/InputField.jsx'
import GoogleButton from '../components/GoogleButton.jsx'
import GradientButton from '../components/GradientButton.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { authService } from '../services/authService.js'
import { getDashboardRoute, registrationRoles } from '../utils/roleRoutes.js'
import { validateEmail, validatePassword, validateConfirmPassword, validateRequired } from '../utils/validators.js'

const initialForm = { fullName: '', email: '', password: '', confirmPassword: '', role: '' }

export default function Register() {
  const [form, setForm] = useState(initialForm)
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const update = (field) => (e) => {
    setForm((f) => ({ ...f, [field]: e.target.value }))
    setErrors((err) => ({ ...err, [field]: '' }))
  }

  const validate = () => {
    const next = {
      fullName: validateRequired(form.fullName, 'Full name'),
      email: validateEmail(form.email),
      password: validatePassword(form.password),
      confirmPassword: validateConfirmPassword(form.password, form.confirmPassword),
      role: validateRequired(form.role, 'Role'),
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
      const nameParts = form.fullName.trim().split(/\s+/)
      const firstName = nameParts.shift()
      const lastName = nameParts.join(' ') || firstName
      await authService.register({ firstName, lastName, email: form.email, password: form.password, role: form.role })
      const session = await authService.login({ email: form.email, password: form.password })
      login(session.user, session.token)
      navigate(getDashboardRoute(session.user.role), { replace: true })
    } catch (error) {
      setFormError(error.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout title="Create your account" subtitle="Join IRES and start engineering clearer requirements.">
      <form className="space-y-5" onSubmit={handleSubmit} noValidate>
        <div>
          <InputField
            id="fullName"
            name="fullName"
            type="text"
            label="Full Name"
            icon={User}
            placeholder="Jane Doe"
            value={form.fullName}
            onChange={update('fullName')}
          />
          {errors.fullName && <p className="mt-1.5 text-xs text-red-300">{errors.fullName}</p>}
        </div>

        <div>
          <InputField
            id="email"
            name="email"
            type="email"
            label="Email"
            icon={Mail}
            placeholder="you@company.com"
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
            placeholder="At least 8 characters, 1 number"
            value={form.password}
            onChange={update('password')}
          />
          {errors.password && <p className="mt-1.5 text-xs text-red-300">{errors.password}</p>}
        </div>

        <div>
          <InputField
            id="confirmPassword"
            name="confirmPassword"
            type="password"
            label="Confirm Password"
            icon={KeyRound}
            placeholder="••••••••"
            value={form.confirmPassword}
            onChange={update('confirmPassword')}
          />
          {errors.confirmPassword && <p className="mt-1.5 text-xs text-red-300">{errors.confirmPassword}</p>}
        </div>

        <div>
          <label htmlFor="role" className="mb-1.5 block text-sm font-medium text-slate-300">
            Role
          </label>
          <div className="group relative">
            <UserPlus
              size={17}
              className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500 transition-colors group-focus-within:text-blue-300"
            />
            <select
              id="role"
              name="role"
              value={form.role}
              onChange={update('role')}
              className="w-full appearance-none rounded-xl border border-white/10 bg-white/[0.04] py-2.5 pl-10 pr-10 text-sm text-slate-100
                outline-none transition-all duration-200 focus:border-blue-400/60 focus:bg-white/[0.07] focus:ring-2 focus:ring-blue-500/20"
            >
              <option value="" disabled className="bg-space-900">
                Select your role
              </option>
              {registrationRoles.map((role) => (
                <option key={role.value} value={role.value} className="bg-space-900">
                  {role.label}
                </option>
              ))}
            </select>
            <ChevronDown
              size={16}
              className="pointer-events-none absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-500"
            />
          </div>
          {errors.role && <p className="mt-1.5 text-xs text-red-300">{errors.role}</p>}
        </div>

        {formError && (
          <p className="flex items-center gap-2 rounded-xl border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-300">
            <AlertCircle size={15} className="shrink-0" />
            {formError}
          </p>
        )}

        <GradientButton type="submit" icon={UserPlus} className="w-full" disabled={submitting}>
          {submitting ? 'Creating account…' : 'Register'}
        </GradientButton>

        <div className="relative py-2 text-center">
          <span className="relative z-10 bg-transparent px-3 text-xs text-slate-500">
            <span className="glass-strong rounded-full px-3 py-1">or</span>
          </span>
          <div className="absolute inset-x-0 top-1/2 -z-0 h-px bg-white/10" />
        </div>

        <GoogleButton label="Sign up with Google" />

        <p className="pt-2 text-center text-sm text-slate-400">
          Already have an account?{' '}
          <Link to="/login" className="font-semibold text-purple-300 hover:text-purple-200">
            Login
          </Link>
        </p>
      </form>
    </AuthLayout>
  )
}
