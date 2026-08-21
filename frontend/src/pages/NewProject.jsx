import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate, Link } from 'react-router-dom'
import { createProject } from '../api/projects'

const LANGUAGES = ['JAVA', 'PYTHON', 'JAVASCRIPT']

export default function NewProject() {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [form, setForm] = useState({
    name: '',
    repositoryUrl: '',
    description: '',
    language: 'JAVA',
    defaultBranch: 'main',
  })
  const [errors, setErrors] = useState({})

  const mut = useMutation({
    mutationFn: createProject,
    onSuccess: (data) => {
      qc.invalidateQueries({ queryKey: ['projects'] })
      navigate(`/projects/${data.id}`)
    },
  })

  const validate = () => {
    const e = {}
    if (!form.name.trim()) e.name = 'Name is required'
    if (!form.repositoryUrl.trim()) e.repositoryUrl = 'Repository URL is required'
    return e
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    const e2 = validate()
    if (Object.keys(e2).length) { setErrors(e2); return }
    mut.mutate(form)
  }

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }))

  return (
    <div className="max-w-xl">
      <div className="mb-8">
        <Link to="/projects" className="text-xs text-slate-500 hover:text-slate-300 flex items-center gap-1 mb-3">
          <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" /></svg>
          Back to Projects
        </Link>
        <h1 className="page-title">New Project</h1>
        <p className="text-sm text-slate-400 mt-1">Connect a repository to start scanning</p>
      </div>

      <form onSubmit={handleSubmit} className="card p-6 space-y-5">
        {mut.isError && (
          <div className="p-3 rounded-lg bg-red-500/10 border border-red-500/25 text-red-400 text-sm">
            {mut.error?.message}
          </div>
        )}

        <div>
          <label className="label">Project Name *</label>
          <input className="input" placeholder="e.g. My API Service" value={form.name} onChange={set('name')} />
          {errors.name && <p className="text-xs text-red-400 mt-1">{errors.name}</p>}
        </div>

        <div>
          <label className="label">Repository URL *</label>
          <input className="input" placeholder="https://github.com/owner/repo" value={form.repositoryUrl} onChange={set('repositoryUrl')} />
          {errors.repositoryUrl && <p className="text-xs text-red-400 mt-1">{errors.repositoryUrl}</p>}
        </div>

        <div>
          <label className="label">Description</label>
          <textarea className="input resize-none" rows={3} placeholder="Optional project description" value={form.description} onChange={set('description')} />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Language</label>
            <select className="input" value={form.language} onChange={set('language')}>
              {LANGUAGES.map((l) => <option key={l} value={l}>{l}</option>)}
            </select>
          </div>
          <div>
            <label className="label">Default Branch</label>
            <input className="input" placeholder="main" value={form.defaultBranch} onChange={set('defaultBranch')} />
          </div>
        </div>

        <div className="flex gap-3 pt-2">
          <button type="submit" disabled={mut.isPending} className="btn-primary flex-1 justify-center">
            {mut.isPending ? 'Creating…' : 'Create Project'}
          </button>
          <Link to="/projects" className="btn-secondary flex-1 justify-center text-center">
            Cancel
          </Link>
        </div>
      </form>
    </div>
  )
}
