const SEV = [
  { key: 'critical', label: 'Critical', color: 'bg-red-500',    textColor: 'text-red-400',    pts: 10 },
  { key: 'high',     label: 'High',     color: 'bg-orange-500', textColor: 'text-orange-400', pts: 6  },
  { key: 'medium',   label: 'Medium',   color: 'bg-yellow-500', textColor: 'text-yellow-400', pts: 3  },
  { key: 'low',      label: 'Low',      color: 'bg-blue-500',   textColor: 'text-blue-400',   pts: 1  },
]

export default function SeverityBreakdown({ metrics }) {
  if (!metrics) return null
  const total = metrics.total || 1 // avoid div/0

  return (
    <div className="space-y-3">
      {SEV.map(({ key, label, color, textColor, pts }) => {
        const count = metrics[key] ?? 0
        const pct = Math.round((count / total) * 100)
        return (
          <div key={key}>
            <div className="flex items-center justify-between mb-1">
              <span className={`text-xs font-semibold ${textColor}`}>{label}</span>
              <span className="text-xs text-slate-400">
                {count} × {pts}pt{pts !== 1 ? 's' : ''} = <span className="text-white font-medium">{count * pts}</span>
              </span>
            </div>
            <div className="h-1.5 bg-slate-700 rounded-full overflow-hidden">
              <div
                className={`h-full ${color} rounded-full transition-all duration-500`}
                style={{ width: `${pct}%` }}
              />
            </div>
          </div>
        )
      })}
    </div>
  )
}
