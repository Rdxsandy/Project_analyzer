import { useState, useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getScanIssues } from '../../api/scans'
import { SeverityBadge } from '../../components/ui/Badge'
import IssueRow from '../../components/scans/IssueRow'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import EmptyState from '../../components/ui/EmptyState'

const SEVERITIES = ['ALL', 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW']
const TYPES = ['ALL', 'SECURITY', 'BUG', 'PERFORMANCE', 'CODE_SMELL', 'CODE_QUALITY', 'MAINTAINABILITY']

export default function IssuesTab({ scanId }) {
  const [sevFilter, setSevFilter] = useState('ALL')
  const [typeFilter, setTypeFilter] = useState('ALL')
  const [search, setSearch] = useState('')

  const { data: issues = [], isLoading } = useQuery({
    queryKey: ['issues', scanId],
    queryFn: () => getScanIssues(scanId),
    staleTime: 30_000,
  })

  const filtered = useMemo(() => {
    return issues.filter((i) => {
      if (sevFilter !== 'ALL' && i.severity !== sevFilter) return false
      if (typeFilter !== 'ALL' && i.type !== typeFilter) return false
      if (search && !i.message?.toLowerCase().includes(search.toLowerCase()) &&
          !i.filePath?.toLowerCase().includes(search.toLowerCase()) &&
          !i.ruleId?.toLowerCase().includes(search.toLowerCase())) return false
      return true
    })
  }, [issues, sevFilter, typeFilter, search])

  // counts per severity
  const counts = useMemo(() => {
    const c = {}
    for (const i of issues) c[i.severity] = (c[i.severity] || 0) + 1
    return c
  }, [issues])

  if (isLoading) return <LoadingSpinner message="Loading issues…" />

  return (
    <div>
      {/* Filter bar */}
      <div className="flex flex-wrap items-center gap-3 mb-5">
        {/* Search */}
        <div className="relative flex-1 min-w-[200px]">
          <svg className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            className="input pl-9"
            placeholder="Search by message, file, or rule…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        {/* Severity filter */}
        <div className="flex gap-1">
          {SEVERITIES.map((s) => (
            <button
              key={s}
              onClick={() => setSevFilter(s)}
              className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors ${
                sevFilter === s
                  ? 'bg-indigo-600 text-white'
                  : 'bg-slate-800 text-slate-400 hover:text-white border border-slate-700'
              }`}
            >
              {s === 'ALL' ? `All (${issues.length})` : `${s} (${counts[s] ?? 0})`}
            </button>
          ))}
        </div>

        {/* Type filter */}
        <select
          className="input py-1.5 text-xs w-auto"
          value={typeFilter}
          onChange={(e) => setTypeFilter(e.target.value)}
        >
          {TYPES.map((t) => <option key={t} value={t}>{t === 'ALL' ? 'All Types' : t.replace('_', ' ')}</option>)}
        </select>
      </div>

      {/* Results count */}
      <p className="text-xs text-slate-500 mb-3">
        Showing {filtered.length} of {issues.length} issues
      </p>

      {/* Issues list */}
      {filtered.length === 0 ? (
        <EmptyState
          title={issues.length === 0 ? 'No issues found' : 'No issues match the filter'}
          description={issues.length === 0 ? 'This scan found no issues — great code!' : 'Try clearing the filters.'}
        />
      ) : (
        <div className="space-y-2">
          {filtered.map((issue) => (
            <IssueRow key={issue.id} issue={issue} />
          ))}
        </div>
      )}
    </div>
  )
}
