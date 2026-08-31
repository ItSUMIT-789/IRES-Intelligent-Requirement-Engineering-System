import { useState } from 'react'
import GlassCard from './GlassCard.jsx'
import Badge from './Badge.jsx'

const badgeByPriority = {
  High: 'danger',
  Medium: 'warning',
  Low: 'info',
  Critical: 'danger',
}

// items: [{ id, title, subtitle, meta, priority, columnKey }]
// columns: [{ key, label }]
export default function Kanban({ columns, items, onMove }) {
  const [dragId, setDragId] = useState(null)
  const [overColumn, setOverColumn] = useState(null)

  const handleDrop = (columnKey) => {
    if (dragId) onMove(dragId, columnKey)
    setDragId(null)
    setOverColumn(null)
  }

  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {columns.map((col) => {
        const colItems = items.filter((i) => i.columnKey === col.key)
        return (
          <div
            key={col.key}
            onDragOver={(e) => {
              e.preventDefault()
              setOverColumn(col.key)
            }}
            onDragLeave={() => setOverColumn((c) => (c === col.key ? null : c))}
            onDrop={() => handleDrop(col.key)}
            className={`flex min-h-[16rem] flex-col rounded-2xl border p-3 transition-colors ${
              overColumn === col.key ? 'border-blue-400/50 bg-blue-500/5' : 'border-white/10 bg-white/[0.02]'
            }`}
          >
            <div className="mb-3 flex items-center justify-between px-1">
              <h4 className="text-sm font-semibold text-white">{col.label}</h4>
              <span className="rounded-full bg-white/10 px-2 py-0.5 text-[11px] text-slate-400">
                {colItems.length}
              </span>
            </div>

            <div className="flex flex-1 flex-col gap-3">
              {colItems.map((item) => (
                <GlassCard
                  key={item.id}
                  hover={false}
                  draggable
                  onDragStart={() => setDragId(item.id)}
                  className={`cursor-grab p-3.5 active:cursor-grabbing ${dragId === item.id ? 'opacity-50' : ''}`}
                >
                  <p className="text-sm font-medium text-white">{item.title}</p>
                  {item.subtitle && <p className="mt-1 text-xs text-slate-400">{item.subtitle}</p>}
                  <div className="mt-3 flex items-center justify-between">
                    {item.priority && (
                      <Badge variant={badgeByPriority[item.priority] || 'neutral'}>{item.priority}</Badge>
                    )}
                    {item.meta && <span className="text-[11px] text-slate-500">{item.meta}</span>}
                  </div>
                </GlassCard>
              ))}
              {colItems.length === 0 && (
                <p className="px-1 py-6 text-center text-xs text-slate-600">Drop a card here</p>
              )}
            </div>
          </div>
        )
      })}
    </div>
  )
}
