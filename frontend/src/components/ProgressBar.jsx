export default function ProgressBar({ value, className = '' }) {
  const pct = Math.max(0, Math.min(100, value))
  return (
    <div className={`h-2 w-full overflow-hidden rounded-full bg-slate-200 ${className}`}>
      <div
        className="h-full rounded-full bg-green-700 transition-all duration-500"
        style={{ width: `${pct}%` }}
      />
    </div>
  )
}
