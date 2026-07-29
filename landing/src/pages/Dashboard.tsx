import { useState, useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { useNavigate } from 'react-router-dom'
import { Navbar } from '../components/layout/Navbar'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, ResponsiveContainer, Tooltip } from 'recharts'
import { canAccessEndpoint } from '../lib/permissions'
import { motion } from 'framer-motion'

interface Stats {
  totalAudits: number
  totalVulnerabilities: number
  severityCounts: Record<string, number>
  byCwe: { cweId: string; count: number }[]
  recentAudits: {
    scanId: string
    date: string
    repositoryUrl: string
    branchName: string
    totalVulnerabilities: number
  }[]
}

const endpoints = [
  { name: 'Inline Audit', path: '/api/v1/audits/inline', method: 'POST', color: '#00d4ff', scope: 'WRITER' },
  { name: 'Repository', path: '/api/v1/audits/repository', method: 'POST', color: '#7c3aed', scope: 'WRITER' },
  { name: 'Statistics', path: '/api/v1/audits/statistics', method: 'GET', color: '#10b981', scope: 'READER' },
  { name: 'Report', path: '/api/v1/audits/report', method: 'GET', color: '#f59e0b', scope: 'READER' },
]

const chartData = [
  { name: 'Lun', audits: 4, vulns: 12 },
  { name: 'Mar', audits: 7, vulns: 19 },
  { name: 'Mié', audits: 3, vulns: 8 },
  { name: 'Jue', audits: 6, vulns: 15 },
  { name: 'Vie', audits: 9, vulns: 22 },
  { name: 'Sáb', audits: 2, vulns: 5 },
  { name: 'Dom', audits: 1, vulns: 3 },
]

export function Dashboard() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [stats, setStats] = useState<Stats | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!user) return
    const fetchStats = async () => {
      try {
        const res = await fetch('/api/v1/audits/statistics', {
          headers: { Authorization: `Bearer ${user.token}` },
        })
        if (res.status === 401) {
          logout()
          navigate('/login')
          return
        }
        if (!res.ok) throw new Error('Error al cargar estadisticas')
        const data = await res.json()
        setStats(data)
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Error de conexion')
      } finally {
        setLoading(false)
      }
    }
    fetchStats()
  }, [user, logout, navigate])

  const totalBySeverity = stats?.severityCounts
    ? Object.entries(stats.severityCounts)
        .sort(([a], [b]) => Number(b) - Number(a))
    : []

  const severityColor = (sev: number) => {
    if (sev >= 8) return 'text-error'
    if (sev >= 5) return 'text-warning'
    return 'text-success'
  }

  const severityBg = (sev: number) => {
    if (sev >= 8) return 'bg-error/10 border-error/20'
    if (sev >= 5) return 'bg-warning/10 border-warning/20'
    return 'bg-success/10 border-success/20'
  }

  return (
    <div className="bg-bg-primary" style={{ width: '100%', minHeight: '100vh' }}>
      <Navbar />
      <div style={{ height: '64px' }} />

      <div style={{ width: '100%', maxWidth: '1280px', margin: '0 auto', padding: '0 24px 48px' }}>
        {loading ? (
          <div className="flex items-center justify-center py-32">
            <div className="w-6 h-6 border-2 border-accent border-t-transparent rounded-full animate-spin" />
          </div>
        ) : error ? (
          <div className="p-10 rounded-xl border border-error/20 bg-error/5 text-center max-w-lg mx-auto glass">
            <p className="text-error text-sm font-medium mb-1">Error al cargar datos</p>
            <p className="text-text-muted text-xs mb-4">{error}</p>
            <button onClick={() => window.location.reload()} className="px-4 py-2 rounded-lg bg-accent text-white text-xs font-medium hover:bg-accent-hover transition-colors neon-glow">
              Reintentar
            </button>
          </div>
        ) : (
          <>
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8 gap-2">
              <div>
                <h1 className="text-2xl font-bold text-white">Dashboard</h1>
                <p className="text-sm text-text-muted mt-0.5">Bienvenido, {user?.username}</p>
              </div>
              <div className="flex gap-1.5 flex-wrap">
                {user?.scopes?.split(',').map((s) => (
                  <span key={s} className="px-2.5 py-0.5 rounded-full bg-accent/10 border border-accent/20 text-accent text-xs font-medium whitespace-nowrap">
                    {s.trim()}
                  </span>
                ))}
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-4 gap-4 mb-8">
              {[
                { label: 'Auditorias', value: stats?.totalAudits ?? 0, desc: 'Total de escaneos realizados', color: 'text-accent', icon: 'M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22' },
                { label: 'Vulnerabilidades', value: stats?.totalVulnerabilities ?? 0, desc: 'Detectadas en todas las auditorias', color: 'text-error', icon: 'M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126ZM12 15.75h.007v.008H12v-.008Z' },
                { label: 'Promedio', value: stats?.totalAudits && stats.totalAudits > 0 ? (stats.totalVulnerabilities / stats.totalAudits).toFixed(1) : '0.0', desc: 'Vulns por auditoria', color: 'text-success', icon: 'M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V8.625zM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V4.125z' },
                { label: 'Severidad Max', value: stats?.severityCounts ? Math.max(...Object.keys(stats.severityCounts).map(Number), 0) : '-', desc: 'Puntuacion mas alta detectada', color: 'text-warning', icon: 'M15.362 5.214A8.252 8.252 0 0112 21 8.25 8.25 0 016.038 7.048 8.287 8.287 0 009 9.6a8.983 8.983 0 013.361-6.867 8.21 8.21 0 003 2.48z' },
              ].map((stat) => (
                <motion.div
                  key={stat.label}
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="relative p-5 rounded-xl border border-border-subtle bg-gradient-to-br from-bg-card to-bg-card/50 glass overflow-hidden group hover:border-accent/30 transition-all duration-300"
                >
                  <div className="absolute top-0 right-0 w-24 h-24 opacity-[0.03] group-hover:opacity-[0.35] transition-all duration-500">
                    <svg viewBox="0 0 24 24" fill="currentColor" className={`w-full h-full ${stat.color}`}>
                      <path d={stat.icon} />
                    </svg>
                  </div>
                  <p className={`text-3xl font-bold ${stat.color} mb-1 relative z-10`}>{stat.value}</p>
                  <p className="text-sm font-medium text-text-secondary relative z-10">{stat.label}</p>
                  <p className="text-xs text-text-muted mt-1 relative z-10">{stat.desc}</p>
                </motion.div>
              ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
              <div className="lg:col-span-2 p-5 rounded-xl border border-border-subtle bg-bg-card/50 glass">
                <h2 className="text-sm font-semibold text-text-secondary uppercase tracking-wider mb-4">Actividad Semanal</h2>
                <ResponsiveContainer width="100%" height={200}>
                  <BarChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#1e1e32" />
                    <XAxis dataKey="name" tick={{ fontSize: 11, fill: '#5c5c72' }} axisLine={false} tickLine={false} />
                    <YAxis tick={{ fontSize: 11, fill: '#5c5c72' }} axisLine={false} tickLine={false} />
                    <Tooltip
                      contentStyle={{ background: '#13131e', border: '1px solid #1e1e32', borderRadius: '8px', fontSize: '12px' }}
                      labelStyle={{ color: '#9494a8' }}
                    />
                    <Bar dataKey="audits" name="Auditorias" fill="#00d4ff" radius={[4, 4, 0, 0]} />
                    <Bar dataKey="vulns" name="Vulnerabilidades" fill="#7c3aed" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>

              <div className="p-5 rounded-xl border border-border-subtle bg-bg-card/50 glass">
                <h2 className="text-sm font-semibold text-text-secondary uppercase tracking-wider mb-4">Estado Endpoints</h2>
                <div className="space-y-3">
                  {endpoints.filter((ep) => canAccessEndpoint(ep.scope, user?.scopes)).length === 0 ? (
                    <p className="text-xs text-text-muted text-center py-4">No hay endpoints disponibles para tu rol.</p>
                  ) : endpoints.filter((ep) => canAccessEndpoint(ep.scope, user?.scopes)).map((ep) => (
                    <div key={ep.name} className="flex items-center justify-between p-2.5 rounded-lg bg-bg-primary/50 border border-border-subtle/50">
                      <div className="flex items-center gap-2.5">
                        <span className="w-2 h-2 rounded-full bg-success animate-pulse" />
                        <div>
                          <p className="text-xs font-medium text-white">{ep.name}</p>
                          <code className="text-[10px] text-text-muted font-mono">{ep.method}</code>
                        </div>
                      </div>
                      <span className="text-[10px] px-2 py-0.5 rounded-full bg-success/10 text-success border border-success/20">200 OK</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {(!stats || (stats.totalAudits === 0 && (!stats.byCwe || stats.byCwe.length === 0))) ? (
              <div className="py-20 flex flex-col items-center justify-center text-center glass rounded-xl border border-border-subtle">
                <div className="w-16 h-16 mb-5 rounded-2xl bg-accent/10 flex items-center justify-center neon-glow">
                  <svg className="w-8 h-8 text-accent" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="1.5">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 3v11.25A2.25 2.25 0 006 16.5h2.25M3.75 3h-1.5m1.5 0h16.5m0 0h1.5m-1.5 0v11.25A2.25 2.25 0 0118 16.5h-2.25m-7.5 0h7.5m-7.5 0l-1 3m8.5-3l1 3m0 0l.5 1.5m-.5-1.5h-9.5m0 0l-.5 1.5m.75-9l3-3 2.148 2.148A12.061 12.061 0 0116.5 7.605" />
                  </svg>
                </div>
                <h3 className="text-lg font-semibold text-white mb-1">No hay auditorias disponibles</h3>
                <p className="text-sm text-text-muted max-w-sm">
                  Los datos apareceran cuando realices tus primeras auditorias. Utiliza la API para comenzar.
                </p>
              </div>
            ) : (
              <>
                {totalBySeverity.length > 0 && (
                  <section className="mb-8">
                    <h2 className="text-sm font-semibold text-text-secondary uppercase tracking-wider mb-3">Distribucion por Severidad</h2>
                    <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                      {totalBySeverity.map(([sev, count]) => {
                        const sevNum = Number(sev)
                        const maxCount = Math.max(...totalBySeverity.map(([, c]) => c))
                        return (
                          <div key={sev} className={`p-4 rounded-xl border ${severityBg(sevNum)} glass`}>
                            <div className="flex items-center justify-between mb-2">
                              <span className={`text-lg font-bold ${severityColor(sevNum)}`}>{sevNum}</span>
                              <span className="text-xs text-text-muted">/10</span>
                            </div>
                            <div className="relative h-1.5 rounded-full bg-bg-primary overflow-hidden">
                              <div className={`absolute inset-y-0 left-0 rounded-full transition-all duration-700 ${
                                sevNum >= 8 ? 'bg-error' : sevNum >= 5 ? 'bg-warning' : 'bg-success'
                              }`} style={{ width: `${(count / maxCount) * 100}%` }} />
                            </div>
                            <p className="text-xs text-text-muted mt-2">{count} vuln{count !== 1 ? 's' : ''}</p>
                          </div>
                        )
                      })}
                    </div>
                  </section>
                )}

                {stats?.byCwe && stats.byCwe.length > 0 && (
                  <section className="mb-8">
                    <h2 className="text-sm font-semibold text-text-secondary uppercase tracking-wider mb-3">Top CWEs</h2>
                    <div className="grid gap-2">
                      {stats.byCwe.slice(0, 5).map((cwe, i) => (
                        <motion.div
                          key={cwe.cweId}
                          initial={{ opacity: 0, x: -10 }}
                          animate={{ opacity: 1, x: 0 }}
                          transition={{ delay: i * 0.05 }}
                          className="flex items-center justify-between p-3 rounded-lg border border-border-subtle bg-bg-card/50 glass hover:border-accent/20 transition-all"
                        >
                          <span className="text-sm font-mono text-accent">{cwe.cweId}</span>
                          <div className="flex items-center gap-3">
                            <div className="w-20 h-1.5 rounded-full bg-bg-primary overflow-hidden">
                              <div className="h-full rounded-full bg-accent-secondary" style={{ width: `${(cwe.count / Math.max(...stats.byCwe.map(c => c.count))) * 100}%` }} />
                            </div>
                            <span className="text-sm text-text-secondary">{cwe.count}</span>
                          </div>
                        </motion.div>
                      ))}
                    </div>
                  </section>
                )}

                {stats?.recentAudits && stats.recentAudits.length > 0 && (
                  <section>
                    <h2 className="text-sm font-semibold text-text-secondary uppercase tracking-wider mb-3">Auditorias Recientes</h2>
                    <div className="overflow-x-auto rounded-xl border border-border-subtle glass">
                      <table className="w-full text-sm">
                        <thead>
                          <tr className="bg-bg-card/50">
                            <th className="text-left px-4 py-3 text-text-muted font-medium">Repositorio</th>
                            <th className="text-left px-4 py-3 text-text-muted font-medium">Rama</th>
                            <th className="text-left px-4 py-3 text-text-muted font-medium">Vulns</th>
                            <th className="text-left px-4 py-3 text-text-muted font-medium">Fecha</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-border-subtle">
                          {stats.recentAudits.map((audit) => (
                            <tr key={audit.scanId} className="hover:bg-bg-hover/30 transition-colors">
                              <td className="px-4 py-3 text-text-secondary max-w-[200px] truncate">{audit.repositoryUrl}</td>
                              <td className="px-4 py-3 text-text-secondary">{audit.branchName}</td>
                              <td className="px-4 py-3">
                                <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                                  audit.totalVulnerabilities > 0 ? 'bg-error/10 text-error' : 'bg-success/10 text-success'
                                }`}>
                                  {audit.totalVulnerabilities}
                                </span>
                              </td>
                              <td className="px-4 py-3 text-text-muted">{new Date(audit.date).toLocaleDateString()}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </section>
                )}
              </>
            )}
          </>
        )}
      </div>
    </div>
  )
}
