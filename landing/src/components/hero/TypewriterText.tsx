import Typewriter from 'typewriter-effect'

const strings = [
  'Análisis Estático Multi-Agente con IA',
  'Arquitectura Hexagonal y Spring Boot 3',
  'DevSecOps Automatizado con Oracle 23c',
  'Seguridad Proactiva en Cada Commit',
]

export function TypewriterText() {
  return (
    <Typewriter
      options={{
        strings,
        autoStart: true,
        loop: true,
        delay: 50,
        deleteSpeed: 30,
      }}
    />
  )
}
