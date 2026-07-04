import request from '@/api/request'

export interface Product {
  id: number; name: string; description: string; price: number; stock: number
  imageUrl: string; status: string; createdAt: string
}

export function fetchProducts() { return request.get('/api/products') }
export function createProduct(data: Partial<Product>) { return request.post('/api/products', data) }
export function updateProduct(id: number, data: Partial<Product>) { return request.put(`/api/products/${id}`, data) }
export function deleteProduct(id: number) { return request.delete(`/api/products/${id}`) }
export function fetchCategories() { return request.get('/api/categories') }
