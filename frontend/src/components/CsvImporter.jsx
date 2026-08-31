import { useRef, useState } from 'react'
import Papa from 'papaparse'
import { FileSpreadsheet, Upload, Download, AlertCircle } from 'lucide-react'
import { exportToCsv } from '../utils/csv.js'

const TEMPLATE_ROWS = [
  { title: 'Guest checkout support', category: 'Functional', description: 'Users can check out without an account.' },
  { title: 'Page load under 2s', category: 'Non-Functional', description: '95% of pages load within 2 seconds.' },
]

// onImport receives the parsed rows (array of objects) when the user confirms.
export default function CsvImporter({ onImport }) {
  const inputRef = useRef(null)
  const [rows, setRows] = useState([])
  const [fileName, setFileName] = useState('')
  const [error, setError] = useState('')

  const handleFile = (file) => {
    if (!file) return
    if (!file.name.toLowerCase().endsWith('.csv')) {
      setError('Please upload a .csv file.')
      return
    }
    setError('')
    setFileName(file.name)
    Papa.parse(file, {
      header: true,
      skipEmptyLines: true,
      complete: (results) => {
        if (!results.data.length) {
          setError('That CSV has no rows to import.')
          setRows([])
          return
        }
        setRows(results.data)
      },
      error: () => setError('Could not parse that file — check it is valid CSV.'),
    })
  }

  const confirmImport = () => {
    onImport(rows)
    setRows([])
    setFileName('')
  }

  const columns = rows.length ? Object.keys(rows[0]) : []

  return (
    <div>
      <div className="flex flex-wrap items-center gap-3">
        <button
          type="button"
          onClick={() => inputRef.current?.click()}
          className="btn-outline !py-2.5 text-sm"
        >
          <Upload size={15} />
          Upload requirements CSV
        </button>
        <button
          type="button"
          onClick={() => exportToCsv('ires-requirements-template', TEMPLATE_ROWS)}
          className="flex items-center gap-2 rounded-xl px-4 py-2.5 text-sm font-medium text-slate-300 hover:text-white"
        >
          <Download size={15} />
          Download CSV template
        </button>
        <input
          ref={inputRef}
          type="file"
          accept=".csv"
          className="hidden"
          onChange={(e) => {
            handleFile(e.target.files?.[0])
            e.target.value = ''
          }}
        />
      </div>

      {error && (
        <p className="mt-3 flex items-center gap-2 text-sm text-red-300">
          <AlertCircle size={14} />
          {error}
        </p>
      )}

      {rows.length > 0 && (
        <div className="mt-4 overflow-hidden rounded-xl border border-white/10">
          <div className="flex items-center justify-between border-b border-white/10 bg-white/[0.03] px-4 py-2.5">
            <span className="flex items-center gap-2 text-xs text-slate-400">
              <FileSpreadsheet size={14} className="text-emerald-300" />
              {fileName} · {rows.length} row{rows.length === 1 ? '' : 's'} detected
            </span>
            <button type="button" onClick={confirmImport} className="btn-gradient !px-4 !py-1.5 text-xs">
              Import all
            </button>
          </div>
          <div className="max-h-56 overflow-auto">
            <table className="w-full text-left text-xs">
              <thead className="sticky top-0 bg-space-900/95 text-slate-400">
                <tr>
                  {columns.map((c) => (
                    <th key={c} className="whitespace-nowrap px-4 py-2 font-medium capitalize">
                      {c}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5 text-slate-300">
                {rows.map((row, i) => (
                  <tr key={i}>
                    {columns.map((c) => (
                      <td key={c} className="max-w-[16rem] truncate px-4 py-2">
                        {row[c]}
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
