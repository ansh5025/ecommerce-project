import { useEffect, useState } from 'react'
import { getCart, removeFromCart } from '../../api/cart'

export default function Cart() {
  const [cartItems, setCartItems] = useState([])
  const [loading, setLoading] = useState(true)

  const fetchCart = () => {
    getCart()
      .then((res) => setCartItems(res.data))
      .catch(() => {})
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchCart() }, [])

  const handleRemove = async (productId) => {
    await removeFromCart(productId)
    fetchCart()
  }

  const total = cartItems.reduce((sum, item) => sum + (item.product?.price || 0), 0)

  if (loading) return <div className="p-8 text-gray-500">Loading cart...</div>

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-800 mb-6">Your Cart</h1>
      {cartItems.length === 0 ? (
        <p className="text-gray-500">Your cart is empty.</p>
      ) : (
        <>
          <div className="space-y-4">
            {cartItems.map((item) => (
              <div key={item.id?.productId} className="bg-white shadow rounded-lg p-4 flex items-center gap-4">
                <img
                  src={item.product?.image || 'https://placehold.co/80x80?text=P'}
                  alt={item.product?.name}
                  className="w-20 h-20 object-cover rounded"
                />
                <div className="flex-1">
                  <h3 className="font-semibold text-gray-800">{item.product?.name}</h3>
                  <p className="text-sm text-gray-500">{item.product?.category?.name}</p>
                  <p className="text-indigo-700 font-bold mt-1">₹{item.product?.price}</p>
                </div>
                <button
                  onClick={() => handleRemove(item.product?.id)}
                  className="text-red-500 hover:text-red-700 text-sm"
                >
                  Remove
                </button>
              </div>
            ))}
          </div>
          <div className="mt-6 bg-white shadow rounded-lg p-4 flex justify-between items-center">
            <span className="text-gray-700 font-semibold text-lg">Total</span>
            <span className="text-indigo-700 font-bold text-xl">₹{total}</span>
          </div>
          <button className="mt-4 w-full bg-green-600 hover:bg-green-700 text-white py-2 rounded font-medium">
            Proceed to Checkout
          </button>
        </>
      )}
    </div>
  )
}
