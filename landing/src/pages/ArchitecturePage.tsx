import { Navbar } from '../components/layout/Navbar'
import { motion } from 'framer-motion'

const layers = [
  {
    name: 'Domain Layer',
    subtitle: 'N\u00facleo del Negocio',
    icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <path d="M12 2L2 7l10 5 10-5-10-5z" />
        <path d="M2 17l10 5 10-5" />
        <path d="M2 12l10 5 10-5" />
      </svg>
    ),
    gradient: 'from-accent/20 to-accent/5',
    border: 'border-accent/30',
    accent: 'text-accent',
    bg: 'bg-accent/10',
    desc: 'Modelos puros de dominio sin dependencias externas. Contienen las reglas de negocio fundamentales del sistema.',
    entities: [
      { name: 'User', type: 'Entidad' },
      { name: 'AuditReport', type: 'Entidad' },
      { name: 'Vulnerability', type: 'Entidad' },
      { name: 'Severity', type: 'Value Object' },
    ],
    badges: ['POJOs', 'Inmutables', 'Domain Events', 'Sin Dependencias'],
    connectors: ['Puertos de Entrada', 'Puertos de Salida'],
  },
  {
    name: 'Application Layer',
    subtitle: 'Casos de Uso y Orquestaci\u00f3n',
    icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <rect x="2" y="2" width="20" height="8" rx="2" />
        <rect x="2" y="14" width="20" height="8" rx="2" />
        <path d="M6 6h.01M6 18h.01" />
      </svg>
    ),
    gradient: 'from-accent-secondary/20 to-accent-secondary/5',
    border: 'border-accent-secondary/30',
    accent: 'text-accent-secondary',
    bg: 'bg-accent-secondary/10',
    desc: 'Orquestaci\u00f3n de la l\u00f3gica de negocio. Define los puertos de entrada (in) y salida (out) como interfaces.',
    entities: [
      { name: 'AuthUseCase', type: 'Use Case' },
      { name: 'AuditCodeUseCase', type: 'Use Case' },
      { name: 'GetReportUseCase', type: 'Use Case' },
      { name: 'AuthPort', type: 'In Port' },
    ],
    badges: ['Spring Boot 3.3', 'JWT Security', 'Use Cases', 'Ports In/Out'],
    connectors: ['Adaptadores REST', 'Adaptadores DB'],
  },
  {
    name: 'Infrastructure Layer',
    subtitle: 'Adaptadores y Conectores',
    icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <path d="M4 7V4h16v3" />
        <path d="M4 17v3h16v-3" />
        <rect x="2" y="7" width="20" height="10" rx="1.5" />
        <circle cx="12" cy="12" r="2" />
      </svg>
    ),
    gradient: 'from-success/20 to-success/5',
    border: 'border-success/30',
    accent: 'text-success',
    bg: 'bg-success/10',
    desc: 'Implementaciones concretas de los puertos definidos en Application. Conectan el dominio con el mundo exterior.',
    entities: [
      { name: 'OracleAuditRepository', type: 'JPA Adapter' },
      { name: 'AiScannerAdapter', type: 'AI Adapter' },
      { name: 'AuditEngineController', type: 'REST' },
      { name: 'JwtAuthFilter', type: 'Security' },
    ],
    badges: ['Oracle 23c', 'JPA / Hibernate', 'Spring AI', 'Docker'],
    connectors: ['Base de Datos', 'API REST', 'IA'],
  },
]

const technologies = [
  { name: 'Spring Boot 3.3', category: 'Framework', color: 'bg-success/10 text-success border-success/20' },
  { name: 'Oracle 23c', category: 'Database', color: 'bg-error/10 text-error border-error/20' },
  { name: 'JPA / Hibernate', category: 'ORM', color: 'bg-accent/10 text-accent border-accent/20' },
  { name: 'Spring AI', category: 'AI', color: 'bg-accent-secondary/10 text-accent-secondary border-accent-secondary/20' },
  { name: 'JWT', category: 'Security', color: 'bg-warning/10 text-warning border-warning/20' },
  { name: 'Flyway', category: 'Migrations', color: 'bg-accent/10 text-accent border-accent/20' },
  { name: 'Docker', category: 'Container', color: 'bg-accent/10 text-accent border-accent/20' },
  { name: 'React 19', category: 'Frontend', color: 'bg-accent-secondary/10 text-accent-secondary border-accent-secondary/20' },
]

const flowSteps = [
  { label: 'Cliente HTTP', desc: 'REST / JWT', accent: 'text-cyan-400', border: 'border-cyan-500/40', bg: 'bg-cyan-500/8' },
  { label: 'Controller', desc: 'Inbound Adapter', accent: 'text-violet-400', border: 'border-violet-500/40', bg: 'bg-violet-500/8' },
  { label: 'Application', desc: 'Use Case / Port', accent: 'text-emerald-400', border: 'border-emerald-500/40', bg: 'bg-emerald-500/8' },
  { label: 'Domain', desc: 'Modelo Puro', accent: 'text-amber-400', border: 'border-amber-500/40', bg: 'bg-amber-500/8' },
  { label: 'Outbound', desc: 'JPA / AI', accent: 'text-violet-400', border: 'border-violet-500/40', bg: 'bg-violet-500/8' },
  { label: 'DB / IA', desc: 'Oracle / AI', accent: 'text-cyan-400', border: 'border-cyan-500/40', bg: 'bg-cyan-500/8' },
]

export function ArchitecturePage() {
  return (
    <div className="bg-bg-primary" style={{ width: '100%', minHeight: '100vh' }}>
      <Navbar />
      <div style={{ height: '64px' }} />
      <div style={{ width: '100%', maxWidth: '1280px', margin: '0 auto', padding: '0 24px 64px' }}>

        {/* ── Header ── */}
        <div className="text-center pt-8 pb-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
          >
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-accent/10 border border-accent/20 text-accent text-xs font-medium mb-5">
              <span className="w-1.5 h-1.5 rounded-full bg-accent animate-pulse" />
              Arquitectura Hexagonal &mdash; Ports &amp; Adapters
            </div>
            <h1 className="text-4xl md:text-5xl font-bold text-white mb-4 tracking-tight">
              AegisCode{' '}
              <span className="bg-gradient-to-r from-accent to-accent-secondary bg-clip-text text-transparent">
                Architecture
              </span>
            </h1>
            <p className="text-text-secondary mx-auto text-sm">
              Puertos y adaptadores que aislan el dominio de la infraestructura, permitiendo cambiar bases de datos o servicios externos sin tocar una línea de lógica de negocio.
            </p>
          </motion.div>
        </div>

        {/* ── Capas Hexagonales ── */}
        <section className="mb-16">
          <div className="flex items-center gap-3 mb-8">
            <div className="w-1 h-6 rounded-full bg-gradient-to-b from-accent to-accent-secondary" />
            <div>
              <h2 className="text-lg font-semibold text-white">Capas de la Arquitectura</h2>
              <p className="text-xs text-text-muted">Tres niveles que separan el dominio del mundo exterior</p>
            </div>
          </div>

          <div className="relative">
            <div className="absolute inset-0 flex items-center justify-center pointer-events-none opacity-[0.03]">
              <svg width="600" height="600" viewBox="0 0 100 100" className="w-[500px] h-[500px]">
                <polygon points="50 5, 95 27.5, 95 72.5, 50 95, 5 72.5, 5 27.5" fill="none" stroke="#00d4ff" strokeWidth="0.5" />
                <polygon points="50 15, 85 32.5, 85 67.5, 50 85, 15 67.5, 15 32.5" fill="none" stroke="#7c3aed" strokeWidth="0.5" />
                <polygon points="50 25, 75 37.5, 75 62.5, 50 75, 25 62.5, 25 37.5" fill="none" stroke="#10b981" strokeWidth="0.5" />
              </svg>
            </div>

            <div className="grid md:grid-cols-3 gap-6 relative z-10">
              {layers.map((layer, i) => (
                <motion.div
                  key={layer.name}
                  initial={{ opacity: 0, y: 30 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: i * 0.15, duration: 0.5 }}
                  className={`group relative p-6 rounded-2xl border ${layer.border} bg-bg-card/70 backdrop-blur-sm hover:bg-bg-card/90 transition-all duration-300 hover:scale-[1.02] hover:shadow-xl ${
                    i === 0 ? 'hover:shadow-accent/15' : i === 1 ? 'hover:shadow-accent-secondary/15' : 'hover:shadow-success/15'
                  }`}
                  style={{
                    background: `linear-gradient(135deg, rgba(19,19,30,0.9), rgba(19,19,30,0.5))`,
                  }}
                >
                  <div className={`absolute inset-0 rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-500 pointer-events-none ${layer.gradient}`} />

                  <div className={`absolute -top-3 -right-3 w-8 h-8 rounded-full ${layer.border} bg-bg-primary flex items-center justify-center text-xs font-bold ${layer.accent}`}>
                    {i + 1}
                  </div>

                  <div className="relative z-10">
                    <div className="flex items-center gap-3 mb-4">
                      <div className={`w-10 h-10 rounded-xl ${layer.bg} border ${layer.border} flex items-center justify-center ${layer.accent} group-hover:scale-110 transition-transform duration-300`}>
                        {layer.icon}
                      </div>
                      <div>
                        <h2 className="text-lg font-bold text-white">{layer.name}</h2>
                        <p className={`text-xs ${layer.accent} font-semibold`}>{layer.subtitle}</p>
                      </div>
                    </div>

                    <p className="text-xs text-text-secondary leading-relaxed mb-4">{layer.desc}</p>

                    <div className="space-y-1.5 mb-4">
                      {layer.entities.map((e) => (
                        <div key={e.name} className="flex items-center justify-between px-3 py-2 rounded-lg bg-black/40 border border-white/[0.06] group-hover:border-white/[0.12] transition-colors">
                          <code className="text-xs font-mono text-gray-200">{e.name}</code>
                          <span className={`text-[10px] px-1.5 py-0.5 rounded font-medium ${layer.accent} bg-white/[0.06]`}>{e.type}</span>
                        </div>
                      ))}
                    </div>

                    <div className="flex items-center gap-2 mb-4">
                      <div className="h-px flex-1 bg-gradient-to-r from-transparent via-white/10 to-transparent" />
                      <span className="text-[10px] text-gray-500 uppercase tracking-wider">Conectores</span>
                      <div className="h-px flex-1 bg-gradient-to-r from-transparent via-white/10 to-transparent" />
                    </div>
                    <div className="flex flex-wrap gap-1.5 mb-4">
                      {layer.connectors.map((c) => (
                        <span key={c} className={`text-[10px] px-2 py-0.5 rounded-full border ${layer.border} ${layer.accent} bg-white/[0.04]`}>
                          {c}
                        </span>
                      ))}
                    </div>

                    <div className="flex flex-wrap gap-1.5">
                      {layer.badges.map((b) => (
                        <span key={b} className={`text-[10px] px-2 py-0.5 rounded-md font-medium ${layer.accent} bg-white/[0.06] border border-white/[0.08] hover:bg-white/[0.12] transition-colors`}>
                          {b}
                        </span>
                      ))}
                    </div>
                  </div>
                </motion.div>
              ))}
            </div>
          </div>
        </section>

        {/* ── Stack Tecnol�gico ── */}
        <section className="mb-16">
          <div className="flex items-center gap-3 mb-8">
            <div className="w-1 h-6 rounded-full bg-gradient-to-b from-accent-secondary to-accent" />
            <div>
              <h2 className="text-lg font-semibold text-white">Stack Tecnol&oacute;gico</h2>
              <p className="text-xs text-text-muted">Tecnolog&iacute;as que potencian la plataforma</p>
            </div>
          </div>

          <div className="p-6 rounded-2xl border border-border-subtle bg-bg-card/40 glass">
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
              {technologies.map((tech, i) => (
                <motion.div
                  key={tech.name}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.6 + i * 0.05, duration: 0.3 }}
                  className="flex items-center gap-2.5 p-3 rounded-xl bg-black/40 border border-white/[0.06] hover:border-white/[0.15] transition-all hover:bg-black/60"
                >
                  <div className={`px-2 py-0.5 rounded text-[10px] font-mono font-semibold ${tech.color}`}>
                    {tech.category}
                  </div>
                  <span className="text-xs text-gray-300">{tech.name}</span>
                </motion.div>
              ))}
            </div>
          </div>
        </section>

        {/* ── Flujo de Datos ── */}
        <section>
          <div className="flex items-center gap-3 mb-8">
            <div className="w-1 h-6 rounded-full bg-gradient-to-b from-accent to-success" />
            <div>
              <h2 className="text-lg font-semibold text-white">Flujo de Datos</h2>
              <p className="text-xs text-text-muted">Recorrido de una solicitud a través de la arquitectura</p>
            </div>
          </div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.8, duration: 0.5 }}
            className="relative p-8 md:p-10 rounded-2xl border border-border-subtle bg-bg-card/30 glass overflow-hidden"
          >
            <div className="absolute inset-0 bg-gradient-to-r from-accent/[0.03] via-transparent to-accent-secondary/[0.03] pointer-events-none" />

            {/* Header */}
            <div className="flex items-center justify-between mb-10 relative">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-accent to-cyan-400 flex items-center justify-center shadow-lg shadow-accent/20">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2">
                    <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
                  </svg>
                </div>
                <div>
                  <p className="text-sm font-semibold text-white">Pipeline de Solicitud</p>
                  <p className="text-[10px] text-text-muted">Request → Response</p>
                </div>
              </div>
              <span className="hidden sm:inline-flex items-center gap-1.5 text-[11px] px-3 py-1 rounded-full bg-accent/10 border border-accent/20 text-accent font-mono">
                <span className="w-1.5 h-1.5 rounded-full bg-accent" />
                6 etapas
              </span>
            </div>

            {/* Stepper */}
            <div className="flex flex-col md:flex-row items-stretch gap-4 md:gap-0 relative">
              {flowSteps.map((step, i) => (
                <div key={step.label} className="flex items-center flex-1 min-w-0">
                  {/* Step card */}
                  <div className={`flex-1 p-4 rounded-xl border ${step.border} ${step.bg} bg-black/40 text-center transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5`}>
                    <p className={`text-[10px] font-bold ${step.accent} mb-1`}>{step.label}</p>
                    <p className="text-[10px] text-gray-500">{step.desc}</p>
                  </div>

                  {/* Arrow connector */}
                  {i < flowSteps.length - 1 && (
                    <div className="hidden md:flex items-center justify-center shrink-0 w-6">
                      <svg width="20" height="12" viewBox="0 0 20 12" fill="none">
                        <path d="M1 6h14M12 2l5 4-5 4" stroke="#3b3b52" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                      </svg>
                    </div>
                  )}

                  {/* Mobile connector */}
                  {i < flowSteps.length - 1 && (
                    <div className="flex md:hidden items-center justify-center w-full py-2">
                      <svg width="12" height="20" viewBox="0 0 12 20" fill="none">
                        <path d="M6 2v14M2 12l4 4 4-4" stroke="#3b3b52" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                      </svg>
                    </div>
                  )}
                </div>
              ))}
            </div>

            {/* Legend */}
            <div className="mt-10 pt-5 border-t border-white/[0.06] flex flex-wrap items-center justify-center gap-8 text-[11px]">
              <div className="flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-cyan-400" />
                <span className="text-gray-400">Inbound</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-violet-400" />
                <span className="text-gray-400">Outbound</span>
              </div>
              <div className="flex items-center gap-2">
                <svg width="28" height="8" viewBox="0 0 28 8" fill="none">
                  <path d="M2 4h20M18 1l5 3-5 3" stroke="#3b3b52" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                <span className="text-gray-400">Flujo</span>
              </div>
            </div>
          </motion.div>
        </section>

      </div>
    </div>
  )
}
