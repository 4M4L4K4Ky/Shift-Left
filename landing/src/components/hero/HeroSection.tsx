import { motion } from 'framer-motion'
import { ParticlesBackground } from './ParticlesBackground'
import { TypewriterText } from './TypewriterText'

export function HeroSection() {
  return (
    <section
      id="hero"
      className="w-full min-h-screen flex flex-col items-center justify-center text-center px-4 mx-auto overflow-hidden relative"
    >
      <div className="absolute inset-0 mesh-gradient" />
      <div className="absolute inset-0 bg-gradient-to-b from-bg-primary/0 via-bg-primary/40 to-bg-primary" />
      <div className="absolute inset-0 data-grid opacity-30" />
      <ParticlesBackground />

      <div className="relative z-10 max-w-4xl mx-auto flex flex-col items-center text-center">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.2 }}
        >
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-accent/10 border border-accent/20 text-accent text-xs font-medium mb-8">
            <span className="w-2 h-2 rounded-full bg-accent" />
            Shift-Left DevSecOps
          </div>
        </motion.div>

        <motion.h1
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.4 }}
          className="text-4xl md:text-6xl lg:text-7xl font-bold leading-tight mb-8 text-center min-h-[5rem]"
        >
          <TypewriterText />
        </motion.h1>

        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.6 }}
          className="text-base md:text-lg text-text-secondary text-center mx-auto max-w-2xl mb-6 leading-relaxed"
        >
          AegisCode es una plataforma de analisis estatico de codigo construida sobre
          una arquitectura hexagonal moderna con Spring Boot 3.3 y Oracle 23c.
          Integrando multiples agentes de inteligencia artificial, realiza la deteccion
          automatizada de vulnerabilidades y genera planes de remediacion de forma autonoma.
        </motion.p>

        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.7 }}
          className="text-sm text-gray-400 text-center mx-auto max-w-xl leading-relaxed"
        >
          Desarrollada como Trabajo de Fin de Master,
          demuestra como el enfoque shift-left puede integrarse de forma efectiva
          en los flujos de desarrollo de software modernos.
        </motion.p>
      </div>
    </section>
  )
}
