import { motion, AnimatePresence } from 'framer-motion'
import { useScrollPosition } from '../../hooks/useScrollPosition'
import { useAuth } from '../../contexts/AuthContext'
import { useState } from 'react'
import { useLocation } from 'react-router-dom'

export function Navbar() {
  const scrolled = useScrollPosition()
  const [mobileOpen, setMobileOpen] = useState(false)
  const { user, logout } = useAuth()
  const location = useLocation()
  const isLoginPage = location.pathname === '/login'

  const isActive = (href: string) => {
    if (href.startsWith('#')) {
      return location.pathname === '/' && location.hash === href
    }
    if (href === '/app') {
      return location.pathname === '/app'
    }
    return location.pathname.startsWith(href)
  }

  const handleLogout = () => {
    logout()
    setMobileOpen(false)
    window.location.href = '/'
  }

  const guestLinks: { label: string; href: string }[] = []

  const authedLinks = [
    { label: 'Inicio', href: '/app' },
    { label: 'Arquitectura', href: '/app/architecture' },
    { label: 'Endpoints', href: '/app/endpoints' },
    { label: 'Administracion', href: '/app/admin' },
  ]

  const links = user ? authedLinks : guestLinks

  if (isLoginPage) return null

  return (
    <motion.nav
      initial={{ y: -80 }}
      animate={{ y: 0 }}
      transition={{ duration: 0.6, ease: 'easeOut' }}
      className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
        scrolled
          ? 'bg-bg-primary/80 backdrop-blur-xl border-b border-border-subtle shadow-lg shadow-black/20'
          : 'bg-transparent'
      }`}
    >
      <div style={{ width: '100%', maxWidth: '1280px', margin: '0 auto', padding: '0 24px', height: '64px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <a
          href={user ? '/app' : '#'}
          className="text-2xl font-bold tracking-tight text-white whitespace-nowrap"
        >
          Aegis<span className="text-accent">Code</span>
        </a>

        <div className="hidden md:flex items-center gap-1.5">
          {links.map((link) => {
            const active = isActive(link.href)
            return (
              <a
                key={link.href}
                href={link.href}
                className={`px-3 py-1.5 text-sm rounded-lg transition-all whitespace-nowrap ${
                  active
                    ? 'text-emerald-400 bg-emerald-500/10 hover:bg-emerald-500/15'
                    : 'text-text-secondary hover:text-white hover:bg-bg-hover'
                }`}
              >
                {link.label}
              </a>
            )
          })}
          {user && (
            <>
              <span className="w-px h-5 bg-border-subtle mx-1.5" />
              <div className="flex items-center gap-2 ml-1">
                <span className="text-sm text-text-muted whitespace-nowrap">{user.username}</span>
                <button
                  onClick={handleLogout}
                  className="px-3 py-1.5 text-sm text-text-secondary hover:text-error hover:bg-error/10 rounded-lg transition-all whitespace-nowrap"
                >
                  Cerrar Sesion
                </button>
              </div>
            </>
          )}
          {!user && (
            <a
              href="/login"
              className="ml-2 px-4 py-1.5 rounded-lg bg-accent text-white text-sm font-medium hover:bg-accent-hover transition-all shadow-lg shadow-accent/20 whitespace-nowrap"
            >
              Acceder al Dashboard
            </a>
          )}
        </div>

        <button
          className="md:hidden text-white p-2"
          onClick={() => setMobileOpen(!mobileOpen)}
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            {mobileOpen ? (
              <path d="M6 18L18 6M6 6l12 12" />
            ) : (
              <path d="M4 6h16M4 12h16M4 18h16" />
            )}
          </svg>
        </button>
      </div>

      <AnimatePresence>
        {mobileOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="md:hidden bg-bg-secondary border-t border-border-subtle overflow-hidden"
          >
            <div className="px-6 py-5 flex flex-col gap-2">
              {links.map((link) => {
                const active = isActive(link.href)
                return (
                  <a
                    key={link.href}
                    href={link.href}
                    onClick={() => setMobileOpen(false)}
                    className={`px-3 py-2 rounded-lg text-sm transition-colors ${
                      active
                        ? 'text-emerald-400 bg-emerald-500/10'
                        : 'text-text-secondary hover:text-white hover:bg-bg-hover'
                    }`}
                  >
                    {link.label}
                  </a>
                )
              })}
              {user ? (
                <div className="flex flex-col gap-2 pt-4 mt-2 border-t border-border-subtle">
                  <span className="px-3 text-sm text-text-muted">{user.username}</span>
                  <button
                    onClick={handleLogout}
                    className="px-3 py-2 rounded-lg text-sm text-left text-text-secondary hover:text-error hover:bg-error/10 transition-colors"
                  >
                    Cerrar Sesion
                  </button>
                </div>
              ) : (
                <a
                  href="/login"
                  onClick={() => setMobileOpen(false)}
                  className="mt-2 px-5 py-2.5 rounded-lg bg-accent text-white text-sm font-medium text-center hover:bg-accent-hover transition-all shadow-lg shadow-accent/20"
                >
                  Acceder al Dashboard
                </a>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.nav>
  )
}
