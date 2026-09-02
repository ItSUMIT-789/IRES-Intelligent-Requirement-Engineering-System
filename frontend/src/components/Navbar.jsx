import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import { Sparkles, Menu, X } from 'lucide-react'

const navLinks = [
  { label: 'Home', href: '#home' },
  { label: 'Features', href: '#features' },
  { label: 'About', href: '#about' },
]

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false)
  const [open, setOpen] = useState(false)

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 12)
    window.addEventListener('scroll', onScroll)
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  return (
    <motion.header
      initial={{ y: -80, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ duration: 0.6, ease: 'easeOut' }}
      className="fixed top-0 inset-x-0 z-50"
    >
      <div
        className={`mx-auto mt-4 max-w-6xl rounded-2xl px-5 py-3 transition-all duration-300 ${
          scrolled ? 'border border-slate-200 bg-white shadow-sm' : 'bg-white/90'
        }`}
      >
        <nav className="flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 group">
            <span className="relative flex h-9 w-9 items-center justify-center rounded-lg bg-green-700">
              <Sparkles size={18} className="text-white" />
            </span>
            <span className="font-display text-lg font-semibold tracking-tight text-white">
              IRES <span className="gradient-text">AI</span>
            </span>
          </Link>

          <div className="hidden md:flex items-center gap-8">
            {navLinks.map((link) => (
              <a
                key={link.label}
                href={link.href}
                className="text-sm font-medium text-slate-600 transition-colors hover:text-green-700"
              >
                {link.label}
              </a>
            ))}
          </div>

          <div className="hidden md:block">
            <Link to="/login" className="btn-gradient !px-5 !py-2.5 text-sm">
              Login
            </Link>
          </div>

          <button
            className="rounded-lg p-2 text-slate-700 hover:bg-green-50 md:hidden"
            onClick={() => setOpen((v) => !v)}
            aria-label="Toggle menu"
          >
            {open ? <X size={22} /> : <Menu size={22} />}
          </button>
        </nav>

        <AnimatePresence>
          {open && (
            <motion.div
              initial={{ height: 0, opacity: 0 }}
              animate={{ height: 'auto', opacity: 1 }}
              exit={{ height: 0, opacity: 0 }}
              transition={{ duration: 0.25 }}
              className="md:hidden overflow-hidden"
            >
              <div className="flex flex-col gap-4 pt-5 pb-2">
                {navLinks.map((link) => (
                  <a
                    key={link.label}
                    href={link.href}
                    onClick={() => setOpen(false)}
                    className="text-sm font-medium text-slate-300 hover:text-white"
                  >
                    {link.label}
                  </a>
                ))}
                <Link to="/login" className="btn-gradient w-full text-sm" onClick={() => setOpen(false)}>
                  Login
                </Link>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </motion.header>
  )
}
