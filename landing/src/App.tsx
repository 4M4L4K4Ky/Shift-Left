import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom'
import { AnimatePresence } from 'framer-motion'
import { AuthProvider } from './contexts/AuthContext'
import { ProtectedRoute } from './components/shared/ProtectedRoute'
import { Landing } from './pages/Landing'
import { LoginPage } from './pages/LoginPage'
import { Dashboard } from './pages/Dashboard'
import { ArchitecturePage } from './pages/ArchitecturePage'
import { EndpointsPage } from './pages/EndpointsPage'
import { AdminPage } from './pages/AdminPage'

function AnimatedRoutes() {
  const location = useLocation()

  return (
    <AnimatePresence mode="wait">
      <Routes location={location} key={location.pathname}>
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/app" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
        <Route path="/app/architecture" element={<ProtectedRoute><ArchitecturePage /></ProtectedRoute>} />
        <Route path="/app/endpoints" element={<ProtectedRoute><EndpointsPage /></ProtectedRoute>} />
        <Route path="/app/admin" element={<ProtectedRoute><AdminPage /></ProtectedRoute>} />
      </Routes>
    </AnimatePresence>
  )
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AnimatedRoutes />
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
