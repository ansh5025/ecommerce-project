import { useEffect, useState } from 'react'
import { getCategories, addCategory, updateCategory, deleteCategory } from '../../api/admin'
import { Link } from 'react-router-dom'

export default function AdminCategories() {
  const [categories, setCategories] = useState([])
  const [newName, setNewName] = useState('')
  const [editing, setEditing] = useState(null)
  const [editName, setEditName] = useState('')

  const load = () => getCategories().then((r) => setCategories(r.data)).catch(() => {})

  useEffect(() => { load() }, [])

  const handleAdd = async (e) => {
    e.preventDefault()
    if (!newName.trim()) return
    await addCategory(newName.trim())
    setNewName('')
    load()
  }

  const handleUpdate = async (id) => {
    await updateCategory(id, editName.trim())
    setEditing(null)
    load()
  }

  const handleDelete = async (id) => {
    if (!confirm('Delete this category?')) return
    await deleteCategory(id)
    load()
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-3xl font-bold text-gray-800">Categories</h1>
        <Link to="/admin" className="text-indigo-600 text-sm hover:underline">← Dashboard</Link>
      </div>

      <form onSubmit={handleAdd} className="flex gap-2 mb-6">
        <input
          type="text"
          value={newName}
          onChange={(e) => setNewName(e.target.value)}
          placeholder="New category name"
          className="flex-1 border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
        <button type="submit" className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded text-sm">
          Add
        </button>
      </form>

      <div className="bg-white shadow rounded-lg overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
            <tr>
              <th className="px-4 py-3 text-left">ID</th>
              <th className="px-4 py-3 text-left">Name</th>
              <th className="px-4 py-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {categories.map((c) => (
              <tr key={c.id} className="border-t border-gray-100 hover:bg-gray-50">
                <td className="px-4 py-3 text-gray-500">{c.id}</td>
                <td className="px-4 py-3">
                  {editing === c.id ? (
                    <input
                      value={editName}
                      onChange={(e) => setEditName(e.target.value)}
                      className="border border-gray-300 rounded px-2 py-1 text-sm w-full"
                    />
                  ) : (
                    c.name
                  )}
                </td>
                <td className="px-4 py-3 text-right space-x-2">
                  {editing === c.id ? (
                    <>
                      <button onClick={() => handleUpdate(c.id)} className="text-green-600 hover:underline">Save</button>
                      <button onClick={() => setEditing(null)} className="text-gray-500 hover:underline">Cancel</button>
                    </>
                  ) : (
                    <>
                      <button onClick={() => { setEditing(c.id); setEditName(c.name) }} className="text-indigo-600 hover:underline">Edit</button>
                      <button onClick={() => handleDelete(c.id)} className="text-red-500 hover:underline">Delete</button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
