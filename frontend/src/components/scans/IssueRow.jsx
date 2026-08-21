import { useState } from 'react'
import { SeverityBadge, TypeBadge } from '../ui/Badge'

export default function IssueRow({ issue }) {
  const [expanded, setExpanded] = useState(false)

  return (
    <div className="border border-slate-700 rounded-lg overflow-hidden">
      {/* Header row */}
      <button
        onClick={() => setExpanded((p) => !p)}
        className="w-full flex items-start gap-3 p-4 text-left hover:bg-slate-700/40 transition-colors"
      >
        {/* Severity */}
        <div className="mt-0.5 shrink-0">
          <SeverityBadge value={issue.severity} />
        </div>

        {/* Main content */}
        <div className="flex-1 min-w-0">
          <p className="text-sm text-white font-medium leading-snug">{issue.message}</p>
          <div className="flex flex-wrap items-center gap-x-3 gap-y-1 mt-1.5">
            <span className="font-mono text-xs text-slate-400 truncate">
              {issue.filePath}
              {issue.lineNumber ? <span className="text-slate-500">:{issue.lineNumber}</span> : null}
            </span>
            <TypeBadge value={issue.type} />
            {issue.ruleId && (
              <span className="code">{issue.ruleId}</span>
            )}
          </div>
        </div>

        {/* Expand chevron */}
        <svg
          className={`w-4 h-4 text-slate-500 shrink-0 mt-0.5 transition-transform ${expanded ? 'rotate-180' : ''}`}
          fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}
        >
          <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
        </svg>
      </button>

      {/* Suggestion panel */}
      {expanded && issue.suggestion && (
        <div className="px-4 pb-4 border-t border-slate-700/60 bg-slate-900/40">
          <p className="text-xs font-semibold text-slate-400 uppercase tracking-wide mt-3 mb-1.5">
            Suggestion
          </p>
          <p className="text-sm text-slate-300 leading-relaxed">{issue.suggestion}</p>
        </div>
      )}
    </div>
  )
}
