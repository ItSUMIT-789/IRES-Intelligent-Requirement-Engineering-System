import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import GlassCard from '../GlassCard.jsx'
import { AdminHeader } from '../../pages/dashboards/AdminDashboard.jsx'
import { adminUserService } from '../../services/adminUserService.js'
import { projectService } from '../../services/projectService.js'
import { Error, Status } from '../client/ClientProjects.jsx'

const assignableRoles = ['DEVELOPER', 'TESTER']
const roleLabel = (role) => role === 'DEVELOPER' ? 'Developer' : 'Tester'

export default function AdminProjectMembers() {
  const { id } = useParams()
  const [project, setProject] = useState()
  const [members, setMembers] = useState([])
  const [users, setUsers] = useState([])
  const [memberRole, setMemberRole] = useState('DEVELOPER')
  const [userId, setUserId] = useState('')
  const [adding, setAdding] = useState(false)
  const [error, setError] = useState('')

  const availableUsers = (loadedUsers = users, loadedMembers = members, role = memberRole) => {
    const memberIds = new Set(loadedMembers.map((member) => member.user?.id))
    return loadedUsers.filter((user) => user.active && user.role === role && !memberIds.has(user.id))
  }

  const load = async () => {
    setError('')
    try {
      const [projectResult, memberResult, userResult] = await Promise.all([
        projectService.getProject(id),
        projectService.getProjectMembers(id),
        adminUserService.getUsers(),
      ])
      const loadedMembers = memberResult.data || []
      const loadedUsers = userResult.data || []
      setProject(projectResult.data)
      setMembers(loadedMembers)
      setUsers(loadedUsers)
      setUserId(availableUsers(loadedUsers, loadedMembers, memberRole)[0]?.id || '')
    } catch (e) {
      setError(e.message)
    }
  }

  useEffect(() => { load() }, [id])

  const changeRole = (role) => {
    setMemberRole(role)
    setUserId(availableUsers(users, members, role)[0]?.id || '')
  }

  const addMember = async () => {
    if (!userId) return
    setAdding(true)
    setError('')
    try {
      await projectService.addProjectMember(id, { userId, projectRole: memberRole })
      await load()
    } catch (e) {
      setError(e.message)
    } finally {
      setAdding(false)
    }
  }

  if (error && !project) return <Error message={error} />
  if (!project) return <GlassCard hover={false} className="p-6 text-slate-400">Loading project members…</GlassCard>

  const eligible = availableUsers()
  return <>
    <AdminHeader title={project.name} subtitle="Manage Developer and Tester project membership before workflow assignment." action={<Link className="text-sm text-blue-300 hover:text-blue-200" to="/admin/projects">← Projects</Link>} />
    {error && <Error message={error} />}
    <GlassCard hover={false} className="mb-6 p-6">
      <div className="flex flex-wrap items-center justify-between gap-3"><div><h2 className="text-lg font-semibold text-white">Project Members</h2><p className="mt-1 text-sm text-slate-400">Only active users whose canonical role matches the selected project role are available.</p></div><Status value={project.status} /></div>
      <div className="mt-5 grid gap-3 sm:grid-cols-2">{assignableRoles.map((role) => { const roleMembers = members.filter((member) => member.projectRole === role); return <div key={role} className="rounded-xl border border-white/10 p-4"><h3 className="font-medium text-white">{roleLabel(role)} Members</h3><div className="mt-3 space-y-2">{roleMembers.map((member) => <p key={member.id} className="text-sm text-slate-300"><span className="font-medium text-white">{member.user?.name}</span><span className="ml-2 text-slate-500">{member.user?.email}</span></p>)}{roleMembers.length === 0 && <p className="text-sm text-amber-300">No {roleLabel(role)} is currently a member.</p>}</div></div> })}</div>
      <div className="mt-5 grid gap-3 sm:grid-cols-[180px_minmax(0,1fr)_auto]"><select value={memberRole} onChange={(event) => changeRole(event.target.value)} className="rounded-xl border border-white/10 bg-space-900 px-3 py-3">{assignableRoles.map((role) => <option key={role} value={role}>{roleLabel(role)}</option>)}</select><select value={userId} onChange={(event) => setUserId(event.target.value)} className="min-w-0 rounded-xl border border-white/10 bg-space-900 px-3 py-3"><option value="">Select an active {roleLabel(memberRole)}</option>{eligible.map((user) => <option key={user.id} value={user.id}>{user.name} · {user.email}</option>)}</select><button className="btn-gradient disabled:opacity-50" disabled={!userId || adding} onClick={addMember}>{adding ? 'Adding…' : `Add ${roleLabel(memberRole)}`}</button></div>
      {eligible.length === 0 && <p className="mt-3 text-sm text-slate-500">No additional active {roleLabel(memberRole)} users are available.</p>}
    </GlassCard>
  </>
}
