const variants = {
  success: 'bg-emerald-500/15 text-emerald-300 border-emerald-500/20',
  warning: 'bg-amber-500/15 text-amber-300 border-amber-500/20',
  danger: 'bg-red-500/15 text-red-300 border-red-500/20',
  info: 'bg-blue-500/15 text-blue-300 border-blue-500/20',
  purple: 'bg-purple-500/15 text-purple-300 border-purple-500/20',
  neutral: 'bg-white/10 text-slate-300 border-white/10',
}

export default function Badge({ children, variant = 'neutral', className = '' }) {
  return (
    <span
      className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium ${variants[variant] || variants.neutral} ${className}`}
    >
      {children}
    </span>
  )
}
