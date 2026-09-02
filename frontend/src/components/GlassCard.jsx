import { motion } from 'framer-motion'

export default function GlassCard({ children, className = '', strong = false, hover = true, ...props }) {
  return (
    <motion.div
      className={`${strong ? 'glass-strong' : 'glass'} rounded-xl ${
        hover ? 'transition-colors duration-200 hover:border-green-200' : ''
      } ${className}`}
      {...props}
    >
      {children}
    </motion.div>
  )
}
