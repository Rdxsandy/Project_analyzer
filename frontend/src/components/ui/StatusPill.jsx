const STYLES = {
  PENDING:   'bg-slate-500/15 text-slate-400 border border-slate-500/25',
  RUNNING:   'bg-blue-500/15 text-blue-400 border border-blue-500/25',
  COMPLETED: 'bg-green-500/15 text-green-400 border border-green-500/25',
  FAILED:    'bg-red-500/15 text-red-400 border border-red-500/25',
}

const DOTS = {
  RUNNING: 'bg-blue-400 animate-pulse',
  COMPLETED: 'bg-green-400',
  FAILED: 'bg-red-400',
  PENDING: 'bg-slate-400',
}

export default function StatusPill({ status }) {
  const cls = STYLES[status] || STYLES.PENDING
  const dot = DOTS[status] || DOTS.PENDING
  return (
    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${cls}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${dot}`} />
      {status}
    </span>
  )
}
