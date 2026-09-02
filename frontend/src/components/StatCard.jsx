import { motion } from 'framer-motion'
import GlassCard from './GlassCard.jsx'

export default function StatCard({ icon: Icon, label, value, delta, index = 0 }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.45, delay: index * 0.06, ease: 'easeOut' }}
    >
      <GlassCard className="p-5">
        <div className="flex items-start justify-between">
          <div>
            <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">{label}</p>
            <p className="mt-2 font-display text-2xl font-bold text-[#17211B] sm:text-3xl">{value}</p>
            {delta && <p className="mt-1 text-xs text-slate-500">{delta}</p>}
          </div>
          {Icon && (
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-green-200 bg-green-50">
              <Icon size={18} className="text-green-700" />
            </div>
          )}
        </div>
      </GlassCard>
    </motion.div>
  )
}
