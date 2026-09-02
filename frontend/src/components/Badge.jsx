const variants = {
  success: 'bg-green-100 text-green-800 border-green-200',
  warning: 'bg-yellow-100 text-yellow-800 border-yellow-200',
  danger: 'bg-red-50 text-red-700 border-red-200',
  info: 'bg-green-50 text-green-700 border-green-200',
  purple: 'bg-yellow-50 text-yellow-800 border-yellow-200',
  neutral: 'bg-slate-100 text-slate-700 border-slate-200',
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
