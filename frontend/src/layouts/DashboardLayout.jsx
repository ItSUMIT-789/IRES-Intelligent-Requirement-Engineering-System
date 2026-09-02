import { useState } from 'react'
import { AnimatePresence, motion } from 'framer-motion'
import { X } from 'lucide-react'
import Sidebar from '../components/Sidebar.jsx'
import Topbar from '../components/Topbar.jsx'

export default function DashboardLayout({ children, items, active, onSelect }) {
  const [mobileOpen, setMobileOpen] = useState(false)

  const handleSelect = (key) => {
    onSelect(key)
    setMobileOpen(false)
  }

  return (
    <div className="relative flex min-h-screen bg-[#F8FAF7]">
      <div className="hidden lg:block">
        <Sidebar items={items} active={active} onSelect={onSelect} />
      </div>

      {/* mobile drawer */}
      <AnimatePresence>
        {mobileOpen && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 z-40 bg-black/60 lg:hidden"
              onClick={() => setMobileOpen(false)}
            />
            <motion.div
              initial={{ x: -280 }}
              animate={{ x: 0 }}
              exit={{ x: -280 }}
              transition={{ duration: 0.25, ease: 'easeOut' }}
              className="fixed inset-y-0 left-0 z-50 w-64 bg-white shadow-xl lg:hidden"
            >
              <button
                onClick={() => setMobileOpen(false)}
                className="absolute right-4 top-4 rounded-lg p-1.5 text-slate-300 hover:bg-white/10"
                aria-label="Close menu"
              >
                <X size={18} />
              </button>
              <Sidebar items={items} active={active} onSelect={handleSelect} />
            </motion.div>
          </>
        )}
      </AnimatePresence>

      <div className="flex min-h-screen min-w-0 flex-1 flex-col">
        <Topbar onMenuClick={() => setMobileOpen(true)} />
        <main className="min-w-0 flex-1 px-5 py-8 lg:px-8">{children}</main>
      </div>
    </div>
  )
}
