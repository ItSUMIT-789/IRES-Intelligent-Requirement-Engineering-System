import { motion } from 'framer-motion'
import {
  ArrowRight,
  PlayCircle,
  ScanSearch,
  AlertTriangle,
  Layers,
  BookOpenText,
  GitBranch,
  FileOutput,
} from 'lucide-react'
import MainLayout from '../layouts/MainLayout.jsx'
import GradientButton from '../components/GradientButton.jsx'
import FeatureCard from '../components/FeatureCard.jsx'
import AIIllustration from '../components/AIIllustration.jsx'
import GlassCard from '../components/GlassCard.jsx'

const features = [
  {
    icon: ScanSearch,
    title: 'Requirement Analysis',
    description: 'Automatically parses raw requirement text and surfaces structure, intent and gaps.',
  },
  {
    icon: AlertTriangle,
    title: 'Ambiguity Detection',
    description: 'Flags vague, conflicting or untestable statements before they reach development.',
  },
  {
    icon: Layers,
    title: 'Requirement Classification',
    description: 'Sorts requirements into functional, non-functional and constraint categories.',
  },
  {
    icon: BookOpenText,
    title: 'User Story Generation',
    description: 'Converts approved requirements into ready-to-groom, developer-friendly user stories.',
  },
  {
    icon: GitBranch,
    title: 'Requirement Traceability',
    description: 'Links every requirement to its source, story and test case across the lifecycle.',
  },
  {
    icon: FileOutput,
    title: 'SRS Report Generation',
    description: 'Compiles a clean, standards-aligned SRS document ready for stakeholder sign-off.',
  },
]

const stats = [
  { value: '10x', label: 'Faster requirement review' },
  { value: '85%', label: 'Ambiguity caught pre-dev' },
  { value: '100%', label: 'Traceable specifications' },
]

const container = {
  hidden: { opacity: 0 },
  show: { opacity: 1, transition: { staggerChildren: 0.12 } },
}
const item = {
  hidden: { opacity: 0, y: 24 },
  show: { opacity: 1, y: 0, transition: { duration: 0.6, ease: 'easeOut' } },
}

export default function Landing() {
  return (
    <MainLayout>
      {/* HERO */}
      <section id="home" className="relative mx-auto max-w-6xl px-6 pt-40 pb-24 lg:pt-48">
        <div className="grid items-center gap-16 lg:grid-cols-2">
          <motion.div variants={container} initial="hidden" animate="show">
            <motion.span
              variants={item}
              className="glass mb-6 inline-flex items-center gap-2 rounded-full px-4 py-1.5 text-xs font-medium text-slate-300"
            >
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 animate-pulse-glow" />
              AI-Powered Requirement Engineering
            </motion.span>

            <motion.h1
              variants={item}
              className="font-display text-4xl font-bold leading-tight text-white sm:text-5xl lg:text-[3.4rem]"
            >
              Transform Software Requirements into{' '}
              <span className="gradient-text">Clear, Intelligent &amp; Actionable</span> Specifications
            </motion.h1>

            <motion.p variants={item} className="mt-6 max-w-xl text-base leading-relaxed text-slate-400">
              IRES uses AI to read raw requirement documents, detect ambiguity, classify needs,
              and generate traceable user stories and SRS reports — so your team builds the right
              thing, the first time.
            </motion.p>

            <motion.div variants={item} className="mt-9 flex flex-wrap items-center gap-4">
              <GradientButton to="/register" icon={ArrowRight}>
                Get Started
              </GradientButton>
              <GradientButton href="#features" variant="outline" icon={PlayCircle}>
                Learn More
              </GradientButton>
            </motion.div>

            <motion.div variants={item} className="mt-14 grid grid-cols-3 gap-6 max-w-md">
              {stats.map((s) => (
                <div key={s.label}>
                  <div className="font-display text-2xl font-bold gradient-text">{s.value}</div>
                  <div className="mt-1 text-xs text-slate-500">{s.label}</div>
                </div>
              ))}
            </motion.div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.8, ease: 'easeOut', delay: 0.2 }}
            className="animate-floaty"
          >
            <AIIllustration />
          </motion.div>
        </div>
      </section>

      {/* FEATURES */}
      <section id="features" className="relative mx-auto max-w-6xl px-6 py-24">
        <div className="mx-auto max-w-2xl text-center">
          <span className="text-xs font-semibold uppercase tracking-widest text-blue-300">Capabilities</span>
          <h2 className="mt-3 font-display text-3xl font-bold text-white sm:text-4xl">
            Everything requirements engineering needs, in one system
          </h2>
          <p className="mt-4 text-sm text-slate-400">
            Six focused capabilities that take a requirement from raw text to a validated,
            traceable specification.
          </p>
        </div>

        <div className="mt-14 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {features.map((f, i) => (
            <FeatureCard key={f.title} {...f} index={i} />
          ))}
        </div>
      </section>

      {/* ABOUT */}
      <section id="about" className="relative mx-auto max-w-6xl px-6 py-24">
        <GlassCard hover={false} className="grid gap-10 p-8 lg:grid-cols-2 lg:p-12">
          <div>
            <span className="text-xs font-semibold uppercase tracking-widest text-purple-300">About IRES</span>
            <h2 className="mt-3 font-display text-3xl font-bold text-white">
              Built for teams who are tired of requirement drift
            </h2>
            <p className="mt-5 text-sm leading-relaxed text-slate-400">
              IRES was designed to sit at the start of the software delivery lifecycle, where
              small misunderstandings become expensive rewrites. By combining natural language
              understanding with structured engineering workflows, it gives business analysts,
              developers, testers and clients a single, shared source of truth.
            </p>
            <p className="mt-4 text-sm leading-relaxed text-slate-400">
              Every requirement stays traceable from first draft to final test case, so nothing
              gets lost in translation between stakeholders.
            </p>
          </div>
          <div className="flex flex-col justify-center gap-4">
            {[
              'Consistent, ambiguity-free specifications',
              'Faster alignment between business and engineering',
              'A single traceable thread from idea to test case',
            ].map((line) => (
              <div key={line} className="glass flex items-center gap-3 rounded-xl px-4 py-3">
                <span className="h-2 w-2 shrink-0 rounded-full bg-gradient-to-br from-blue-400 to-purple-400" />
                <span className="text-sm text-slate-200">{line}</span>
              </div>
            ))}
          </div>
        </GlassCard>
      </section>
    </MainLayout>
  )
}
