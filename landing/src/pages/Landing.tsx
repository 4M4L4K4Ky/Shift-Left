import { Navbar } from '../components/layout/Navbar'
import { HeroSection } from '../components/hero/HeroSection'
import { Footer } from '../components/layout/Footer'

export function Landing() {
  return (
    <div className="bg-bg-primary min-h-screen w-full overflow-x-hidden">
      <Navbar />
      <HeroSection />
      <Footer />
    </div>
  )
}
