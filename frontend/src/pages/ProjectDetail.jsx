import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import { getProject } from '../api/projects'
import { getProjectScans } from '../api/scans'
import { LanguageBadge } from '../components/ui/Badge'
import StatusPill from '../components/ui/StatusPill'
import QualityScore from '../components/ui/QualityScore'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import EmptyState from '../components/ui/EmptyState'

function fmt(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })
}

export default function ProjectDetail() {
  const { id } = useParams()

  const { data: project, isLoading: projLoading } = useQuery({
    queryKey: ['project', id],
    queryFn: () => getProject(id),
  })

  const { data: scans = [], isLoading: scansLoading } = useQuery({
    queryKey: ['scans', 'project', id],
    queryFn: () => getProjectScans(id),
    refetchInterval: (data) =>
      data?.some?.((s) => s.status === 'RUNNING') ? 4000 : false,
  })

  if (projLoading) return <LoadingSpinner />

  return (
    <div>
      {/* Breadcrumb */}
      <Link to="/projects" className="text-xs text-slate-500 hover:text-slate-300 flex items-center gap-1 mb-6">
        <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" /></svg>
        Projects
      </Link>

      {/* Project header */}
      <div className="card p-6 mb-6">
        <div className="flex items-start justify-between gap-6 flex-wrap">
          <div className="min-w-0">
            <div className="flex items-center gap-3 flex-wrap">
              <h1 className="page-title">{project?.name}</h1>
              <LanguageBadge value={project?.language} />
            </div>
            {project?.description && (
              <p className="text-sm text-slate-400 mt-1.5">{project.description}</p>
            )}
            <div className="flex items-center gap-4 mt-3">
              {project?.repositoryUrl && (
                <a href={project.repositoryUrl} target="_blank" rel="noopener noreferrer"
                  className="flex items-center gap-1.5 text-xs text-indigo-400 hover:text-indigo-300">
                  <svg className="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/>
                  </svg>
                  View on GitHub
                </a>
              )}
              <span className="text-xs text-slate-500">Branch: <span className="code">{project?.defaultBranch || 'main'}</span></span>
            </div>
          </div>

          <Link to={`/scans/new?projectId=${id}`} className="btn-primary shrink-0">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
            </svg>
            Run Scan
          </Link>
        </div>
      </div>

      {/* Scans table */}
      <div className="flex items-center justify-between mb-4">
        <h2 className="section-title">Scan History</h2>
        <span className="text-xs text-slate-500">{scans.length} scan{scans.length !== 1 ? 's' : ''}</span>
      </div>

      {scansLoading && <LoadingSpinner message="Loading scans…" />}

      {!scansLoading && scans.length === 0 && (
        <EmptyState
          title="No scans yet"
          description="Trigger your first scan to see results here."
          action={<Link to={`/scans/new?projectId=${id}`} className="btn-primary mt-2">Run Scan</Link>}
        />
      )}

      {!scansLoading && scans.length > 0 && (
        <div className="card overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-700 text-left">
                <th className="px-4 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wide">Status</th>
                <th className="px-4 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wide">Quality</th>
                <th className="px-4 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wide">Issues</th>
                <th className="px-4 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wide">PR</th>
                <th className="px-4 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wide">Started</th>
                <th className="px-4 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wide"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-700/60">
              {scans.map((s) => (
                <tr key={s.id} className="hover:bg-slate-700/30 transition-colors">
                  <td className="px-4 py-3.5"><StatusPill status={s.status} /></td>
                  <td className="px-4 py-3.5">
                    {s.qualityScore != null ? (
                      <span className={`font-bold text-sm ${s.qualityScore >= 80 ? 'text-green-400' : s.qualityScore >= 60 ? 'text-amber-400' : 'text-red-400'}`}>
                        {s.qualityScore}/100
                      </span>
                    ) : <span className="text-slate-600">—</span>}
                  </td>
                  <td className="px-4 py-3.5">
                    <div className="flex items-center gap-2 text-xs">
                      {s.criticalIssues > 0 && <span className="text-red-400 font-semibold">{s.criticalIssues}C</span>}
                      {s.highIssues > 0 && <span className="text-orange-400 font-semibold">{s.highIssues}H</span>}
                      {s.mediumIssues > 0 && <span className="text-yellow-400">{s.mediumIssues}M</span>}
                      {s.lowIssues > 0 && <span className="text-blue-400">{s.lowIssues}L</span>}
                      {!s.totalIssues && <span className="text-slate-600">—</span>}
                    </div>
                  </td>
                  <td className="px-4 py-3.5 text-slate-400 text-xs">
                    {s.pullRequestNumber ? `#${s.pullRequestNumber}` : <span className="text-slate-600">Full</span>}
                  </td>
                  <td className="px-4 py-3.5 text-slate-400 text-xs">{fmt(s.startedAt || s.createdAt)}</td>
                  <td className="px-4 py-3.5">
                    <Link to={`/scans/${s.id}`} className="text-indigo-400 hover:text-indigo-300 text-xs font-medium">
                      View →
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
