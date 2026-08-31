import { useRef } from 'react'
import { Bold, Italic, Underline, List, ListOrdered } from 'lucide-react'

const tools = [
  { icon: Bold, command: 'bold', label: 'Bold' },
  { icon: Italic, command: 'italic', label: 'Italic' },
  { icon: Underline, command: 'underline', label: 'Underline' },
  { icon: List, command: 'insertUnorderedList', label: 'Bulleted list' },
  { icon: ListOrdered, command: 'insertOrderedList', label: 'Numbered list' },
]

export default function RichTextEditor({ onChange, placeholder = 'Describe the requirement in detail…' }) {
  const editorRef = useRef(null)

  const runCommand = (command) => {
    editorRef.current?.focus()
    document.execCommand(command)
    onChange?.(editorRef.current?.innerHTML || '')
  }

  return (
    <div className="overflow-hidden rounded-xl border border-white/10 bg-white/[0.03]">
      <div className="flex items-center gap-1 border-b border-white/10 px-2 py-1.5">
        {tools.map(({ icon: Icon, command, label }) => (
          <button
            key={command}
            type="button"
            onClick={() => runCommand(command)}
            aria-label={label}
            title={label}
            className="rounded-lg p-1.5 text-slate-400 hover:bg-white/10 hover:text-white"
          >
            <Icon size={15} />
          </button>
        ))}
      </div>
      <div
        ref={editorRef}
        contentEditable
        suppressContentEditableWarning
        data-placeholder={placeholder}
        onInput={(e) => onChange?.(e.currentTarget.innerHTML)}
        className="min-h-[9rem] px-4 py-3 text-sm text-slate-100 outline-none empty:before:text-slate-500 empty:before:content-[attr(data-placeholder)]"
      />
    </div>
  )
}
