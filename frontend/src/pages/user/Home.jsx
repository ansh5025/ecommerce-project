import { useEffect, useState } from 'react'
import { getProducts } from '../../api/products'
import { addToCart } from '../../api/cart'

export default function Home() {
  const [products, setProducts] = useState([])
  const [msg, setMsg] = useState('')

  useEffect(() => {
    getProducts().then((res) => setProducts(res.data)).catch(() => {})
  }, [])

  const handleAddToCart = async (id) => {
    try {
      await addToCart(id)
      setMsg('Added to cart!')
      setTimeout(() => setMsg(''), 2000)
    } catch {
      setMsg('Failed to add to cart')
    }
  }

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-800 mb-2">All Products</h1>
      {msg && <p className="text-green-600 text-sm mb-4">{msg}</p>}
      {products.length === 0 ? (
        <p className="text-gray-500">No products available.</p>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 mt-6">
          {products.map((p) => (
            <div key={p.id} className="bg-white rounded-lg shadow hover:shadow-md transition p-4 flex flex-col">
              <img
                src={p.image || 'https://placehold.co/300x200?text=Product'}
                alt={p.name}
                className="w-full h-48 object-cover rounded mb-3"
              />
              <h3 className="font-semibold text-gray-800 text-lg">{p.name}</h3>
              <p className="text-sm text-gray-500 mb-1">{p.category?.name}</p>
              <p className="text-sm text-gray-600 mb-3 flex-1">{p.description}</p>
              <div className="flex items-center justify-between">
                <span className="text-indigo-700 font-bold text-lg">₹{p.price}</span>
                <button
                  onClick={() => handleAddToCart(p.id)}
                  className="bg-indigo-600 hover:bg-indigo-700 text-white text-sm px-3 py-1 rounded"
                >
                  Add to Cart
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
