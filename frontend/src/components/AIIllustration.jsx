import { motion } from 'framer-motion'
import { FileText, CheckCircle2, Brain, ListChecks } from 'lucide-react'

// A layered "AI reading a requirements document" scene, built from glass
// panels and lucide icons rather than a static image, so it lives inside the
// aurora background instead of sitting on top of it.
export default function AIIllustration() {
  return (
    <div className="relative mx-auto w-full max-w-md aspect-square">
      {/* orbiting glow ring */}
      <div className="absolute inset-6 rounded-full border border-white/10" />
      <div className="absolute inset-14 rounded-full border border-white/5" />

      {/* central AI core */}
      <motion.div
        animate={{ scale: [1, 1.05, 1] }}
        transition={{ duration: 4, repeat: Infinity, ease: 'easeInOut' }}
        className="absolute left-1/2 top-1/2 flex h-28 w-28 -translate-x-1/2 -translate-y-1/2 items-center justify-center rounded-full bg-gradient-to-br from-blue-500 to-purple-500 shadow-glow animate-pulse-glow"
      >
        <Brain size={40} className="text-white" />
      </motion.div>

      {/* document card 1 */}
      <motion.div
        animate={{ y: [0, -14, 0] }}
        transition={{ duration: 5, repeat: Infinity, ease: 'easeInOut' }}
        className="absolute left-0 top-6 w-40"
      >
        <div className="glass rounded-xl p-3 shadow-lg">
          <div className="flex items-center gap-2 mb-2">
            <FileText size={14} className="text-blue-300" />
            <span className="text-[11px] font-medium text-slate-200">Requirement.doc</span>
          </div>
          <div className="space-y-1.5">
            <div className="h-1.5 w-full rounded-full bg-white/10" />
            <div className="h-1.5 w-4/5 rounded-full bg-white/10" />
            <div className="h-1.5 w-2/3 rounded-full bg-aurora-cyan/50" />
          </div>
        </div>
      </motion.div>

      {/* classification card */}
      <motion.div
        animate={{ y: [0, 12, 0] }}
        transition={{ duration: 6, repeat: Infinity, ease: 'easeInOut', delay: 0.5 }}
        className="absolute right-0 top-14 w-36"
      >
        <div className="glass rounded-xl p-3 shadow-lg">
          <div className="flex items-center gap-2 mb-2">
            <ListChecks size={14} className="text-purple-300" />
            <span className="text-[11px] font-medium text-slate-200">Functional</span>
          </div>
          <span className="inline-block rounded-full bg-purple-500/20 px-2 py-0.5 text-[10px] text-purple-200">
            classified
          </span>
        </div>
      </motion.div>

      {/* validated user story card */}
      <motion.div
        animate={{ y: [0, -10, 0] }}
        transition={{ duration: 5.5, repeat: Infinity, ease: 'easeInOut', delay: 1 }}
        className="absolute bottom-4 left-4 w-44"
      >
        <div className="glass rounded-xl p-3 shadow-lg">
          <div className="flex items-center gap-2 mb-1.5">
            <CheckCircle2 size={14} className="text-emerald-300" />
            <span className="text-[11px] font-medium text-slate-200">User story generated</span>
          </div>
          <p className="text-[10px] leading-snug text-slate-400">
            "As a user, I want to reset my password securely."
          </p>
        </div>
      </motion.div>

      {/* connecting lines */}
      <svg className="absolute inset-0 h-full w-full" viewBox="0 0 400 400" fill="none">
        <line x1="90" y1="90" x2="190" y2="190" stroke="url(#lineGrad)" strokeWidth="1.5" strokeDasharray="4 4" />
        <line x1="310" y1="120" x2="210" y2="200" stroke="url(#lineGrad)" strokeWidth="1.5" strokeDasharray="4 4" />
        <line x1="110" y1="320" x2="200" y2="220" stroke="url(#lineGrad)" strokeWidth="1.5" strokeDasharray="4 4" />
        <defs>
          <linearGradient id="lineGrad" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#3b82f6" stopOpacity="0.6" />
            <stop offset="100%" stopColor="#a855f7" stopOpacity="0.6" />
          </linearGradient>
        </defs>
      </svg>
    </div>
  )
}
