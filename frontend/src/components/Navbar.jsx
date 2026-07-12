import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { logout } from '../api/auth'

export default function Navbar() {
  const { user, setUser } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logout()
    setUser(null)
    navigate('/login')
  }

  return (
    <nav className="bg-gray-900 text-white px-6 py-3 flex items-center justify-between shadow-md">
      <Link to="/" className="text-xl font-bold tracking-wide">ShopApp</Link>
      <div className="flex items-center gap-4">
        {user ? (
          <>
            <Link to="/products" className="hover:text-gray-300 text-sm">Products</Link>
            <Link to="/cart" className="hover:text-gray-300 text-sm">Cart</Link>
            <Link to="/profile" className="hover:text-gray-300 text-sm">{user.username}</Link>
            <button
              onClick={handleLogout}
              className="bg-red-600 hover:bg-red-700 text-sm px-3 py-1 rounded"
            >
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="hover:text-gray-300 text-sm">Login</Link>
            <Link to="/register" className="bg-indigo-600 hover:bg-indigo-700 text-sm px-3 py-1 rounded">
              Register
            </Link>
          </>
        )}
      </div>
    </nav>
  )
}
