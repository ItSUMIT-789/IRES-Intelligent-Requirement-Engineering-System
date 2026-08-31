import { Sparkles, Github, Twitter, Linkedin, Mail } from 'lucide-react'

const socials = [
  { icon: Github, href: '#', label: 'GitHub' },
  { icon: Twitter, href: '#', label: 'Twitter' },
  { icon: Linkedin, href: '#', label: 'LinkedIn' },
  { icon: Mail, href: '#', label: 'Email' },
]

export default function Footer() {
  return (
    <footer className="relative border-t border-white/10 mt-24">
      <div className="mx-auto max-w-6xl px-6 py-12">
        <div className="flex flex-col items-center justify-between gap-6 md:flex-row">
          <div className="flex items-center gap-2">
            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-blue-500 to-purple-500">
              <Sparkles size={16} className="text-white" />
            </span>
            <span className="font-display font-semibold text-white">
              IRES <span className="gradient-text">AI</span>
            </span>
          </div>

          <p className="text-sm text-slate-400 text-center">
            Intelligent Requirement Engineering System — turning raw requirements into clarity.
          </p>

          <div className="flex items-center gap-3">
            {socials.map(({ icon: Icon, href, label }) => (
              <a
                key={label}
                href={href}
                aria-label={label}
                className="flex h-9 w-9 items-center justify-center rounded-lg border border-white/10 bg-white/5 text-slate-300 transition-all hover:border-white/30 hover:text-white hover:-translate-y-0.5"
              >
                <Icon size={16} />
              </a>
            ))}
          </div>
        </div>

        <div className="mt-8 border-t border-white/5 pt-6 text-center text-xs text-slate-500">
          © {new Date().getFullYear()} IRES AI. All rights reserved.
        </div>
      </div>
    </footer>
  )
}
