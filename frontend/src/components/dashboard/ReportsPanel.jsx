import { FileText, Download } from 'lucide-react'
import GlassCard from '../GlassCard.jsx'

// reports: [{ name, type, date, size }]
export default function ReportsPanel({ reports }) {
  return (
    <GlassCard hover={false} className="p-6">
      <h3 className="mb-5 font-display text-lg font-semibold text-white">Reports</h3>
      <div className="divide-y divide-white/10">
        {reports.map((r, i) => (
          <div key={i} className="flex items-center gap-3 py-3.5 first:pt-0 last:pb-0">
            <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-white/10 bg-gradient-to-br from-blue-500/15 to-purple-500/15">
              <FileText size={16} className="text-blue-300" />
            </span>
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-medium text-white">{r.name}</p>
              <p className="text-xs text-slate-500">
                {r.type} · {r.date} · {r.size}
              </p>
            </div>
            <button
              type="button"
              onClick={(e) => e.preventDefault()}
              className="flex shrink-0 items-center gap-1.5 rounded-lg border border-white/10 bg-white/5 px-3 py-1.5 text-xs font-medium text-slate-200 hover:bg-white/10"
            >
              <Download size={13} />
              Download
            </button>
          </div>
        ))}
      </div>
    </GlassCard>
  )
}
