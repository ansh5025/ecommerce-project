import api from './axios'

export const getCart = () => api.get('/cart')
export const addToCart = (productId) => api.post(`/cart/add?productId=${productId}`)
export const removeFromCart = (productId) => api.delete(`/cart/remove?productId=${productId}`)
