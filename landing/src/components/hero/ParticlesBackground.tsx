import { useCallback } from 'react'
import Particles, { ParticlesProvider } from '@tsparticles/react'
import { loadSlim } from '@tsparticles/slim'
import type { Engine } from '@tsparticles/engine'

function ParticlesContent() {
  return (
    <Particles
      id="tsparticles"
      className="absolute inset-0"
      options={{
        fullScreen: false,
        fpsLimit: 60,
        particles: {
          color: { value: '#00d4ff' },
          links: {
            color: '#7c3aed',
            distance: 140,
            enable: true,
            opacity: 0.12,
            width: 1,
          },
          move: {
            enable: true,
            speed: 0.3,
            direction: 'none' as const,
            random: true,
            straight: false,
          },
          number: {
            density: { enable: true },
            value: 100,
          },
          opacity: {
            value: { min: 0.1, max: 0.4 },
            animation: { enable: true, speed: 0.5, sync: false },
          },
          size: { value: { min: 1, max: 3 } },
        },
        interactivity: {
          events: {
            onhover: { enable: true, mode: 'grab' },
          },
          modes: {
            grab: { distance: 180, links: { opacity: 0.25, color: '#00d4ff' } },
          },
        },
        detectRetina: true,
      }}
    />
  )
}

export function ParticlesBackground() {
  const init = useCallback(async (engine: Engine) => {
    await loadSlim(engine)
  }, [])

  return (
    <ParticlesProvider init={init}>
      <ParticlesContent />
    </ParticlesProvider>
  )
}
