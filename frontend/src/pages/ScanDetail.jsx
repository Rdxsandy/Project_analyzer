import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useParams, Link, Navigate } from 'react-router-dom'
import { getScan } from '../api/scans'
import StatusPill from '../components/ui/StatusPill'
import QualityScore from '../components/ui/QualityScore'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import IssuesTab from './tabs/IssuesTab'
import MetricsTab from './tabs/MetricsTab'
import AIReviewsTab from './tabs/AIReviewsTab'

const TABS = [
  { key: 'issues',     label: 'Issues' },
  { key: 'metrics',    label: 'Metrics' },
  { key: 'ai-reviews', label: 'AI Reviews' },
]

function fmt(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })
}

export default function ScanDetail() {
  const { id } = useParams()
  const [activeTab, setActiveTab] = useState('issues')

  const { data: scan, isLoading, error } = useQuery({
    queryKey: ['scan', id],
    queryFn: () => getScan(id),
    // Auto-poll every 4 seconds while RUNNING
    refetchInterval: (data) => data?.status === 'RUNNING' ? 4000 : false,
  })

  if (isLoading) return <LoadingSpinner message="Loading scan…" />
  if (error) return <p className="text-red-400 text-sm mt-8">{error.message}</p>
  if (!scan) return <Navigate to="/" />

  const duration = scan.startedAt && scan.completedAt
    ? Math.round((new Date(scan.completedAt) - new Date(scan.startedAt)) / 1000)
    : null

  return (
    <div>
      {/* Breadcrumb */}
      <Link to={`/projects/${scan.projectId}`} className="text-xs text-slate-500 hover:text-slate-300 flex items-center gap-1 mb-6">
        <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" /></svg>
        Back to Project
      </Link>

      {/* Scan header card */}
      <div className="card p-6 mb-6">
        {/* Running banner */}
        {scan.status === 'RUNNING' && (
          <div className="flex items-center gap-2 p-3 rounded-lg bg-blue-500/10 border border-blue-500/25 text-blue-300 text-sm mb-5">
            <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
            Analyzing repository… results will appear automatically
          </div>
        )}
        {scan.status === 'FAILED' && (
          <div className="p-3 rounded-lg bg-red-500/10 border border-red-500/25 text-red-300 text-sm mb-5">
            ⚠ Scan failed — check analyzer-service logs for details.
          </div>
        )}

        <div className="flex items-start gap-8 flex-wrap">
          {/* Quality score */}
          {scan.qualityScore != null && (
            <div className="shrink-0">
              <QualityScore score={scan.qualityScore} size="lg" />
            </div>
          )}

          {/* Meta info */}
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-3 flex-wrap mb-3">
              <h1 className="text-xl font-bold text-white font-mono">
                {scan.repositoryOwner}/{scan.repositoryName}
              </h1>
              <StatusPill status={scan.status} />
              {scan.pullRequestNumber && (
                <span className="code">PR #{scan.pullRequestNumber}</span>
              )}
            </div>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-4">
              {[
                { label: 'Critical', val: scan.criticalIssues, cls: 'text-red-400' },
                { label: 'High',     val: scan.highIssues,     cls: 'text-orange-400' },
                { label: 'Medium',   val: scan.mediumIssues,   cls: 'text-yellow-400' },
                { label: 'Low',      val: scan.lowIssues,      cls: 'text-blue-400' },
              ].map(({ label, val, cls }) => (
                <div key={label} className="bg-slate-900/50 rounded-lg px-3 py-2.5">
                  <p className="text-xs text-slate-500 font-medium">{label}</p>
                  <p className={`text-xl font-bold mt-0.5 ${cls}`}>{val ?? 0}</p>
                </div>
              ))}
            </div>

            <div className="flex items-center gap-6 mt-4 text-xs text-slate-500">
              {scan.startedAt && <span>Started: {fmt(scan.startedAt)}</span>}
              {scan.completedAt && <span>Completed: {fmt(scan.completedAt)}</span>}
              {duration != null && <span>Duration: {duration}s</span>}
              {scan.commitSha && <span className="code">{scan.commitSha.slice(0, 7)}</span>}
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 mb-6 border-b border-slate-700 pb-0">
        {TABS.map((t) => (
          <button
            key={t.key}
            onClick={() => setActiveTab(t.key)}
            className={`px-4 py-2.5 text-sm font-medium rounded-t-lg transition-colors ${
              activeTab === t.key
                ? 'text-indigo-400 border-b-2 border-indigo-400 -mb-px bg-indigo-500/5'
                : 'text-slate-400 hover:text-white hover:bg-slate-800'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* Tab content */}
      {activeTab === 'issues'     && <IssuesTab scanId={id} totalIssues={scan.totalIssues} />}
      {activeTab === 'metrics'    && <MetricsTab scanId={id} />}
      {activeTab === 'ai-reviews' && <AIReviewsTab scanId={id} />}
    </div>
  )
}
