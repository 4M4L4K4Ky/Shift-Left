import { motion } from 'framer-motion'
import { AnimatedSection } from '../shared/AnimatedSection'

interface Layer {
  name: string
  desc: string
  color: string
  borderColor: string
  items: string[]
}

const layers: Layer[] = [
  {
    name: 'Interfaz de Usuario',
    desc: 'Frontend React con Tailwind v4',
    color: 'from-accent/15 to-accent/5',
    borderColor: 'border-accent/20',
    items: ['Landing Page', 'Dashboard', 'Reportes'],
  },
  {
    name: 'Puertos de Entrada',
    desc: 'Casos de uso definidos como interfaces',
    color: 'from-success/15 to-success/5',
    borderColor: 'border-success/20',
    items: ['AuditCodeUseCase', 'GetAuditReportUseCase', 'GetStatisticsUseCase'],
  },
  {
    name: 'Puertos de Salida',
    desc: 'Contratos de infraestructura',
    color: 'from-accent-secondary/15 to-accent-secondary/5',
    borderColor: 'border-accent-secondary/20',
    items: ['AuditRepository', 'AiRepository', 'AuditStatisticsPort'],
  },
  {
    name: 'Adaptadores REST',
    desc: 'Controladores Spring Boot que exponen la API',
    color: 'from-warning/15 to-warning/5',
    borderColor: 'border-warning/20',
    items: ['AuditEngineApiController', 'DTOs', 'ApiUtil'],
  },
  {
    name: 'Adaptadores DB/AI',
    desc: 'Implementaciones Oracle 23c y Spring AI',
    color: 'from-error/15 to-error/5',
    borderColor: 'border-error/20',
    items: ['OracleAuditRepository', 'AiRepositoryScannerAdapter', 'AuditStatisticsAdapter'],
  },
  {
    name: 'Infraestructura',
    desc: 'Oracle 23c, Flyway, Docker, Spring AI',
    color: 'from-accent/15 to-accent-secondary/5',
    borderColor: 'border-accent/20',
    items: ['Oracle 23c DB', 'Flyway Migrations', 'Spring AI', 'Docker'],
  },
]

export function ArchitectureSection() {
  return (
    <AnimatedSection id="architecture" className="py-24 relative">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-16">
          <h2 className="text-3xl md:text-4xl font-bold text-white mb-4">
            Arquitectura Hexagonal
          </h2>
          <p className="text-text-secondary max-w-2xl mx-auto">
            Puertos y adaptadores que aislan el dominio de la infraestructura,
            permitiendo cambiar bases de datos o servicios externos sin tocar
            una linea de logica de negocio.
          </p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-5">
          {layers.map((layer, i) => (
            <motion.div
              key={layer.name}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, margin: '-50px' }}
              transition={{ duration: 0.4, delay: i * 0.08 }}
              className={`relative p-6 rounded-xl border ${layer.borderColor} bg-bg-card/60 hover:bg-bg-card transition-colors duration-200`}
            >
              <div className={`absolute inset-0 rounded-xl bg-gradient-to-br ${layer.color} pointer-events-none`} />
              <div className="relative z-10">
                <h3 className="text-sm font-semibold text-white mb-1">{layer.name}</h3>
                <p className="text-xs text-text-secondary mb-3">{layer.desc}</p>
                <div className="flex flex-wrap gap-1.5">
                  {layer.items.map((item) => (
                    <span key={item} className="text-[11px] px-2 py-0.5 rounded bg-bg-hover text-text-muted font-mono">
                      {item}
                    </span>
                  ))}
                </div>
              </div>
            </motion.div>
          ))}
        </div>
      </div>
    </AnimatedSection>
  )
}
