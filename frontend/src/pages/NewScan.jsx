import { useState, useEffect } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useSearchParams, Link } from 'react-router-dom'
import { getProjects } from '../api/projects'
import { createScan } from '../api/scans'

const LANGUAGES = ['MULTI', 'JAVA', 'PYTHON', 'JAVASCRIPT']

export default function NewScan() {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [params] = useSearchParams()
  const preselectedProjectId = params.get('projectId')

  const { data: projects = [] } = useQuery({ queryKey: ['projects'], queryFn: getProjects })

  const [form, setForm] = useState({
    projectId: preselectedProjectId || '',
    repositoryOwner: '',
    repositoryName: '',
    language: 'MULTI',
    branch: 'main',
    pullRequestNumber: '',
  })

  // Auto-fill from selected project
  useEffect(() => {
    if (!form.projectId) return
    const proj = projects.find((p) => String(p.id) === String(form.projectId))
    if (!proj) return
    const url = proj.repositoryUrl || ''
    // parse owner/repo from GitHub URL
    const match = url.match(/github\.com\/([^/]+)\/([^/]+?)(?:\.git)?$/)
    setForm((f) => ({
      ...f,
      repositoryOwner: match ? match[1] : f.repositoryOwner,
      repositoryName: match ? match[2] : f.repositoryName,
      language: proj.language || f.language,
      branch: proj.defaultBranch || f.branch,
    }))
  }, [form.projectId, projects])

  const mut = useMutation({
    mutationFn: createScan,
    onSuccess: (data) => {
      qc.invalidateQueries({ queryKey: ['scans'] })
      navigate(`/scans/${data.id}`)
    },
  })

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }))

  const handleSubmit = (e) => {
    e.preventDefault()
    const payload = {
      projectId: Number(form.projectId),
      repositoryOwner: form.repositoryOwner,
      repositoryName: form.repositoryName,
      language: form.language,
      pullRequestNumber: form.pullRequestNumber ? Number(form.pullRequestNumber) : null,
    }
    mut.mutate(payload)
  }

  const isIncremental = Boolean(form.pullRequestNumber)

  return (
    <div className="max-w-xl">
      <div className="mb-8">
        <Link to="/" className="text-xs text-slate-500 hover:text-slate-300 flex items-center gap-1 mb-3">
          <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" /></svg>
          Back
        </Link>
        <h1 className="page-title">Run New Scan</h1>
        <p className="text-sm text-slate-400 mt-1">
          {isIncremental ? 'PR scan — only changed files will be analyzed' : 'Full scan — entire repository will be analyzed'}
        </p>
      </div>

      <form onSubmit={handleSubmit} className="card p-6 space-y-5">
        {mut.isError && (
          <div className="p-3 rounded-lg bg-red-500/10 border border-red-500/25 text-red-400 text-sm">
            {mut.error?.message}
          </div>
        )}

        <div>
          <label className="label">Project *</label>
          <select className="input" value={form.projectId} onChange={set('projectId')} required>
            <option value="">Select a project…</option>
            {projects.map((p) => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Repository Owner *</label>
            <input className="input" placeholder="e.g. octocat" value={form.repositoryOwner} onChange={set('repositoryOwner')} required />
          </div>
          <div>
            <label className="label">Repository Name *</label>
            <input className="input" placeholder="e.g. my-repo" value={form.repositoryName} onChange={set('repositoryName')} required />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Language</label>
            <select className="input" value={form.language} onChange={set('language')}>
              {LANGUAGES.map((l) => <option key={l} value={l}>{l}</option>)}
            </select>
          </div>
          <div>
            <label className="label">
              PR Number
              <span className="ml-1 text-slate-500 font-normal">(optional — for PR scan)</span>
            </label>
            <input className="input" type="number" min="1" placeholder="e.g. 42" value={form.pullRequestNumber} onChange={set('pullRequestNumber')} />
          </div>
        </div>

        {isIncremental && (
          <div className="p-3 rounded-lg bg-indigo-500/10 border border-indigo-500/25 text-indigo-300 text-xs">
            ⚡ Incremental PR scan — only changed files in PR #{form.pullRequestNumber} will be analyzed
          </div>
        )}

        <div className="flex gap-3 pt-2">
          <button type="submit" disabled={mut.isPending || !form.projectId} className="btn-primary flex-1 justify-center">
            {mut.isPending ? 'Starting scan…' : 'Start Scan'}
          </button>
          <Link to="/" className="btn-secondary flex-1 justify-center text-center">Cancel</Link>
        </div>
      </form>
    </div>
  )
}
