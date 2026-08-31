import { motion } from 'framer-motion'
import GlassCard from './GlassCard.jsx'

export default function FeatureCard({ icon: Icon, title, description, index = 0 }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 30 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.3 }}
      transition={{ duration: 0.5, delay: index * 0.08, ease: 'easeOut' }}
    >
      <GlassCard className="group h-full p-6">
        <div className="mb-5 flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500/20 to-purple-500/20 border border-white/10 transition-all duration-300 group-hover:from-blue-500 group-hover:to-purple-500 group-hover:shadow-glow">
          <Icon size={22} className="text-blue-300 transition-colors group-hover:text-white" />
        </div>
        <h3 className="font-display text-lg font-semibold text-white mb-2">{title}</h3>
        <p className="text-sm leading-relaxed text-slate-400">{description}</p>
      </GlassCard>
    </motion.div>
  )
}
