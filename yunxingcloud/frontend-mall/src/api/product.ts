import request from './request'

export interface Product {
  id: number; name: string; description?: string; price: number
  stock: number; imageUrl?: string; status: string
  categoryId?: number; brandId?: number; sales?: number
  createdAt?: string
}
export interface Category { id: number; name: string; description?: string }
export interface Brand { id: number; name: string; description?: string }
export interface Banner { id: number; title: string; imageUrl: string; link?: string; sort?: number }
export interface Sku { id: number; productId: number; name: string; price: number; stock: number; specs?: string }
export interface Review { id: number; productId: number; username: string; content: string; rating: number; createdAt?: string }

// Products
export const getProducts = (params?: Record<string, any>) => request.get('/products', { params })
export const getHotProducts = () => request.get('/products/hot')
export const getNewProducts = () => request.get('/products/new')
export const searchProducts = (q: string, page?: number, size?: number) =>
  request.get('/products/search', { params: { q, page: page || 1, size: size || 20 } })
export const getProductById = (id: number) => request.get(`/products/${id}`)
export const getProductSkus = (id: number) => request.get(`/products/${id}/skus`)
export const getProductReviews = (id: number) => request.get(`/products/${id}/reviews`)
export const getRelatedProducts = (id: number) => request.get(`/products/${id}/related`)
/** Aggregated: product + skus + reviews + related in one request */
export const getProductDetail = (id: number) => request.get(`/products/${id}/detail`)

// Categories
export const getCategories = () => request.get('/categories')

// Brands
export const getBrands = () => request.get('/brands')

// Banners
export const getBanners = () => request.get('/banners')
/** Aggregated: banners + hotProducts + newProducts + categories */
export const getHomeData = () => request.get('/home')

// Recommendations
export const getRecommend = () => request.get('/recommend/hot')
