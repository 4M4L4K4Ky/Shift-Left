import { Navbar } from '../components/layout/Navbar'
import { useAuth } from '../contexts/AuthContext'

export function ProfilePage() {
  const { user } = useAuth()
  return (
    <div className="bg-bg-primary" style={{ width: '100%', minHeight: '100vh' }}>
      <Navbar />
      <div style={{ height: '64px' }} />
      <div style={{ width: '100%', maxWidth: '1280px', margin: '0 auto', padding: '0 24px 48px' }}>
        <div className="max-w-2xl mx-auto">
          <h1 className="text-3xl font-bold text-white mb-2">Perfil</h1>
          <p className="text-text-secondary mb-8">Informacion de tu cuenta</p>

          <div className="p-6 rounded-xl border border-border-subtle bg-bg-card/50 glass">
            <div className="flex items-center gap-4 mb-6">
              <div className="w-16 h-16 rounded-xl bg-gradient-to-br from-accent to-accent-secondary flex items-center justify-center text-white font-bold text-2xl shadow-lg">
                {user?.username?.charAt(0).toUpperCase()}
              </div>
              <div>
                <h2 className="text-xl font-semibold text-white">{user?.username}</h2>
                <div className="flex gap-1.5 mt-1.5">
                  {user?.scopes?.split(',').map((s) => (
                    <span key={s} className="px-2 py-0.5 rounded-full bg-accent/10 border border-accent/20 text-accent text-xs font-medium">
                      {s.trim()}
                    </span>
                  ))}
                </div>
              </div>
            </div>
            <div className="space-y-3">
              <div className="flex items-center justify-between p-3 rounded-lg bg-bg-primary/50 border border-border-subtle/50">
                <span className="text-sm text-text-secondary">Usuario</span>
                <span className="text-sm text-white font-mono">{user?.username}</span>
              </div>
              <div className="flex items-center justify-between p-3 rounded-lg bg-bg-primary/50 border border-border-subtle/50">
                <span className="text-sm text-text-secondary">Estado</span>
                <span className="text-sm px-2 py-0.5 rounded-full bg-success/10 text-success border border-success/20">Activo</span>
              </div>
              <div className="flex items-center justify-between p-3 rounded-lg bg-bg-primary/50 border border-border-subtle/50">
                <span className="text-sm text-text-secondary">Permisos</span>
                <span className="text-sm text-white font-mono">{
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
        </div>
      </div>
    </div>
  )
}
