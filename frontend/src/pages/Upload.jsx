import { useState, useRef, useCallback } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import axios from '../api/axios'
import { useQuery } from '@tanstack/react-query'
import { getProjects } from '../api/projects'
import { LanguageBadge } from '../components/ui/Badge'
import QualityScore from '../components/ui/QualityScore'
import { SeverityBadge } from '../components/ui/Badge'

const LANGUAGES = ['MULTI', 'JAVA', 'PYTHON', 'JAVASCRIPT']
const ALLOWED_EXT = new Set(['.java', '.py', '.js', '.ts', '.jsx', '.tsx', '.zip'])

function ext(name) {
  const i = name.lastIndexOf('.')
  return i >= 0 ? name.slice(i).toLowerCase() : ''
}

function formatBytes(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1048576).toFixed(1) + ' MB'
}

// ---------- Upload Results Component ----------
function UploadResults({ result, onNewScan }) {
  const navigate = useNavigate()
  const [expandedRows, setExpandedRows] = useState({})

  const toggle = (id) => setExpandedRows(p => ({ ...p, [id]: !p[id] }))

  const SEV_COLORS = {
    CRITICAL: 'text-red-400',
    HIGH:     'text-orange-400',
    MEDIUM:   'text-yellow-400',
    LOW:      'text-blue-400',
  }

  return (
    <div className="space-y-6">
      {/* Result header */}
      <div className="card p-6">
        <div className="flex items-start gap-8 flex-wrap">
          <div className="shrink-0">
            <QualityScore score={result.qualityScore} size="lg" />
          </div>
          <div className="flex-1">
            <h2 className="text-xl font-bold text-white mb-1">Analysis Complete</h2>
            <p className="text-sm text-slate-400 mb-4">
              Found <span className="text-white font-semibold">{result.totalIssues}</span> issue{result.totalIssues !== 1 ? 's' : ''}
            </p>
            <div className="grid grid-cols-4 gap-3">
              {[
                { label: 'Critical', val: result.criticalIssues, cls: 'text-red-400' },
                { label: 'High',     val: result.highIssues,     cls: 'text-orange-400' },
                { label: 'Medium',   val: result.mediumIssues,   cls: 'text-yellow-400' },
                { label: 'Low',      val: result.lowIssues,      cls: 'text-blue-400' },
              ].map(s => (
                <div key={s.label} className="bg-slate-900/60 rounded-lg px-3 py-2">
                  <p className="text-xs text-slate-500">{s.label}</p>
                  <p className={`text-2xl font-bold ${s.cls}`}>{s.val}</p>
                </div>
              ))}
            </div>
          </div>
          <div className="flex flex-col gap-2 shrink-0">
            {result.scanId && (
              <button
                onClick={() => navigate(`/scans/${result.scanId}`)}
                className="btn-primary"
              >
                View Full Scan →
              </button>
            )}
            <button onClick={onNewScan} className="btn-secondary">
              Upload Another
            </button>
          </div>
        </div>
      </div>

      {/* Issues list */}
      {result.issues && result.issues.length > 0 && (
        <div>
          <h3 className="section-title mb-4">Issues Found</h3>
          <div className="space-y-2">
            {result.issues.map((issue, i) => (
              <div key={i} className="border border-slate-700 rounded-lg overflow-hidden">
                <button
                  onClick={() => toggle(i)}
                  className="w-full flex items-start gap-3 p-4 text-left hover:bg-slate-700/40 transition-colors"
                >
                  <SeverityBadge value={issue.severity} />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-white font-medium leading-snug">{issue.message}</p>
                    <div className="flex items-center gap-3 mt-1 flex-wrap">
                      <span className="font-mono text-xs text-slate-400">
                        {issue.filePath}
                        {issue.lineNumber > 0 && <span className="text-slate-500">:{issue.lineNumber}</span>}
                      </span>
                      {issue.ruleId && <span className="code">{issue.ruleId}</span>}
                    </div>
                  </div>
                  <svg
                    className={`w-4 h-4 text-slate-500 shrink-0 mt-0.5 transition-transform ${expandedRows[i] ? 'rotate-180' : ''}`}
                    fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}
                  >
                    <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
                  </svg>
                </button>
                {expandedRows[i] && issue.suggestion && (
                  <div className="px-4 pb-4 border-t border-slate-700/60 bg-slate-900/40">
                    <p className="text-xs font-semibold text-slate-400 uppercase tracking-wide mt-3 mb-1.5">Suggestion</p>
                    <p className="text-sm text-slate-300 leading-relaxed">{issue.suggestion}</p>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {result.issues && result.issues.length === 0 && (
        <div className="card p-10 flex flex-col items-center justify-center gap-3">
          <div className="w-14 h-14 rounded-full bg-green-500/20 flex items-center justify-center">
            <svg className="w-7 h-7 text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <p className="text-base font-semibold text-green-400">No issues found — excellent code!</p>
          <button onClick={onNewScan} className="btn-secondary mt-2">Upload Another</button>
        </div>
      )}
    </div>
  )
}

// ---------- Main Upload Page ----------
export default function Upload() {
  const { data: projects = [] } = useQuery({ queryKey: ['projects'], queryFn: getProjects })

  const [language, setLanguage] = useState('MULTI')
  const [projectId, setProjectId] = useState('')
  const [files, setFiles] = useState([])
  const [isDragging, setIsDragging] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [progress, setProgress] = useState(0)
  const [error, setError] = useState('')
  const [result, setResult] = useState(null)

  const fileInputRef = useRef(null)
  const folderInputRef = useRef(null)

  // ---------- File handling ----------
  const VENDOR_DIRS = ['node_modules/', '__pycache__/', '.venv/', 'venv/', '.git/', 'target/', 'build/', 'dist/', '.idea/', '.gradle/']

  const addFiles = useCallback((incoming) => {
    const valid = Array.from(incoming).filter(f => {
      if (!ALLOWED_EXT.has(ext(f.name))) return false
      
      const path = f.webkitRelativePath || f.name
      if (VENDOR_DIRS.some(vd => path.includes('/' + vd) || path.startsWith(vd))) return false
      
      return true
    })
    
    if (valid.length === 0) {
      setError('No supported files found (vendor directories are excluded).')
      return
    }
    setFiles(prev => {
      const names = new Set(prev.map(f => f.name + f.size))
      const deduped = valid.filter(f => !names.has(f.name + f.size))
      return [...prev, ...deduped]
    })
    setError('')
  }, [])

  const onDrop = useCallback((e) => {
    e.preventDefault()
    setIsDragging(false)
    const items = e.dataTransfer.files
    if (items) addFiles(items)
  }, [addFiles])

  const onDragOver = (e) => { e.preventDefault(); setIsDragging(true) }
  const onDragLeave = () => setIsDragging(false)

  const removeFile = (i) => setFiles(p => p.filter((_, idx) => idx !== i))

  const totalSize = files.reduce((s, f) => s + f.size, 0)

  // ---------- Submit ----------
  const handleSubmit = async (e) => {
    e.preventDefault()
    if (files.length === 0) { setError('Please select at least one file.'); return }

    setUploading(true)
    setError('')
    setProgress(0)

    const fd = new FormData()
    files.forEach(f => fd.append('files', f, f.webkitRelativePath || f.name))
    fd.append('language', language)
    if (projectId) fd.append('projectId', projectId)

    try {
      const response = await axios.post('/api/upload/scan', fd, {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (evt) => {
          if (evt.total) setProgress(Math.round((evt.loaded / evt.total) * 50))
        },
      })
      setProgress(100)
      setResult(response.data)
    } catch (err) {
      setError(err.message || 'Upload failed. Make sure upload-service is running on port 8086.')
    } finally {
      setUploading(false)
    }
  }

  const resetForm = () => {
    setFiles([])
    setResult(null)
    setError('')
    setProgress(0)
  }

  // ---------- Render ----------
  if (result) {
    return (
      <div>
        <div className="mb-6">
          <h1 className="page-title">Upload Scan Results</h1>
          <p className="text-sm text-slate-400 mt-1">Static analysis of your uploaded files</p>
        </div>
        <UploadResults result={result} onNewScan={resetForm} />
      </div>
    )
  }

  return (
    <div className="max-w-2xl">
      <div className="mb-8">
        <h1 className="page-title">Upload & Scan</h1>
        <p className="text-sm text-slate-400 mt-1">
          Upload a project folder, individual source files, or a ZIP archive for instant security analysis
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-5">
        {/* Error banner */}
        {error && (
          <div className="p-3 rounded-lg bg-red-500/10 border border-red-500/25 text-red-400 text-sm">
            {error}
          </div>
        )}

        {/* Drop zone */}
        <div
          onDrop={onDrop}
          onDragOver={onDragOver}
          onDragLeave={onDragLeave}
          className={`card border-2 border-dashed rounded-xl p-10 text-center transition-colors duration-200 ${
            isDragging
              ? 'border-indigo-500 bg-indigo-500/10'
              : 'border-slate-600 hover:border-slate-500 hover:bg-slate-800/50'
          }`}
        >
          <div className="flex flex-col items-center gap-3">
            <div className={`w-14 h-14 rounded-full flex items-center justify-center transition-colors ${
              isDragging ? 'bg-indigo-500/30' : 'bg-slate-700/60'
            }`}>
              <svg className={`w-7 h-7 ${isDragging ? 'text-indigo-300' : 'text-slate-400'}`}
                fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                <path strokeLinecap="round" strokeLinejoin="round"
                  d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" />
              </svg>
            </div>
            <div>
              <p className="text-sm font-medium text-slate-300">
                {isDragging ? 'Drop files here' : 'Drag & drop files here'}
              </p>
              <p className="text-xs text-slate-500 mt-1">
                .java · .py · .js · .ts · .jsx · .tsx · .zip
              </p>
            </div>
            <div className="flex gap-3 mt-2">
              <span className="btn-secondary text-xs py-1.5 cursor-pointer"
                onClick={() => fileInputRef.current?.click()}>
                Browse Files
              </span>
              <span className="btn-secondary text-xs py-1.5 cursor-pointer"
                onClick={() => folderInputRef.current?.click()}>
                Browse Folder
              </span>
            </div>
          </div>

          {/* Hidden inputs */}
          <input
            ref={fileInputRef}
            type="file"
            multiple
            accept=".java,.py,.js,.ts,.jsx,.tsx,.zip"
            className="hidden"
            onChange={e => { addFiles(e.target.files); e.target.value = '' }}
          />
          <input
            ref={folderInputRef}
            type="file"
            webkitdirectory=""
            directory=""
            className="hidden"
            onChange={e => { addFiles(e.target.files); e.target.value = '' }}
          />
        </div>

        {/* File list */}
        {files.length > 0 && (
          <div className="card overflow-hidden">
            <div className="flex items-center justify-between px-4 py-3 border-b border-slate-700">
              <span className="text-sm font-medium text-slate-300">
                {files.length} file{files.length !== 1 ? 's' : ''} selected
                <span className="text-slate-500 ml-2">({formatBytes(totalSize)})</span>
              </span>
              <button type="button" onClick={() => setFiles([])}
                className="text-xs text-slate-500 hover:text-red-400 transition-colors">
                Clear all
              </button>
            </div>
            <div className="max-h-48 overflow-y-auto divide-y divide-slate-700/60">
              {files.map((f, i) => (
                <div key={i} className="flex items-center gap-3 px-4 py-2.5 hover:bg-slate-700/30">
                  <span className="font-mono text-xs text-slate-400 flex-1 truncate">{f.webkitRelativePath || f.name}</span>
                  <span className="text-xs text-slate-600 shrink-0">{formatBytes(f.size)}</span>
                  <button type="button" onClick={() => removeFile(i)}
                    className="text-slate-600 hover:text-red-400 transition-colors shrink-0">
                    <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Settings row */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Language</label>
            <select className="input" value={language} onChange={e => setLanguage(e.target.value)}>
              {LANGUAGES.map(l => <option key={l} value={l}>{l}</option>)}
            </select>
          </div>
          <div>
            <label className="label">
              Link to Project
              <span className="text-slate-500 font-normal ml-1">(optional)</span>
            </label>
            <select className="input" value={projectId} onChange={e => setProjectId(e.target.value)}>
              <option value="">None — standalone scan</option>
              {projects.map(p => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
            </select>
          </div>
        </div>

        {/* Progress bar */}
        {uploading && (
          <div>
            <div className="flex justify-between text-xs text-slate-400 mb-1">
              <span>{progress < 50 ? 'Uploading files…' : 'Analyzing…'}</span>
              <span>{progress}%</span>
            </div>
            <div className="h-2 bg-slate-700 rounded-full overflow-hidden">
              <div
                className="h-full bg-indigo-500 rounded-full transition-all duration-300"
                style={{ width: `${progress}%` }}
              />
            </div>
          </div>
        )}

        {/* Tip */}
        <div className="p-3 rounded-lg bg-slate-800/60 border border-slate-700 text-xs text-slate-500">
          💡 <strong className="text-slate-400">Tip:</strong> Use "Browse Folder" to upload an entire project directory.
          node_modules, __pycache__, .venv, and .git folders are automatically excluded.
        </div>

        {/* Submit */}
        <button
          type="submit"
          disabled={uploading || files.length === 0}
          className="btn-primary w-full justify-center py-3 text-base"
        >
          {uploading ? (
            <>
              <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
              Analyzing…
            </>
          ) : (
            <>
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
              </svg>
              Start Analysis
            </>
          )}
        </button>
      </form>
    </div>
  )
}
