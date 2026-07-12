import { useEffect, useState } from 'react'
import { getProfile, updateProfile } from '../../api/user'

export default function Profile() {
  const [form, setForm] = useState({ username: '', email: '', password: '', address: '' })
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    getProfile().then((res) => {
      setForm({ ...res.data, password: '' })
    }).catch(() => {})
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setMsg('')
    setError('')
    try {
      await updateProfile(form)
      setMsg('Profile updated successfully')
    } catch {
      setError('Update failed')
    }
  }

  return (
    <div className="max-w-lg mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-800 mb-6">My Profile</h1>
      {msg && <p className="text-green-600 text-sm mb-4">{msg}</p>}
      {error && <p className="text-red-500 text-sm mb-4">{error}</p>}
      <form onSubmit={handleSubmit} className="bg-white shadow rounded-lg p-6 space-y-4">
        {[
          { label: 'Username', key: 'username', type: 'text' },
          { label: 'Email', key: 'email', type: 'email' },
          { label: 'New Password', key: 'password', type: 'password' },
          { label: 'Address', key: 'address', type: 'text' },
        ].map(({ label, key, type }) => (
          <div key={key}>
            <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
            <input
              type={type}
              value={form[key] || ''}
              onChange={(e) => setForm({ ...form, [key]: e.target.value })}
              className="w-full border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
        ))}
        <button
          type="submit"
          className="w-full bg-indigo-600 hover:bg-indigo-700 text-white py-2 rounded font-medium text-sm"
        >
          Save Changes
        </button>
      </form>
    </div>
  )
}
