import { useState } from 'react'
import { Eye, EyeOff } from 'lucide-react'

export default function InputField({ label, icon: Icon, type = 'text', id, ...props }) {
  const [show, setShow] = useState(false)
  const isPassword = type === 'password'
  const inputType = isPassword ? (show ? 'text' : 'password') : type

  return (
    <div>
      {label && (
        <label htmlFor={id} className="mb-1.5 block text-sm font-medium text-slate-300">
          {label}
        </label>
      )}
      <div className="group relative">
        {Icon && (
          <Icon
            size={17}
            className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500 transition-colors group-focus-within:text-blue-300"
          />
        )}
        <input
          id={id}
          type={inputType}
          className={`w-full rounded-xl border border-white/10 bg-white/[0.04] py-2.5 text-sm text-slate-100
            placeholder:text-slate-500 outline-none transition-all duration-200
            focus:border-blue-400/60 focus:bg-white/[0.07] focus:ring-2 focus:ring-blue-500/20
            ${Icon ? 'pl-10' : 'pl-4'} ${isPassword ? 'pr-10' : 'pr-4'}`}
          {...props}
        />
        {isPassword && (
          <button
            type="button"
            tabIndex={-1}
            onClick={() => setShow((v) => !v)}
            className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-500 transition-colors hover:text-slate-300"
            aria-label={show ? 'Hide password' : 'Show password'}
          >
            {show ? <EyeOff size={17} /> : <Eye size={17} />}
          </button>
        )}
      </div>
    </div>
  )
}
