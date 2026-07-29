import { Navbar } from '../components/layout/Navbar'
import { useAuth } from '../contexts/AuthContext'

export function AdminPage() {
  const { user } = useAuth()
  return (
    <div className="bg-bg-primary" style={{ width: '100%', minHeight: '100vh' }}>
      <Navbar />
      <div style={{ height: '64px' }} />
      <div style={{ width: '100%', maxWidth: '1280px', margin: '0 auto', padding: '0 24px 48px' }}>
        <div className="max-w-4xl mx-auto">
          <h1 className="text-3xl font-bold text-white mb-2">Administracion</h1>
          <p className="text-text-secondary mb-8">Panel de administracion y perfil de usuario</p>

          <div className="grid md:grid-cols-2 gap-6 mb-8">
            <div className="p-6 rounded-xl border border-border-subtle bg-bg-card/50 glass">
              <div className="flex items-center gap-4 mb-6">
                <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-accent to-accent-secondary flex items-center justify-center text-white font-bold text-xl shadow-lg">
                  {user?.username?.charAt(0).toUpperCase()}
                </div>
                <div>
                  <h2 className="text-lg font-semibold text-white">{user?.username}</h2>
                  <div className="flex gap-1.5 mt-1">
                    {user?.scopes?.split(',').map((s) => (
                      <span key={s} className="px-2 py-0.5 rounded-full bg-accent/10 border border-accent/20 text-accent text-[10px] font-medium">
                        {s.trim()}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
              <div className="space-y-3">
                <div className="flex items-center justify-between p-3 rounded-lg bg-bg-primary/50 border border-border-subtle/50">
                  <span className="text-xs text-text-secondary">Estado</span>
                  <span className="text-xs px-2 py-0.5 rounded-full bg-success/10 text-success border border-success/20">Activo</span>
                </div>
                <div className="flex items-center justify-between p-3 rounded-lg bg-bg-primary/50 border border-border-subtle/50">
                  <span className="text-xs text-text-secondary">Rol</span>
                  <span className="text-xs text-white font-mono">{
                    user?.scopes?.includes('ADMIN')
                      ? 'Administrador'
                      : user?.scopes?.includes('WRITER') && user?.scopes?.includes('READER')
                        ? 'Lectura y Escritura'
                        : user?.scopes?.includes('WRITER')
                          ? 'Solo Escritura'
                          : 'Solo Lectura'
                  }</span>
                </div>
              </div>
            </div>

            <div className="p-6 rounded-xl border border-border-subtle bg-bg-card/50 glass">
              <h2 className="text-lg font-semibold text-white mb-4">Sistema</h2>
              <div className="space-y-3">
                {[
                  { label: 'Version', value: '1.0.0' },
                  { label: 'Base de datos', value: 'H2 / Oracle 23c' },
                  { label: 'Perfil activo', value: 'local' },
                  { label: 'Framework', value: 'Spring Boot 3.3' },
                  { label: 'Frontend', value: 'React 19 + Vite 8' },
                ].map((row) => (
                  <div key={row.label} className="flex justify-between p-3 rounded-lg bg-bg-primary/50 border border-border-subtle/50">
                    <span className="text-xs text-text-secondary">{row.label}</span>
                    <span className="text-xs text-white font-mono">{row.value}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
