import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Sparkles, ShieldCheck, Zap, FileSearch } from 'lucide-react'

const highlights = [
  { icon: FileSearch, text: 'Parses raw requirement documents in seconds' },
  { icon: ShieldCheck, text: 'Flags ambiguity before it reaches development' },
  { icon: Zap, text: 'Generates traceable, ready-to-build user stories' },
]

export default function AuthLayout({ children, title, subtitle }) {
  return (
    <div className="relative flex min-h-screen items-stretch">
      {/* left panel */}
      <div className="relative hidden w-1/2 flex-col justify-between overflow-hidden border-r border-green-100 bg-green-50 p-12 lg:flex">

        <Link to="/" className="relative z-10 flex items-center gap-2">
          <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-green-700">
            <Sparkles size={18} className="text-white" />
          </span>
          <span className="font-display text-lg font-semibold text-white">
            IRES <span className="gradient-text">AI</span>
          </span>
        </Link>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7 }}
          className="relative z-10"
        >
          <h2 className="font-display text-3xl font-bold text-white leading-tight mb-4">
            Requirements engineering,
            <br /> guided by AI.
          </h2>
          <div className="space-y-4 mt-8">
            {highlights.map(({ icon: Icon, text }, i) => (
              <motion.div
                key={text}
                initial={{ opacity: 0, x: -16 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.2 + i * 0.15, duration: 0.5 }}
                className="glass flex items-center gap-3 rounded-xl px-4 py-3"
              >
                <Icon size={18} className="shrink-0 text-green-700" />
                <span className="text-sm text-slate-200">{text}</span>
              </motion.div>
            ))}
          </div>
        </motion.div>

        <p className="relative z-10 text-xs text-slate-400">
          © {new Date().getFullYear()} IRES AI. All rights reserved.
        </p>
      </div>

      {/* right panel */}
      <div className="flex w-full items-center justify-center px-6 py-16 lg:w-1/2">
        <motion.div
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, ease: 'easeOut' }}
          className="glass-strong w-full max-w-md rounded-xl p-8 sm:p-10"
        >
          <Link to="/" className="mb-8 flex items-center gap-2 lg:hidden">
            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-green-700">
              <Sparkles size={16} className="text-white" />
            </span>
            <span className="font-display text-base font-semibold text-white">
              IRES <span className="gradient-text">AI</span>
            </span>
          </Link>

          <h1 className="font-display text-2xl font-bold text-white sm:text-3xl">{title}</h1>
          {subtitle && <p className="mt-2 text-sm text-slate-400">{subtitle}</p>}

          <div className="mt-8">{children}</div>
        </motion.div>
      </div>
    </div>
  )
}
