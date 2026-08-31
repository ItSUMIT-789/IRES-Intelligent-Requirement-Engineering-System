import { useCallback, useEffect, useState } from 'react'
import { Download, RefreshCw, Users } from 'lucide-react'
import Badge from '../Badge.jsx'
import GlassCard from '../GlassCard.jsx'
import { adminUserService } from '../../services/adminUserService.js'
import { exportToCsv } from '../../utils/csv.js'
import { getRoleLabel } from '../../utils/roleRoutes.js'

const dateFormatter = new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' })

export default function AdminUsersPanel() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadUsers = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const response = await adminUserService.getUsers()
      setUsers(Array.isArray(response.data) ? response.data : [])
    } catch (requestError) {
      setUsers([])
      setError(requestError.message || 'Users could not be loaded.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { loadUsers() }, [loadUsers])

  const exportUsers = () => exportToCsv('ires-users.csv', users.map((user) => ({
    Name: user.name || [user.firstName, user.lastName].filter(Boolean).join(' '),
    Email: user.email,
    Username: user.username || '',
    Role: getRoleLabel(user.role),
    Status: user.active ? 'Active' : 'Inactive',
    'Created At': user.createdAt || '',
  })))

  return <>
    <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <span className="text-xs font-semibold uppercase tracking-widest text-blue-300">Admin</span>
        <h1 className="font-display text-2xl font-bold text-white sm:text-3xl">Users &amp; Roles</h1>
        <p className="mt-1 text-sm text-slate-400">{loading ? 'Loading users…' : `${users.length} users`}</p>
      </div>
      <button type="button" onClick={exportUsers} disabled={loading || Boolean(error) || users.length === 0}
        className="btn-outline inline-flex items-center justify-center gap-2 disabled:cursor-not-allowed disabled:opacity-50">
        <Download size={16} /> Export CSV
      </button>
    </div>

    {loading && <State message="Loading users…" />}
    {!loading && error && <State message={error} action="Retry" onAction={loadUsers} error />}
    {!loading && !error && users.length === 0 && <State message="No users found." />}
    {!loading && !error && users.length > 0 && <UserTable users={users} />}
  </>
}

function UserTable({ users }) {
  return <GlassCard hover={false} className="overflow-hidden">
    <div className="overflow-x-auto">
      <table className="w-full min-w-[760px] text-left text-sm">
        <thead className="border-b border-white/10 bg-white/[0.03] text-xs uppercase tracking-wider text-slate-500">
          <tr>{['Name', 'Email', 'Username', 'Role', 'Status', 'Created'].map((label) => <th key={label} className="px-5 py-4 font-semibold">{label}</th>)}</tr>
        </thead>
        <tbody className="divide-y divide-white/[0.07]">
          {users.map((user) => <tr key={user.id} className="transition-colors hover:bg-white/[0.03]">
            <td className="px-5 py-4 font-medium text-slate-100">{user.name || [user.firstName, user.lastName].filter(Boolean).join(' ')}</td>
            <td className="px-5 py-4 text-slate-300">{user.email}</td>
            <td className="px-5 py-4 text-slate-400">{user.username || '—'}</td>
            <td className="px-5 py-4"><Badge variant="purple">{getRoleLabel(user.role)}</Badge></td>
            <td className="px-5 py-4"><Badge variant={user.active ? 'success' : 'neutral'}>{user.active ? 'Active' : 'Inactive'}</Badge></td>
            <td className="px-5 py-4 text-slate-400">{user.createdAt ? dateFormatter.format(new Date(user.createdAt)) : '—'}</td>
          </tr>)}
        </tbody>
      </table>
    </div>
  </GlassCard>
}

function State({ message, action, onAction, error }) {
  return <GlassCard hover={false} className="p-8 text-center">
    <Users size={28} className={`mx-auto mb-3 ${error ? 'text-red-300' : 'text-slate-500'}`} />
    <p className={error ? 'text-red-300' : 'text-slate-400'}>{message}</p>
    {action && <button type="button" onClick={onAction} className="btn-outline mt-4 inline-flex items-center gap-2"><RefreshCw size={15} />{action}</button>}
  </GlassCard>
}
