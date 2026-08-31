import { motion } from 'framer-motion'

export default function GlassCard({ children, className = '', strong = false, hover = true, ...props }) {
  return (
    <motion.div
      className={`${strong ? 'glass-strong' : 'glass'} rounded-2xl shadow-xl shadow-black/20 ${
        hover ? 'transition-transform duration-300 hover:-translate-y-1' : ''
      } ${className}`}
      {...props}
    >
      {children}
    </motion.div>
  )
}
