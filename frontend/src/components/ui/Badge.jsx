// Severity badge: CRITICAL | HIGH | MEDIUM | LOW
const SEVERITY_STYLES = {
  CRITICAL: 'bg-red-500/15 text-red-400 border border-red-500/25',
  HIGH:     'bg-orange-500/15 text-orange-400 border border-orange-500/25',
  MEDIUM:   'bg-yellow-500/15 text-yellow-400 border border-yellow-500/25',
  LOW:      'bg-blue-500/15 text-blue-400 border border-blue-500/25',
}

// Issue type badge: SECURITY | BUG | PERFORMANCE | CODE_SMELL | CODE_QUALITY | MAINTAINABILITY
const TYPE_STYLES = {
  SECURITY:       'bg-red-500/10 text-red-300',
  BUG:            'bg-orange-500/10 text-orange-300',
  PERFORMANCE:    'bg-purple-500/10 text-purple-300',
  CODE_SMELL:     'bg-yellow-500/10 text-yellow-300',
  CODE_QUALITY:   'bg-sky-500/10 text-sky-300',
  MAINTAINABILITY:'bg-slate-500/10 text-slate-300',
}

// Language badge: JAVA | PYTHON | JAVASCRIPT
const LANG_STYLES = {
  JAVA:       'bg-amber-500/15 text-amber-400 border border-amber-500/25',
  PYTHON:     'bg-blue-500/15 text-blue-400 border border-blue-500/25',
  JAVASCRIPT: 'bg-yellow-400/15 text-yellow-300 border border-yellow-400/25',
}

export function SeverityBadge({ value }) {
  const cls = SEVERITY_STYLES[value] || 'bg-slate-500/15 text-slate-400 border border-slate-500/25'
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-md text-xs font-semibold tracking-wide ${cls}`}>
      {value}
    </span>
  )
}

export function TypeBadge({ value }) {
  const label = value?.replace('_', ' ') ?? '—'
  const cls = TYPE_STYLES[value] || 'bg-slate-500/10 text-slate-400'
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium ${cls}`}>
      {label}
    </span>
  )
}

export function LanguageBadge({ value }) {
  const cls = LANG_STYLES[value] || 'bg-slate-500/15 text-slate-400 border border-slate-500/25'
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-md text-xs font-semibold ${cls}`}>
      {value ?? '—'}
    </span>
  )
}
