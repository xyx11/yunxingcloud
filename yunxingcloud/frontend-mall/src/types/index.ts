// ===== Core Domain Types =====

export interface Product {
  id: number
  name: string
  price: number
  originalPrice?: number
  imageUrl?: string
  images?: string[]
  description?: string
  sales?: number
  rating?: number
  stock?: number
  categoryId?: number
  brandId?: number
  isNew?: boolean
  isHot?: boolean
  badge?: string
  tags?: string[]
  skus?: Sku[]
  specs?: Spec[]
  _added?: boolean
  categoryName?: string
  brandName?: string
  createdAt?: string
}

export interface Sku {
  id: number
  productId: number
  name?: string
  specValues: string
  price: number
  stock: number
  imageUrl?: string
}

export interface Spec {
  name: string
  values: string[]
}

export interface Category {
  id: number
  name: string
  icon?: string
  parentId?: number
  children?: Category[]
}

export interface Banner {
  id: number
  title: string
  subtitle?: string
  imageUrl?: string
  linkUrl?: string
  sortOrder?: number
}

export interface CartItem {
  id: number
  productId: number
  productName: string
  productImage?: string
  price: number
  quantity: number
  selected: boolean
  skuId?: number
  stock: number
}

export interface OrderHead {
  id: number
  orderNo: string
  status: OrderStatus
  totalAmount: number
  payAmount: number
  freight: number
  couponDiscount: number
  createTime: string
  payTime?: string
  deliveryTime?: string
  items: OrderItem[]
  address?: Address
  receiverName?: string
  receiverPhone?: string
  receiverAddress?: string
  expiresAt?: string
}

export interface OrderItem {
  id: number
  productId: number
  productName: string
  productImage?: string
  price: number
  quantity: number
  skuInfo?: string
}

export type OrderStatus = 0 | 1 | 2 | 3 | 4 | 5 | '0' | '1' | '2' | '3' | '4' | '5'
// 0-待支付 1-已支付 2-已发货 3-已完成 4-已取消 5-售后中

export interface Address {
  id: number
  name: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: boolean
}

export interface Coupon {
  id: number
  name: string
  type: 'discount' | 'cash'
  value: number
  amount?: number
  minAmount: number
  startTime: string
  endTime: string
  status: 'available' | 'used' | 'expired'
  couponId?: number
}

export interface UserInfo {
  username: string
  nickname?: string
  avatar?: string
  phone?: string
  email?: string
  points?: number
  memberLevel?: string
}

export interface ApiResponse<T = unknown> {
  success: boolean
  message?: string
  data?: T
}

export interface PaginatedData<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

// ===== UI Types =====
export interface ToastItem {
  id: number
  message: string
  type: 'success' | 'error' | 'warning' | 'info'
}

export interface CompareItem {
  id: number
  name: string
  price: number
  imageUrl?: string
  sales?: number
  description?: string
}

export interface ViewedProduct {
  id: number
  name: string
  price: number
  imageUrl?: string
  viewedAt: string
}

// ===== API / Extra Types =====

export interface Review {
  id: number
  username: string
  rating: number
  content: string
  createdAt: string
}

export interface FavoriteItem {
  id: number
  productId: number
  imageUrl?: string
  productName?: string
  name?: string
  price?: number
}

export interface ApiError {
  response?: {
    data?: {
      message?: string
      code?: string
    }
  }
}

// ===== Event Types =====
export interface MouseEventWithTarget extends MouseEvent {
  target: HTMLElement
}

export interface TouchEventWithTarget extends TouchEvent {
  target: HTMLElement
}
