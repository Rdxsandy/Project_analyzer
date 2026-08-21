import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getProjects } from '../api/projects'
import { getProjectScans } from '../api/scans'
import { LanguageBadge } from '../components/ui/Badge'
import StatusPill from '../components/ui/StatusPill'
import QualityScore from '../components/ui/QualityScore'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import EmptyState from '../components/ui/EmptyState'

function ProjectCard({ project }) {
  const { data: scans = [] } = useQuery({
    queryKey: ['scans', 'project', project.id],
    queryFn: () => getProjectScans(project.id),
    staleTime: 30_000,
  })

  const latest = scans[0]
  const totalIssues = scans.reduce((s, sc) => s + (sc.totalIssues ?? 0), 0)

  return (
    <Link
      to={`/projects/${project.id}`}
      className="card block p-5 hover:border-indigo-500/50 hover:bg-slate-700/30 transition-all duration-200 group"
    >
      <div className="flex items-start justify-between gap-4 mb-4">
        <div className="min-w-0">
          <p className="font-semibold text-white group-hover:text-indigo-300 transition-colors truncate">
            {project.name}
          </p>
          <p className="text-xs text-slate-500 mt-0.5 truncate">{project.repositoryUrl}</p>
        </div>
        <LanguageBadge value={project.language} />
      </div>

      {project.description && (
        <p className="text-sm text-slate-400 mb-4 line-clamp-2">{project.description}</p>
      )}

      <div className="flex items-center justify-between pt-3 border-t border-slate-700">
        <div className="flex items-center gap-3">
          {latest ? (
            <StatusPill status={latest.status} />
          ) : (
            <span className="text-xs text-slate-600">No scans yet</span>
          )}
          <span className="text-xs text-slate-500">{scans.length} scan{scans.length !== 1 ? 's' : ''}</span>
        </div>
        {latest?.qualityScore != null && (
          <span className={`text-sm font-bold ${latest.qualityScore >= 80 ? 'text-green-400' : latest.qualityScore >= 60 ? 'text-amber-400' : 'text-red-400'}`}>
            {latest.qualityScore}/100
          </span>
        )}
      </div>
    </Link>
  )
}

export default function Dashboard() {
  const { data: projects = [], isLoading, error } = useQuery({
    queryKey: ['projects'],
    queryFn: getProjects,
  })

  return (
    <div>
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="page-title">Dashboard</h1>
          <p className="text-sm text-slate-400 mt-1">Monitor all your projects and scans</p>
        </div>
        <div className="flex gap-3">
          <Link to="/projects/new" className="btn-secondary">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
            </svg>
            New Project
          </Link>
          <Link to="/scans/new" className="btn-primary">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
            </svg>
            Run Scan
          </Link>
        </div>
      </div>

      {/* Stats strip */}
      <div className="grid grid-cols-3 gap-4 mb-8">
        {[
          { label: 'Total Projects', value: projects.length, color: 'text-indigo-400' },
          { label: 'Languages', value: [...new Set(projects.map(p => p.language).filter(Boolean))].join(', ') || '—', color: 'text-slate-300', small: true },
          { label: 'Active', value: projects.length, sub: 'projects tracked', color: 'text-green-400' },
        ].map((stat) => (
          <div key={stat.label} className="card px-5 py-4">
            <p className="text-xs text-slate-500 uppercase tracking-wide font-semibold">{stat.label}</p>
            <p className={`${stat.small ? 'text-base' : 'text-3xl'} font-bold mt-1 ${stat.color}`}>{stat.value}</p>
            {stat.sub && <p className="text-xs text-slate-500 mt-0.5">{stat.sub}</p>}
          </div>
        ))}
      </div>

      {/* Projects grid */}
      <div className="mb-2 flex items-center justify-between">
        <h2 className="section-title">Projects</h2>
        <Link to="/projects" className="text-xs text-indigo-400 hover:text-indigo-300 font-medium">
          View all →
        </Link>
      </div>

      {isLoading && <LoadingSpinner />}
      {error && (
        <div className="card p-4 border-red-500/30 text-red-400 text-sm">
          Failed to load projects: {error.message}
        </div>
      )}

      {!isLoading && !error && projects.length === 0 && (
        <EmptyState
          title="No projects yet"
          description="Create your first project to start scanning code."
          action={
            <Link to="/projects/new" className="btn-primary mt-2">
              Create Project
            </Link>
          }
        />
      )}

      {!isLoading && projects.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4 mt-4">
          {projects.map((p) => (
            <ProjectCard key={p.id} project={p} />
          ))}
        </div>
      )}
    </div>
  )
}
