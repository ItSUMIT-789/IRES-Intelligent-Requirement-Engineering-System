import { useRef, useState } from 'react'
import { UploadCloud, File, X } from 'lucide-react'

function formatSize(bytes) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

// Frontend-only: files are kept in local state to show the interaction,
// nothing is actually uploaded anywhere.
export default function FileDropzone({ accept = '.pdf,.docx,.csv', files, onChange }) {
  const [dragActive, setDragActive] = useState(false)
  const inputRef = useRef(null)

  const addFiles = (fileList) => {
    const incoming = Array.from(fileList)
    onChange([...files, ...incoming])
  }

  const removeFile = (index) => {
    onChange(files.filter((_, i) => i !== index))
  }

  return (
    <div>
      <div
        onDragOver={(e) => {
          e.preventDefault()
          setDragActive(true)
        }}
        onDragLeave={() => setDragActive(false)}
        onDrop={(e) => {
          e.preventDefault()
          setDragActive(false)
          if (e.dataTransfer.files?.length) addFiles(e.dataTransfer.files)
        }}
        onClick={() => inputRef.current?.click()}
        className={`flex cursor-pointer flex-col items-center justify-center rounded-xl border-2 border-dashed px-6 py-10 text-center transition-colors ${
          dragActive ? 'border-blue-400/60 bg-blue-500/10' : 'border-white/15 bg-white/[0.02] hover:bg-white/[0.04]'
        }`}
      >
        <div className="mb-3 flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500/20 to-purple-500/20 border border-white/10">
          <UploadCloud size={20} className="text-blue-300" />
        </div>
        <p className="text-sm font-medium text-slate-200">
          Drag &amp; drop files here, or <span className="text-blue-300">browse</span>
        </p>
        <p className="mt-1 text-xs text-slate-500">Supports PDF, DOCX and CSV</p>
        <input
          ref={inputRef}
          type="file"
          multiple
          accept={accept}
          className="hidden"
          onChange={(e) => {
            if (e.target.files?.length) addFiles(e.target.files)
            e.target.value = ''
          }}
        />
      </div>

      {files.length > 0 && (
        <ul className="mt-4 space-y-2">
          {files.map((f, i) => (
            <li
              key={`${f.name}-${i}`}
              className="flex items-center gap-3 rounded-xl border border-white/10 bg-white/[0.03] px-3.5 py-2.5"
            >
              <File size={16} className="shrink-0 text-slate-400" />
              <span className="truncate text-sm text-slate-200">{f.name}</span>
              <span className="ml-auto shrink-0 text-xs text-slate-500">{formatSize(f.size)}</span>
              <button
                type="button"
                onClick={() => removeFile(i)}
                className="shrink-0 rounded-lg p-1 text-slate-500 hover:bg-white/10 hover:text-red-300"
                aria-label={`Remove ${f.name}`}
              >
                <X size={14} />
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
