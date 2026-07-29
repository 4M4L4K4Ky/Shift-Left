import { useState, useCallback } from 'react'
import { Navbar } from '../components/layout/Navbar'
import { useAuth } from '../contexts/AuthContext'
import { canAccessEndpoint } from '../lib/permissions'
import { motion, AnimatePresence } from 'framer-motion'

interface FieldDef {
  name: string
  label: string
  type: string
  placeholder: string
  default: string
}

interface EndpointDef {
  method: 'GET' | 'POST'
  path: string
  title: string
  desc: string
  scope: string
  accent: string
  icon: string
  fields: FieldDef[]
}

const endpoints: EndpointDef[] = [
  {
    method: 'POST', path: '/api/v1/audits/inline', title: 'Auditar Código Inline',
    desc: 'Analiza un fragmento de código fuente y detecta vulnerabilidades de seguridad.',
    scope: 'WRITER', accent: '#3b82f6', icon: 'M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4',
    fields: [{ name: 'sourceCode', label: 'Código Fuente', type: 'code', placeholder: 'Pega tu código Java aquí...', default: 'String query = "SELECT * FROM users WHERE username = \'" + userInput + "\'";\nstatement.executeQuery(query);' }],
  },
  {
    method: 'POST', path: '/api/v1/audits/repository', title: 'Auditar Repositorio',
    desc: 'Clona un repositorio GitHub y analiza todo su código Java en busca de fallos.',
    scope: 'WRITER', accent: '#7c3aed', icon: 'M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z',
    fields: [
      { name: 'repositoryUrl', label: 'URL del Repositorio', type: 'text', placeholder: 'https://github.com/user/repo.git', default: 'https://github.com/4M4L4K4Ky/vulnerable-bank-api.git' },
      { name: 'branch', label: 'Rama', type: 'text', placeholder: 'main', default: 'main' },
    ],
  },
  {
    method: 'GET', path: '/api/v1/audits/statistics', title: 'Estadísticas',
    desc: 'Métricas globales de vulnerabilidades, distribución por severidad y top CWEs.',
    scope: 'READER', accent: '#10b981', icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z',
    fields: [],
  },
  {
    method: 'GET', path: '/api/v1/audits/report', title: 'Informe PDF',
    desc: 'Descarga el informe global de todas las auditorías realizadas en formato PDF.',
    scope: 'READER', accent: '#f59e0b', icon: 'M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z',
    fields: [],
  },
]

function formatJson(obj: unknown): string {
  try { return JSON.stringify(obj, null, 2) } catch { return String(obj) }
}

function highlightJson(text: string): string {
  return text
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/"([^"]+)":/g, '<span class="text-blue-400">"$1"</span>:')
    .replace(/: "([^"]+)"/g, ': <span class="text-emerald-400">"$1"</span>')
    .replace(/: (\d+)/g, ': <span class="text-amber-300">$1</span>')
    .replace(/: (true|false)/g, ': <span class="text-violet-400">$1</span>')
    .replace(/: (null)/g, ': <span class="text-gray-500">$1</span>')
}

interface CodeBlockProps {
  code: string
  lang?: string
}

function CodeBlock({ code, lang }: CodeBlockProps) {
  const [copied, setCopied] = useState(false)
  const handleCopy = useCallback(() => {
    navigator.clipboard.writeText(code)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }, [code])
  return (
    <div className="relative rounded-xl bg-[#0d1117] border border-border-subtle overflow-hidden group">
      <div className="flex items-center justify-between px-3 py-2 bg-[#161b22] border-b border-border-subtle">
        <span className="text-[11px] font-mono text-text-muted">{lang}</span>
        <button onClick={handleCopy}
          className={`flex items-center gap-1.5 px-2 py-1 rounded-md text-[11px] font-medium transition-all ${
            copied ? 'bg-emerald-500/15 text-emerald-400' : 'bg-bg-hover/50 text-text-muted hover:text-white opacity-0 group-hover:opacity-100'
          }`}>
          {copied ? (
            <>
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M20 6L9 17l-5-5" /></svg>
              Copiado
            </>
          ) : (
            <>
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="9" y="9" width="13" height="13" rx="2" /><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1" /></svg>
              Copiar
            </>
          )}
        </button>
      </div>
      <pre className="p-4 text-xs font-mono leading-relaxed overflow-x-auto text-text-secondary whitespace-pre-wrap">{code}</pre>
    </div>
  )
}

interface ResponsePanelProps {
  status: number
  body: string
}

function ResponsePanel({ status, body }: ResponsePanelProps) {
  const lines = body.split('\n')
  const statusText = status < 300 ? 'OK' : status < 500 ? 'Error de Cliente' : 'Error de Servidor'
  const statusColor = status < 300 ? 'text-emerald-400 bg-emerald-500/15 border-emerald-500/20'
    : status < 500 ? 'text-amber-400 bg-amber-500/15 border-amber-500/20'
    : 'text-red-400 bg-red-500/15 border-red-500/20'

  return (
    <div className="rounded-xl border border-border-subtle bg-[#0a0a0f] overflow-hidden">
      <div className="flex items-center justify-between px-4 py-2.5 bg-[#0d1117] border-b border-border-subtle">
        <div className="flex items-center gap-2">
          <span className={`text-[11px] font-mono font-bold px-2 py-0.5 rounded-md border ${statusColor}`}>
            HTTP {status}
          </span>
          <span className="text-[11px] text-text-muted">{statusText}</span>
        </div>
        <span className="text-[10px] text-text-muted font-mono">
          {body.length} bytes
        </span>
      </div>
      <div className="flex">
        <div className="select-none px-3 py-4 text-right text-[11px] font-mono leading-relaxed text-gray-600 border-r border-border-subtle/50 min-w-[40px]">
          {lines.map((_, i) => (
            <div key={i}>{i + 1}</div>
          ))}
        </div>
        <pre className="flex-1 p-4 text-xs font-mono leading-relaxed overflow-x-auto max-h-72 overflow-y-auto"
          dangerouslySetInnerHTML={{ __html: highlightJson(body) }} />
      </div>
    </div>
  )
}

interface CodeEditorProps {
  value: string
  onChange: (v: string) => void
  placeholder: string
}

function CodeEditor({ value, onChange, placeholder }: CodeEditorProps) {
  return (
    <div className="rounded-xl border border-border-subtle bg-[#0d1117] overflow-hidden focus-within:border-blue-500/40 transition-colors">
      <div className="flex items-center gap-1.5 px-3 py-2 bg-[#161b22] border-b border-border-subtle">
        <span className="w-2.5 h-2.5 rounded-full bg-red-500/80" />
        <span className="w-2.5 h-2.5 rounded-full bg-amber-500/80" />
        <span className="w-2.5 h-2.5 rounded-full bg-emerald-500/80" />
        <span className="text-[10px] text-text-muted ml-2 font-mono">SourceCode.java</span>
      </div>
      <textarea
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="w-full px-4 py-3 bg-transparent text-white text-xs font-mono leading-relaxed focus:outline-none resize-y min-h-[120px]"
        style={{ caretColor: '#3b82f6' }}
        placeholder={placeholder}
        spellCheck={false}
      />
    </div>
  )
}

export function EndpointsPage() {
  const { user } = useAuth()
  const accessibleEndpoints = endpoints.filter((ep) => canAccessEndpoint(ep.scope, user?.scopes))
  const [selectedPath, setSelectedPath] = useState<string>(() => {
    const first = accessibleEndpoints[0]?.path
    return first || ''
  })
  const [formData, setFormData] = useState<Record<string, string>>(() => {
    if (accessibleEndpoints.length === 0) return {}
    const defaults: Record<string, string> = {}
    accessibleEndpoints[0].fields.forEach((f) => { defaults[f.name] = f.default })
    return defaults
  })
  const [response, setResponse] = useState<{ status: number; body: string } | null>(null)
  const [sending, setSending] = useState(false)
  const [error, setError] = useState('')
  const [codeTab, setCodeTab] = useState<'curl' | 'python' | 'node'>('curl')

  const selected = accessibleEndpoints.find((e) => e.path === selectedPath) || accessibleEndpoints[0]

  const selectEndpoint = (path: string) => {
    setSelectedPath(path)
    setResponse(null)
    setError('')
    const ep = accessibleEndpoints.find((e) => e.path === path) || accessibleEndpoints[0]
    if (!ep) return
    const defaults: Record<string, string> = {}
    ep.fields.forEach((f) => { defaults[f.name] = f.default })
    setFormData(defaults)
  }

  const sendRequest = async (ep: EndpointDef) => {
    setSending(true); setResponse(null); setError('')
    try {
      const headers: Record<string, string> = { 'Content-Type': 'application/json' }
      if (user?.token) headers['Authorization'] = `Bearer ${user.token}`

      const opts: RequestInit = { method: ep.method, headers }
      if (ep.method === 'POST') {
        const body: Record<string, unknown> = {}
        ep.fields.forEach((f) => { body[f.name] = formData[f.name] })
        opts.body = JSON.stringify(body)
      }

      const res = await fetch(ep.path, opts)
      const ct = res.headers.get('content-type') || ''
      let body: string
      if (ct.includes('application/json')) {
        body = formatJson(await res.json())
      } else if (ct.includes('application/pdf')) {
        const blob = await res.blob()
        const url = URL.createObjectURL(blob)
        window.open(url, '_blank')
        body = '[PDF descargado en nueva pestaña]'
      } else {
        body = await res.text()
      }
      setResponse({ status: res.status, body })
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Error de conexión')
    } finally { setSending(false) }
  }

  const genCurl = (ep: EndpointDef): string => {
    let cmd = `curl -X ${ep.method} 'http://localhost:8080${ep.path}'`
    if (user?.token) cmd += ` \\\n  -H 'Authorization: Bearer ${user.token.substring(0, 20)}...'`
    cmd += ` \\\n  -H 'Content-Type: application/json'`
    if (ep.method === 'POST' && ep.fields.length > 0) {
      const body: Record<string, string> = {}
      ep.fields.forEach((f) => { body[f.name] = formData[f.name] || f.default })
      cmd += ` \\\n  -d '${JSON.stringify(body)}'`
    }
    return cmd
  }

  const genPython = (ep: EndpointDef): string => {
    let code = `import requests\n\nurl = "http://localhost:8080${ep.path}"\nheaders = {"Content-Type": "application/json"}`
    if (user?.token) code += `\nheaders["Authorization"] = "Bearer ${user.token.substring(0, 20)}..."`
    if (ep.method === 'POST' && ep.fields.length > 0) {
      const body: Record<string, string> = {}
      ep.fields.forEach((f) => { body[f.name] = formData[f.name] || f.default })
      code += `\n\ndata = ${JSON.stringify(body, null, 2)}\n\nresponse = requests.${ep.method.toLowerCase()}(url, json=data, headers=headers)`
    } else {
      code += `\n\nresponse = requests.${ep.method.toLowerCase()}(url, headers=headers)`
    }
    code += `\nprint(response.status_code)\nprint(response.json())`
    return code
  }

  const genNode = (ep: EndpointDef): string => {
    let code = `const url = "http://localhost:8080${ep.path}";\nconst headers = { "Content-Type": "application/json" };`
    if (user?.token) code += `\nheaders["Authorization"] = "Bearer ${user.token.substring(0, 20)}...";`
    if (ep.method === 'POST' && ep.fields.length > 0) {
      const body: Record<string, string> = {}
      ep.fields.forEach((f) => { body[f.name] = formData[f.name] || f.default })
      code += `\n\nconst data = ${JSON.stringify(body, null, 2)};\n\nconst response = await fetch(url, {\n  method: '${ep.method}',\n  headers,\n  body: JSON.stringify(data)\n});`
    } else {
      code += `\n\nconst response = await fetch(url, { method: '${ep.method}', headers });`
    }
    code += `\nconst result = await response.json();\nconsole.log(response.status, result);`
    return code
  }

  const codeSnippets = { curl: genCurl, python: genPython, node: genNode }
  const codeTabs = [
    { id: 'curl' as const, label: 'cURL', lang: 'bash' },
    { id: 'python' as const, label: 'Python', lang: 'python' },
    { id: 'node' as const, label: 'Node.js', lang: 'javascript' },
  ]

  return (
    <div className="bg-bg-primary" style={{ width: '100%', minHeight: '100vh' }}>
      <Navbar />
      <div style={{ height: '64px' }} />
      <div style={{ width: '100%', maxWidth: '1280px', margin: '0 auto', padding: '0 24px 48px' }}>

        {/* Header */}
        <div className="mb-8">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-accent/10 border border-accent/20 text-accent text-xs font-medium mb-3">
            <span className="w-1.5 h-1.5 rounded-full bg-accent animate-pulse" />
            API Playground â€” Interactivo
          </div>
          <h1 className="text-3xl font-bold text-white">API <span className="bg-gradient-to-r from-accent to-accent-secondary bg-clip-text text-transparent">Playground</span></h1>
          <p className="text-sm text-text-muted mt-1">Selecciona un endpoint, completa los parámetros y ejecuta el análisis en tiempo real.</p>
        </div>

        {/* Two-column layout */}
        <div className="flex gap-6">

          {/* Left sidebar - endpoint navigation */}
          <div className="w-72 shrink-0">
            <div className="sticky top-20 space-y-2">
              <div className="flex items-center justify-between px-2 mb-3">
                <span className="text-[10px] font-medium text-text-muted uppercase tracking-widest">Endpoints</span>
                <span className="text-[10px] text-text-muted font-mono">{accessibleEndpoints.length}/{endpoints.length}</span>
              </div>
              {accessibleEndpoints.length === 0 ? (
                <div className="px-3 py-6 text-center">
                  <p className="text-xs text-text-muted">No tienes permisos para acceder a ningún endpoint.</p>
                </div>
              ) : accessibleEndpoints.map((ep) => {
                const isActive = selectedPath === ep.path
                return (
                  <button key={ep.path} onClick={() => selectEndpoint(ep.path)}
                    className={`relative w-full text-left p-3 rounded-xl border transition-all duration-200 ${
                      isActive
                        ? 'bg-bg-card/80 border-accent/30 shadow-lg shadow-accent/5'
                        : 'bg-bg-card/30 border-border-subtle hover:bg-bg-card/50 hover:border-border-subtle/80'
                    }`}>
                    <div className="flex items-center gap-2.5">
                      <span className={`text-[10px] font-mono font-bold px-1.5 py-0.5 rounded-md ${
                        ep.method === 'GET'
                          ? 'bg-emerald-500/15 text-emerald-400'
                          : 'bg-blue-500/15 text-blue-400'
                      }`}>
                        {ep.method}
                      </span>
                      <span className={`text-[10px] px-1.5 py-0.5 rounded-full font-medium ${
                        ep.scope === 'WRITER'
                          ? 'bg-amber-500/10 text-amber-400'
                          : 'bg-violet-500/10 text-violet-400'
                      }`}>
                        {ep.scope}
                      </span>
                    </div>
                    <p className="text-sm font-semibold text-white mt-2 leading-tight">{ep.title}</p>
                    <p className="text-[10px] text-text-muted font-mono mt-1.5 truncate">{ep.path}</p>
                    {isActive && (
                      <div className="absolute left-0 top-1/2 -translate-y-1/2 w-0.5 h-8 rounded-full bg-gradient-to-b from-accent to-accent-secondary" />
                    )}
                  </button>
                )
              })}
            </div>
          </div>

          {/* Right panel - active endpoint workspace */}
          <div className="flex-1 min-w-0">
            {!selected ? (
              <div className="flex items-center justify-center py-20">
                <div className="text-center">
                  <div className="w-12 h-12 mx-auto mb-4 rounded-xl bg-accent/10 border border-accent/20 flex items-center justify-center">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#00d4ff" strokeWidth="1.5">
                      <path d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z" />
                    </svg>
                  </div>
                  <p className="text-sm font-medium text-text-secondary mb-1">Sin acceso a endpoints</p>
                  <p className="text-xs text-text-muted">Tu rol actual no tiene permisos para usar el API Playground.</p>
                </div>
              </div>
            ) : (
            <AnimatePresence mode="wait">
              <motion.div
                key={selected.path}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                transition={{ duration: 0.2 }}
              >
                {/* Endpoint header */}
                <div className="flex items-center gap-3 mb-5 pb-5 border-b border-border-subtle/50">
                  <div className="flex items-center gap-2">
                    <span className={`text-xs font-mono font-bold px-2.5 py-1 rounded-lg ${
                      selected.method === 'GET'
                        ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/20'
                        : 'bg-blue-500/15 text-blue-400 border border-blue-500/20'
                    }`}>
                      {selected.method}
                    </span>
                    <code className="text-sm font-mono text-white">{selected.path}</code>
                  </div>
                  <span className={`text-[10px] px-2 py-0.5 rounded-full font-medium ${
                    selected.scope === 'WRITER'
                      ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                      : 'bg-violet-500/10 text-violet-400 border border-violet-500/20'
                  }`}>
                    {selected.scope}
                  </span>
                </div>

                {/* Description */}
                <p className="text-xs text-text-secondary mb-6 leading-relaxed">{selected.desc}</p>

                {/* Input area */}
                {selected.fields.length > 0 && (
                  <div className="space-y-4 mb-6">
                    {selected.fields.map((f) => (
                      <div key={f.name}>
                        <label className="block text-xs text-text-secondary font-medium mb-1.5">{f.label}</label>
                        {f.type === 'code' ? (
                          <CodeEditor
                            value={formData[f.name] ?? ''}
                            onChange={(v) => setFormData({ ...formData, [f.name]: v })}
                            placeholder={f.placeholder}
                          />
                        ) : (
                          <input
                            type={f.type}
                            value={formData[f.name] ?? ''}
                            onChange={(e) => setFormData({ ...formData, [f.name]: e.target.value })}
                            className="w-full px-3.5 py-2.5 rounded-xl bg-bg-primary/80 border border-border-subtle text-white text-xs font-mono focus:outline-none focus:border-blue-500/50 focus:ring-2 focus:ring-blue-500/20 transition-all placeholder:text-text-muted/40"
                            placeholder={f.placeholder}
                          />
                        )}
                      </div>
                    ))}
                  </div>
                )}

                {selected.fields.length === 0 && (
                  <div className="flex items-center gap-3 px-4 py-3 rounded-xl bg-accent/5 border border-accent/10 mb-6">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#00d4ff" strokeWidth="1.5">
                      <path d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <p className="text-xs text-text-secondary">Este endpoint no requiere parámetros de entrada. Haz clic en ejecutar para obtener los datos.</p>
                  </div>
                )}

                {/* Execute button */}
                <button
                  onClick={() => sendRequest(selected)}
                  disabled={sending}
                  className="w-full py-2.5 rounded-xl text-xs font-semibold text-white bg-gradient-to-r from-accent to-accent-secondary hover:from-accent-secondary hover:to-accent transition-all duration-300 border border-white/10 hover:shadow-lg hover:shadow-accent/20 disabled:opacity-50 disabled:cursor-not-allowed mb-6"
                >
                  {sending ? (
                    <span className="flex items-center justify-center gap-2">
                      <span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                      Ejecutando...
                    </span>
                  ) : (
                    <span className="flex items-center justify-center gap-2">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M5 3l14 9-14 9V3z" />
                      </svg>
                      Ejecutar {selected.method === 'GET' ? 'Consulta' : 'Análisis'}
                    </span>
                  )}
                </button>

                {/* Error */}
                {error && (
                  <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/20 mb-6">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="w-1.5 h-1.5 rounded-full bg-red-500 animate-pulse" />
                      <span className="text-xs font-medium text-red-400">Error</span>
                    </div>
                    <p className="text-xs text-red-400/80">{error}</p>
                  </div>
                )}

                {/* Response */}
                {response && (
                  <div className="mb-6">
                    <ResponsePanel status={response.status} body={response.body} />
                  </div>
                )}

                {/* Code integration tabs */}
                <div className="rounded-xl border border-border-subtle bg-bg-card/40 overflow-hidden">
                  <div className="flex items-center gap-1 px-4 py-2.5 border-b border-border-subtle">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="text-text-muted">
                      <path d="M17.25 6.75L22.5 12l-5.25 5.25m-10.5 0L1.5 12l5.25-5.25m7.5-3l-4.5 16.5" />
                    </svg>
                    <span className="text-xs font-medium text-text-muted">Integración Rápida</span>
                  </div>
                  <div className="px-4 py-3">
                    <div className="flex gap-1 mb-3">
                      {codeTabs.map((tab) => (
                        <button key={tab.id} onClick={() => setCodeTab(tab.id)}
                          className={`px-3 py-1.5 text-[11px] font-mono rounded-lg transition-all ${
                            codeTab === tab.id
                              ? 'bg-accent/15 text-accent border border-accent/20'
                              : 'text-text-muted hover:text-text-secondary border border-transparent'
                          }`}>
                          {tab.label}
                        </button>
                      ))}
                    </div>
                    <CodeBlock code={codeSnippets[codeTab](selected)} lang={codeTabs.find(t => t.id === codeTab)?.lang} />
                  </div>
                </div>

              </motion.div>
            </AnimatePresence>
            )}
          </div>

        </div>
      </div>
    </div>
  )
}
