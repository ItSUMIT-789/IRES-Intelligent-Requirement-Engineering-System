import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts'
import GlassCard from './GlassCard.jsx'

function ChartTooltip({ active, payload }) {
  if (!active || !payload?.length) return null
  const { role, count } = payload[0].payload
  return (
    <div className="glass-strong rounded-lg px-3 py-2 text-xs text-slate-100 shadow-lg">
      <span className="font-semibold">{role}</span>: {count} users
    </div>
  )
}

export default function RoleDistributionChart({ data }) {
  return (
    <GlassCard hover={false} className="p-6">
      <div className="mb-5 flex items-center justify-between">
        <div>
          <h3 className="font-display text-lg font-semibold text-white">Users by role</h3>
          <p className="mt-1 text-xs text-slate-500">Distribution across all {data.length} roles in IRES</p>
        </div>
      </div>

      <div className="h-64 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={data} margin={{ top: 4, right: 8, left: -16, bottom: 0 }}>
            <XAxis
              dataKey="role"
              tick={{ fill: '#94a3b8', fontSize: 11 }}
              tickLine={false}
              axisLine={{ stroke: 'rgba(255,255,255,0.1)' }}
              interval={0}
              angle={-15}
              textAnchor="end"
              height={50}
            />
            <YAxis tick={{ fill: '#94a3b8', fontSize: 11 }} tickLine={false} axisLine={false} allowDecimals={false} />
            <Tooltip cursor={{ fill: 'rgba(255,255,255,0.04)' }} content={<ChartTooltip />} />
            <Bar dataKey="count" radius={[8, 8, 0, 0]} maxBarSize={44}>
              {data.map((entry) => (
                <Cell key={entry.role} fill={entry.color} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-3">
        {data.map((r) => (
          <div key={r.role} className="flex items-center gap-2 text-xs text-slate-400">
            <span className="h-2.5 w-2.5 shrink-0 rounded-full" style={{ backgroundColor: r.color }} />
            <span className="truncate">{r.role}</span>
            <span className="ml-auto font-medium text-slate-200">{r.count}</span>
          </div>
        ))}
      </div>
    </GlassCard>
  )
}
