import { useState, useCallback, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import Particles, { ParticlesProvider } from '@tsparticles/react'
import { loadSlim } from '@tsparticles/slim'
import type { Engine } from '@tsparticles/engine'
import { useAuth } from '../contexts/AuthContext'

function ParticlesContent() {
    return (
        <Particles
            id="login-particles"
            className="absolute inset-0"
            options={{
                fullScreen: false,
                fpsLimit: 60,
                particles: {
                    color: { value: '#00d4ff' },
                    links: {
                        color: '#7c3aed',
                        distance: 120,
                        enable: true,
                        opacity: 0.1,
                        width: 1,
                    },
                    move: {
                        enable: true,
                        speed: 0.3,
                        direction: 'none' as const,
                        random: false,
                        straight: false,
                    },
                    number: {
                        density: { enable: true },
                        value: 60,
                    },
                    opacity: { value: 0.2 },
                    size: { value: { min: 1, max: 2 } },
                },
                detectRetina: true,
            }}
        />
    )
}

export function LoginPage() {
    const [username, setUsername] = useState('')
    const [password, setPassword] = useState('')
    const [showPassword, setShowPassword] = useState(false)
    const [error, setError] = useState('')
    const [submitting, setSubmitting] = useState(false)
    const { login } = useAuth()
    const navigate = useNavigate()

    const initParticles = useCallback(async (engine: Engine) => {
        await loadSlim(engine)
    }, [])

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault()
        setError('')
        setSubmitting(true)
        try {
            await login(username, password)
            navigate('/app')
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Error desconocido')
        } finally {
            setSubmitting(false)
        }
    }

    return (
        <div className="min-h-screen bg-bg-primary flex items-center justify-center relative overflow-hidden">
            <ParticlesProvider init={initParticles}>
                <ParticlesContent />
            </ParticlesProvider>

            <div className="absolute inset-0 mesh-gradient opacity-60 pointer-events-none" />

            <motion.div
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, ease: 'easeOut' }}
                className="relative z-50 w-full max-w-md px-6"
            >
                <div className="text-center mb-8">
                    <motion.a
                        href="/"
                        initial={{ opacity: 0, scale: 0.9 }}
                        animate={{ opacity: 1, scale: 1 }}
                        transition={{ delay: 0.2, duration: 0.4 }}
                        className="inline-block text-3xl font-bold tracking-tight text-white"
                    >
                        Aegis<span className="text-accent">Code</span>
                    </motion.a>
                    <motion.p
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        transition={{ delay: 0.4, duration: 0.4 }}
                        className="text-text-muted text-sm mt-2"
                    >
                        Shift-Left DevSecOps Platform
                    </motion.p>
                </div>

                <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.3, duration: 0.5 }}
                    className="relative"
                >
                    <div className="absolute -inset-1 bg-gradient-to-r from-accent/20 via-accent-secondary/20 to-accent/20 rounded-2xl blur-xl opacity-75" />
                    <div className="relative glass-strong bg-bg-card p-8 rounded-2xl">
                        <h2 className="text-lg font-semibold text-white mb-6">Iniciar Sesión</h2>

                        <form onSubmit={handleSubmit} className="space-y-4">
                            {/* USUARIO (Limpio, sin icono) */}
                            <div>
                                <label className="block text-sm text-text-secondary mb-1.5">Usuario</label>
                                <input
                                    type="text"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    className="w-full bg-bg-primary border border-border-subtle rounded-lg py-2.5 px-4 text-white text-sm focus:outline-none focus:border-accent focus:ring-1 focus:ring-accent/50 transition-all placeholder:text-white/40"
                                    placeholder="tu_usuario"
                                    autoComplete="off"
                                    required
                                />
                            </div>

                            {/* CONTRASEÑA (Limpia, solo ojo a la derecha) */}
                            <div>
                                <label className="block text-sm text-text-secondary mb-1.5">Contraseña</label>
                                <div className="relative">
                                    <input
                                        type={showPassword ? 'text' : 'password'}
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        className="w-full bg-bg-primary border border-border-subtle rounded-lg py-2.5 pl-4 pr-10 text-white text-sm focus:outline-none focus:border-accent focus:ring-1 focus:ring-accent/50 transition-all placeholder:text-white/40"
                                        placeholder="••••••••"
                                        autoComplete="off"
                                        required
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowPassword(!showPassword)}
                                        className="absolute right-3 top-1/2 -translate-y-1/2 text-text-muted hover:text-text-secondary transition-colors z-10"
                                        tabIndex={-1}
                                    >
                                        {showPassword ? (
                                            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="1.5">
                                                <path strokeLinecap="round" strokeLinejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
                                            </svg>
                                        ) : (
                                            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="1.5">
                                                <path strokeLinecap="round" strokeLinejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                                                <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                            </svg>
                                        )}
                                    </button>
                                </div>
                            </div>

                            {error && (
                                <p className="text-sm text-error">{error}</p>
                            )}

                            <motion.button
                                type="submit"
                                disabled={submitting}
                                whileHover={!submitting ? { scale: 1.01 } : {}}
                                whileTap={!submitting ? { scale: 0.99 } : {}}
                                className="w-full py-2.5 rounded-lg bg-accent text-white font-semibold text-sm hover:bg-accent-hover transition-all disabled:opacity-50 shadow-lg shadow-accent/20"
                            >
                                {submitting ? (
                                    <span className="flex items-center justify-center gap-2">
                    <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    Entrando...
                  </span>
                                ) : (
                                    'Entrar'
                                )}
                            </motion.button>
                        </form>
                    </div>
                </motion.div>

                <motion.p
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ delay: 0.8, duration: 0.4 }}
                    className="text-center mt-6 text-xs text-text-muted"
                >
                    Shift-Left DevSecOps &mdash; Arquitectura Hexagonal
                </motion.p>
            </motion.div>
        </div>
    )
}