import { motion } from 'framer-motion'
import { AnimatedSection } from '../shared/AnimatedSection'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, ResponsiveContainer,
} from 'recharts'

interface EndpointCardProps {
  title: string
  desc: string
  method: string
  path: string
  icon: React.ReactNode
  span?: number
  children?: React.ReactNode
}

const chartData = [
  { name: 'Lun', value: 12 },
  { name: 'Mar', value: 19 },
  { name: 'Mie', value: 8 },
  { name: 'Jue', value: 15 },
  { name: 'Vie', value: 22 },
  { name: 'Sab', value: 5 },
  { name: 'Dom', value: 3 },
]

function EndpointCard({ title, desc, method, path, icon, span = 1, children }: EndpointCardProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: '-50px' }}
      transition={{ duration: 0.4 }}
      className={`p-6 rounded-xl border border-border-subtle bg-bg-card/60 hover:bg-bg-card transition-colors duration-200 ${span === 2 ? 'md:col-span-2' : ''}`}
    >
      <div className="flex items-start justify-between mb-4">
        <div className="w-10 h-10 rounded-lg bg-accent/10 flex items-center justify-center text-accent">
          {icon}
        </div>
        <span className="text-xs font-mono px-2 py-1 rounded bg-bg-hover text-text-muted">
          {method}
        </span>
      </div>
      <h3 className="text-lg font-semibold text-white mb-1">{title}</h3>
      <p className="text-sm text-text-secondary mb-3">{desc}</p>
      <code className="text-xs text-text-muted font-mono block mb-4">{path}</code>
      {children}
    </motion.div>
  )
}

export function FeaturesSection() {
  const cards = [
    {
      title: 'Analizar Codigo',
      desc: 'Escanea repositorios GitHub en busca de vulnerabilidades',
      method: 'POST',
      path: '/api/v1/audit/analyze',
      span: 2,
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22" />
        </svg>
      ),
      children: (
        <div className="flex gap-2">
          <span className="text-xs px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">GitHub</span>
          <span className="text-xs px-2 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20">SonarQube</span>
          <span className="text-xs px-2 py-0.5 rounded bg-violet-500/10 text-violet-400 border border-violet-500/20">OpenAI</span>
        </div>
      ),
    },
    {
      title: 'Auditar Inline',
      desc: 'Audita fragmentos de codigo directamente',
      method: 'POST',
      path: '/api/v1/audit/inline',
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
        </svg>
      ),
      children: (
        <span className="text-xs text-text-muted">Pega codigo directamente desde tu IDE</span>
      ),
    },
    {
      title: 'Generar Informe',
      desc: 'Exporta resultados detallados de auditoria',
      method: 'GET',
      path: '/api/v1/audit/report',
      span: 2,
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
          <polyline points="14 2 14 8 20 8" />
          <line x1="16" y1="13" x2="8" y2="13" />
          <line x1="16" y1="17" x2="8" y2="17" />
          <polyline points="10 9 9 9 8 9" />
        </svg>
      ),
      children: (
        <ResponsiveContainer width="100%" height={80}>
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" stroke="#2a2a3a" />
            <XAxis dataKey="name" tick={{ fontSize: 10, fill: '#6b7280' }} axisLine={false} tickLine={false} />
            <YAxis hide />
            <Bar dataKey="value" fill="#00d4ff" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      ),
    },
    {
      title: 'Estadisticas',
      desc: 'Metricas y tendencias de analisis',
      method: 'GET',
      path: '/api/v1/audit/statistics',
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <line x1="18" y1="20" x2="18" y2="10" />
          <line x1="12" y1="20" x2="12" y2="4" />
          <line x1="6" y1="20" x2="6" y2="14" />
        </svg>
      ),
    },
  ]

  return (
    <AnimatedSection id="features" className="py-24 relative">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-16">
          <h2 className="text-3xl md:text-4xl font-bold text-white mb-4">
            Endpoints
          </h2>
          <p className="text-text-secondary max-w-2xl mx-auto">
            Cuatro operaciones principales que exponen toda la potencia
            del analisis estatico multi-agente.
          </p>
        </div>

        <div className="grid md:grid-cols-3 gap-5">
          {cards.map((card) => (
            <EndpointCard key={card.title} {...card} />
          ))}
        </div>
      </div>
    </AnimatedSection>
  )
}
