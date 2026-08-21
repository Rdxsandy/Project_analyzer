import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getProjects, deleteProject } from '../api/projects'
import { LanguageBadge } from '../components/ui/Badge'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import EmptyState from '../components/ui/EmptyState'

export default function Projects() {
  const qc = useQueryClient()
  const { data: projects = [], isLoading, error } = useQuery({
    queryKey: ['projects'],
    queryFn: getProjects,
  })

  const deleteMut = useMutation({
    mutationFn: deleteProject,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['projects'] }),
  })

  const handleDelete = (id, name) => {
    if (window.confirm(`Delete project "${name}"? This cannot be undone.`)) {
      deleteMut.mutate(id)
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="page-title">Projects</h1>
          <p className="text-sm text-slate-400 mt-1">{projects.length} project{projects.length !== 1 ? 's' : ''} tracked</p>
        </div>
        <Link to="/projects/new" className="btn-primary">
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
          </svg>
          New Project
        </Link>
      </div>

      {isLoading && <LoadingSpinner />}
      {error && <p className="text-red-400 text-sm">{error.message}</p>}

      {!isLoading && projects.length === 0 && (
        <EmptyState
          title="No projects"
          description="Add a repository to start analyzing your code."
          action={<Link to="/projects/new" className="btn-primary mt-2">Create Project</Link>}
        />
      )}

      {!isLoading && projects.length > 0 && (
        <div className="card overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-700 text-left">
                <th className="px-4 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wide">Name</th>
                <th className="px-4 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wide">Language</th>
                <th className="px-4 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wide">Repository</th>
                <th className="px-4 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wide">Branch</th>
                <th className="px-4 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wide"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-700/60">
              {projects.map((p) => (
                <tr key={p.id} className="hover:bg-slate-700/30 transition-colors">
                  <td className="px-4 py-3.5">
                    <Link to={`/projects/${p.id}`} className="font-medium text-white hover:text-indigo-300 transition-colors">
                      {p.name}
                    </Link>
                    {p.description && <p className="text-xs text-slate-500 mt-0.5 truncate max-w-xs">{p.description}</p>}
                  </td>
                  <td className="px-4 py-3.5"><LanguageBadge value={p.language} /></td>
                  <td className="px-4 py-3.5 font-mono text-xs text-slate-400 truncate max-w-[200px]">{p.repositoryUrl}</td>
                  <td className="px-4 py-3.5"><span className="code">{p.defaultBranch || 'main'}</span></td>
                  <td className="px-4 py-3.5">
                    <div className="flex items-center gap-2 justify-end">
                      <Link to={`/scans/new?projectId=${p.id}`} className="btn-secondary py-1.5 text-xs">
                        Scan
                      </Link>
                      <button
                        onClick={() => handleDelete(p.id, p.name)}
                        disabled={deleteMut.isPending}
                        className="btn-danger py-1.5 text-xs"
                      >
                        Delete
                      </button>
                    </div>
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
