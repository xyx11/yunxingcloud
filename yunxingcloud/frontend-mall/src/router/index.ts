import { createRouter, createWebHistory } from 'vue-router'

const HomeView = () => import('@/views/HomeView.vue')
const ProductView = () => import('@/views/ProductView.vue')
const ProductListView = () => import('@/views/ProductListView.vue')
const SearchView = () => import('@/views/SearchView.vue')
const CartView = () => import('@/views/CartView.vue')
const CheckoutView = () => import('@/views/CheckoutView.vue')
const OrdersView = () => import('@/views/OrdersView.vue')
const OrderDetailView = () => import('@/views/OrderDetailView.vue')
const LoginView = () => import('@/views/LoginView.vue')
const RegisterView = () => import('@/views/RegisterView.vue')
const ProfileView = () => import('@/views/ProfileView.vue')
const PaymentResultView = () => import('@/views/PaymentResultView.vue')
const PayView = () => import('@/views/PayView.vue')
const PointsView = () => import('@/views/PointsView.vue')
const MallGiftCardView = () => import('@/views/GiftCardView.vue')
const LogisticsView = () => import('@/views/LogisticsView.vue')
const WishlistView = () => import('@/views/WishlistView.vue')

export default createRouter({
  history: createWebHistory('/mall'),
  routes: [
    { path: '/', component: HomeView },
    { path: '/products', component: ProductListView },
    { path: '/product/:id', component: ProductView },
    { path: '/search', component: SearchView },
    { path: '/cart', component: CartView },
    { path: '/checkout', component: CheckoutView },
    { path: '/orders', component: OrdersView },
    { path: '/order/:id', component: OrderDetailView },
    { path: '/login', component: LoginView },
    { path: '/register', component: RegisterView },
    { path: '/profile', component: ProfileView },
    { path: '/pay/:id', component: PayView },
    { path: '/order/:id/result', component: PaymentResultView },
    { path: '/points', component: PointsView },
    { path: '/gift-card', component: MallGiftCardView },
    { path: '/logistics', component: LogisticsView },
    { path: '/wishlist', component: WishlistView },
  ],
})
