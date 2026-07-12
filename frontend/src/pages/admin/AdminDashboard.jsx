import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { logout } from '../../api/auth'
import { useNavigate } from 'react-router-dom'

export default function AdminDashboard() {
  const { user, setUser } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logout()
    setUser(null)
    navigate('/admin/login')
  }

  const cards = [
    { label: 'Products', to: '/admin/products', color: 'bg-indigo-600' },
    { label: 'Categories', to: '/admin/categories', color: 'bg-purple-600' },
    { label: 'Customers', to: '/admin/customers', color: 'bg-green-600' },
  ]

  return (
    <div className="min-h-screen bg-gray-100">
      <nav className="bg-gray-900 text-white px-6 py-3 flex items-center justify-between">
        <span className="text-xl font-bold">Admin Panel</span>
        <div className="flex items-center gap-4">
          <span className="text-sm text-gray-300">Hello, {user?.username}</span>
          <button onClick={handleLogout} className="bg-red-600 hover:bg-red-700 text-sm px-3 py-1 rounded">
            Logout
          </button>
        </div>
      </nav>
      <div className="max-w-4xl mx-auto px-4 py-10">
        <h1 className="text-3xl font-bold text-gray-800 mb-8">Dashboard</h1>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
          {cards.map((c) => (
            <Link
              key={c.to}
              to={c.to}
              className={`${c.color} text-white rounded-lg p-6 text-center text-xl font-semibold shadow hover:opacity-90 transition`}
            >
              {c.label}
            </Link>
          ))}
        </div>
      </div>
    </div>
  )
}
