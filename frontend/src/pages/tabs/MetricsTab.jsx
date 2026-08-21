import { useQuery } from '@tanstack/react-query'
import { getScanMetrics } from '../../api/scans'
import QualityScore from '../../components/ui/QualityScore'
import SeverityBreakdown from '../../components/ui/SeverityBreakdown'
import LoadingSpinner from '../../components/ui/LoadingSpinner'

const METRIC_CARDS = [
  { key: 'critical', label: 'Critical Issues', pts: 10, cls: 'border-red-500/30 bg-red-500/5', valCls: 'text-red-400' },
  { key: 'high',     label: 'High Issues',     pts: 6,  cls: 'border-orange-500/30 bg-orange-500/5', valCls: 'text-orange-400' },
  { key: 'medium',   label: 'Medium Issues',   pts: 3,  cls: 'border-yellow-500/30 bg-yellow-500/5', valCls: 'text-yellow-400' },
  { key: 'low',      label: 'Low Issues',      pts: 1,  cls: 'border-blue-500/30 bg-blue-500/5',    valCls: 'text-blue-400' },
]

export default function MetricsTab({ scanId }) {
  const { data: metrics, isLoading, error } = useQuery({
    queryKey: ['metrics', scanId],
    queryFn: () => getScanMetrics(scanId),
    staleTime: 60_000,
  })

  if (isLoading) return <LoadingSpinner message="Loading metrics…" />
  if (error) return <p className="text-red-400 text-sm">{error.message}</p>
  if (!metrics) return <p className="text-slate-500 text-sm">No metrics available yet.</p>

  const deductions = (metrics.critical ?? 0) * 10
                   + (metrics.high ?? 0) * 6
                   + (metrics.medium ?? 0) * 3
                   + (metrics.low ?? 0) * 1

  return (
    <div className="space-y-6">
      {/* Top row: score + breakdown */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Quality score */}
        <div className="card p-6 flex flex-col items-center justify-center gap-4">
          <QualityScore score={metrics.qualityScore} size="lg" />
          <div className="text-center">
            <p className="text-sm text-slate-400">Quality Score</p>
            <p className="text-xs text-slate-600 mt-1">
              100 − {deductions} penalty points = <span className="text-white font-medium">{metrics.qualityScore ?? 0}</span>
            </p>
          </div>
        </div>

        {/* Severity breakdown */}
        <div className="card p-6">
          <p className="text-sm font-semibold text-slate-300 mb-4">Issue Breakdown</p>
          <SeverityBreakdown metrics={metrics} />
          <div className="mt-4 pt-4 border-t border-slate-700 flex items-center justify-between">
            <span className="text-xs text-slate-400">Total Issues</span>
            <span className="text-lg font-bold text-white">{metrics.total ?? 0}</span>
          </div>
        </div>
      </div>

      {/* Metric cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {METRIC_CARDS.map(({ key, label, pts, cls, valCls }) => {
          const count = metrics[key] ?? 0
          return (
            <div key={key} className={`card border p-4 ${cls}`}>
              <p className="text-xs text-slate-400 font-medium">{label}</p>
              <p className={`text-3xl font-bold mt-1 ${valCls}`}>{count}</p>
              <p className="text-xs text-slate-600 mt-1">
                × {pts}pt = <span className="text-slate-400 font-medium">{count * pts}</span>
              </p>
            </div>
          )
        })}
      </div>

      {/* Scoring explanation */}
      <div className="card p-4 bg-slate-900/50">
        <p className="text-xs font-semibold text-slate-400 uppercase tracking-wide mb-3">Scoring Formula</p>
        <div className="font-mono text-xs text-slate-400 space-y-1">
          <p>Quality Score = max(0, 100 − penalties)</p>
          <p className="text-slate-600">where penalties =</p>
          <p className="pl-4">critical × 10 + high × 6 + medium × 3 + low × 1</p>
          <p className="text-slate-600 mt-2">= {metrics.critical ?? 0}×10 + {metrics.high ?? 0}×6 + {metrics.medium ?? 0}×3 + {metrics.low ?? 0}×1 = <span className="text-white">{deductions}</span></p>
        </div>
      </div>
    </div>
  )
}
