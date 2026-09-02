/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        display: ['Poppins', 'sans-serif'],
        body: ['Inter', 'sans-serif'],
      },
      colors: {
        brand: {
          green: '#15803D',
          dark: '#166534',
          soft: '#DCFCE7',
          pale: '#F0FDF4',
          yellow: '#FACC15',
          amber: '#CA8A04',
          ink: '#17211B',
          muted: '#647067',
          border: '#E5E7EB',
          canvas: '#F8FAF7',
        },
        space: {
          950: '#F8FAF7',
          900: '#FFFFFF',
          800: '#F3F5F3',
          700: '#E5E7EB',
        },
        aurora: {
          blue: '#3b82f6',
          indigo: '#6366f1',
          violet: '#8b5cf6',
          purple: '#a855f7',
          cyan: '#22d3ee',
        },
      },
      backgroundImage: {
        'aurora-radial':
          'radial-gradient(circle at 20% 20%, rgba(99,102,241,0.35), transparent 45%), radial-gradient(circle at 80% 30%, rgba(168,85,247,0.30), transparent 50%), radial-gradient(circle at 50% 80%, rgba(34,211,238,0.20), transparent 45%)',
        'grid-glow':
          'linear-gradient(rgba(255,255,255,0.04) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.04) 1px, transparent 1px)',
      },
      backgroundSize: {
        grid: '48px 48px',
      },
      keyframes: {
        drift1: {
          '0%, 100%': { transform: 'translate(0px, 0px) scale(1)' },
          '33%': { transform: 'translate(60px, -40px) scale(1.15)' },
          '66%': { transform: 'translate(-40px, 30px) scale(0.95)' },
        },
        drift2: {
          '0%, 100%': { transform: 'translate(0px, 0px) scale(1)' },
          '33%': { transform: 'translate(-70px, 50px) scale(1.1)' },
          '66%': { transform: 'translate(50px, -30px) scale(0.9)' },
        },
        drift3: {
          '0%, 100%': { transform: 'translate(0px, 0px) scale(1)' },
          '50%': { transform: 'translate(30px, 60px) scale(1.2)' },
        },
        floaty: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-14px)' },
        },
        shimmer: {
          '0%': { backgroundPosition: '0% 50%' },
          '100%': { backgroundPosition: '200% 50%' },
        },
        'pulse-glow': {
          '0%, 100%': { opacity: 0.6 },
          '50%': { opacity: 1 },
        },
      },
      animation: {
        drift1: 'drift1 22s ease-in-out infinite',
        drift2: 'drift2 26s ease-in-out infinite',
        drift3: 'drift3 18s ease-in-out infinite',
        floaty: 'floaty 6s ease-in-out infinite',
        shimmer: 'shimmer 6s linear infinite',
        'pulse-glow': 'pulse-glow 3.5s ease-in-out infinite',
      },
      boxShadow: {
        glow: '0 1px 3px rgba(23, 33, 27, 0.10)',
        'glow-blue': '0 1px 3px rgba(23, 33, 27, 0.10)',
      },
    },
  },
  plugins: [],
}
