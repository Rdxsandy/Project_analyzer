import { useState } from 'react'

const CONFIDENCE_STYLES = {
  HIGH:    'bg-green-500/15 text-green-400 border border-green-500/25',
  MEDIUM:  'bg-yellow-500/15 text-yellow-400 border border-yellow-500/25',
  LOW:     'bg-red-500/15 text-red-400 border border-red-500/25',
  UNKNOWN: 'bg-slate-500/15 text-slate-400 border border-slate-500/25',
}

export default function AIReviewCard({ review }) {
  const [expanded, setExpanded] = useState(false)
  const confStyle = CONFIDENCE_STYLES[review.confidence?.toUpperCase()] || CONFIDENCE_STYLES.UNKNOWN

  return (
    <div className={`card overflow-hidden border ${review.valid ? 'border-slate-700' : 'border-red-500/20'}`}>
      <button
        onClick={() => setExpanded((p) => !p)}
        className="w-full flex items-center gap-3 p-4 text-left hover:bg-slate-700/30 transition-colors"
      >
        {/* Valid indicator */}
        <div className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 ${
          review.valid ? 'bg-green-500/20' : 'bg-red-500/20'
        }`}>
          {review.valid ? (
            <svg className="w-4 h-4 text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
            </svg>
          ) : (
            <svg className="w-4 h-4 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          )}
        </div>

        {/* Rule + confidence */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <span className="code">{review.rule}</span>
            <span className={`inline-flex items-center px-2 py-0.5 rounded-md text-xs font-semibold ${confStyle}`}>
              {review.confidence ?? 'UNKNOWN'} confidence
            </span>
            <span className={`text-xs font-medium ${review.valid ? 'text-green-400' : 'text-red-400'}`}>
              {review.valid ? '✓ Confirmed' : '✗ False positive'}
            </span>
          </div>
        </div>

        <svg
          className={`w-4 h-4 text-slate-500 shrink-0 transition-transform ${expanded ? 'rotate-180' : ''}`}
          fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}
        >
          <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
        </svg>
      </button>

      {expanded && (
        <div className="px-4 pb-4 border-t border-slate-700/60 space-y-4 bg-slate-900/30">
          {review.explanation && (
            <div className="pt-4">
              <p className="text-xs font-semibold text-slate-400 uppercase tracking-wide mb-1.5">AI Explanation</p>
              <p className="text-sm text-slate-300 leading-relaxed">{review.explanation}</p>
            </div>
          )}
          {review.recommendation && (
            <div>
              <p className="text-xs font-semibold text-indigo-400 uppercase tracking-wide mb-1.5">Recommendation</p>
              <p className="text-sm text-slate-300 leading-relaxed">{review.recommendation}</p>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
