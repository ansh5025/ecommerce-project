import api from './axios'

// Customers
export const getCustomers = () => api.get('/admin/customers')

// Categories
export const getCategories = () => api.get('/admin/categories')
export const addCategory = (name) => api.post('/admin/categories', { name })
export const updateCategory = (id, name) => api.put(`/admin/categories/${id}`, { name })
export const deleteCategory = (id) => api.delete(`/admin/categories/${id}`)

// Products
export const getAdminProducts = () => api.get('/admin/products')
export const addProduct = (data) => api.post('/admin/products', data)
export const updateProduct = (id, data) => api.put(`/admin/products/${id}`, data)
export const deleteProduct = (id) => api.delete(`/admin/products/${id}`)
