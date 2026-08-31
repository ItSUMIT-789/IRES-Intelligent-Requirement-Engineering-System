// AuroraBackground — the signature visual of IRES.
// A living, slowly drifting field of aurora light behind a faint circuit grid,
// evoking an AI system continuously "reading" and re-analyzing requirements.
export default function AuroraBackground() {
  return (
    <div className="fixed inset-0 -z-10 overflow-hidden bg-space-950">
      {/* base gradient wash */}
      <div className="absolute inset-0 bg-aurora-radial" />

      {/* drifting aurora blobs */}
      <div className="absolute -top-40 -left-40 h-[36rem] w-[36rem] rounded-full bg-aurora-indigo/30 blur-[110px] animate-drift1" />
      <div className="absolute top-1/3 -right-40 h-[40rem] w-[40rem] rounded-full bg-aurora-purple/25 blur-[120px] animate-drift2" />
      <div className="absolute bottom-[-10rem] left-1/4 h-[30rem] w-[30rem] rounded-full bg-aurora-cyan/20 blur-[100px] animate-drift3" />
      <div className="absolute bottom-0 right-1/4 h-[26rem] w-[26rem] rounded-full bg-aurora-blue/20 blur-[100px] animate-drift1" style={{ animationDelay: '-8s' }} />

      {/* faint circuit / spec grid, reinforcing "engineering" */}
      <div className="absolute inset-0 bg-grid-glow bg-grid opacity-[0.35] [mask-image:radial-gradient(ellipse_60%_60%_at_50%_20%,#000_10%,transparent_75%)]" />

      {/* scanning highlight line, evokes AI parsing a document top to bottom */}
      <div className="absolute inset-x-0 top-0 h-[2px] bg-gradient-to-r from-transparent via-aurora-cyan/70 to-transparent animate-shimmer bg-[length:200%_100%]" />

      {/* vignette so foreground content stays legible */}
      <div className="absolute inset-0 bg-gradient-to-b from-space-950/40 via-transparent to-space-950" />
      <div className="absolute inset-0 bg-space-950/30" />
    </div>
  )
}
