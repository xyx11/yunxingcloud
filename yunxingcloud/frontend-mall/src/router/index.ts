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
const CouponCenterView = () => import('@/views/CouponCenterView.vue')
const AfterSaleView = () => import('@/views/AfterSaleView.vue')
const GroupBuyView = () => import('@/views/GroupBuyView.vue')
const FlashSaleView = () => import('@/views/FlashSaleView.vue')
const InvoiceView = () => import('@/views/InvoiceView.vue')
const RecentView = () => import('@/views/RecentView.vue')
const RankingView = () => import('@/views/RankingView.vue')
const HelpView = () => import('@/views/HelpView.vue')
const NotFoundView = () => import('@/views/NotFoundView.vue')

const pageTitles: Record<string, string> = {
  '/': 'YXCLOUD 商城 - 品质生活，一站购齐',
  '/products': '全部商品 - YXCLOUD',
  '/search': '商品搜索 - YXCLOUD',
  '/cart': '购物车 - YXCLOUD',
  '/checkout': '结算 - YXCLOUD',
  '/orders': '我的订单 - YXCLOUD',
  '/login': '登录 - YXCLOUD',
  '/register': '注册 - YXCLOUD',
  '/profile': '个人中心 - YXCLOUD',
  '/points': '积分中心 - YXCLOUD',
  '/gift-card': '礼品卡 - YXCLOUD',
  '/logistics': '物流追踪 - YXCLOUD',
  '/wishlist': '收藏夹 - YXCLOUD',
  '/coupons': '优惠券中心 - YXCLOUD',
  '/after-sale': '售后 - YXCLOUD',
  '/group-buy': '拼团专区 - YXCLOUD',
  '/flash-sale': '限时秒杀 - YXCLOUD',
  '/invoices': '发票管理 - YXCLOUD',
  '/recent': '最近浏览 - YXCLOUD',
  '/ranking': '排行榜 - YXCLOUD',
  '/help': '帮助中心 - YXCLOUD',
}

// Track page load progress
let progressTimer: ReturnType<typeof setInterval> | null = null
function startProgress() {
  const bar = document.getElementById('nprogress-bar')
  if (!bar) return
  bar.style.transition = 'none'
  bar.style.width = '0%'
  requestAnimationFrame(() => {
    bar.style.transition = 'width .4s ease'
    bar.style.width = '30%'
  })
  let w = 30
  progressTimer = setInterval(() => {
    w += (100 - w) * 0.2
    bar.style.width = `${Math.min(w, 85)}%`
  }, 400)
}
function finishProgress() {
  if (progressTimer) clearInterval(progressTimer)
  const bar = document.getElementById('nprogress-bar')
  if (!bar) return
  bar.style.width = '100%'
  setTimeout(() => { if (bar) bar.style.width = '0%' }, 300)
}

const router = createRouter({
  history: createWebHistory('/'),
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0, behavior: 'smooth' }
  },
  routes: [
    { path: '/', component: HomeView, meta: { keepAlive: true } },
    { path: '/products', component: ProductListView, meta: { keepAlive: true } },
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
    { path: '/coupons', component: CouponCenterView },
    { path: '/after-sale', component: AfterSaleView },
    { path: '/group-buy', component: GroupBuyView },
    { path: '/flash-sale', component: FlashSaleView },
    { path: '/invoices', component: InvoiceView },
    { path: '/recent', component: RecentView },
    { path: '/ranking', component: RankingView },
    { path: '/help', component: HelpView },
    { path: '/:pathMatch(.*)*', component: NotFoundView },
  ],
})

router.beforeEach((to, from) => {
  // SEO: dynamic title
  const title = pageTitles[to.path] || 'YXCLOUD 商城'
  document.title = title

  // NProgress-style loading bar
  startProgress()

  // Auth guard
  const protectedPaths = ['/orders', '/profile', '/checkout', '/cart']
  const needsAuth = protectedPaths.includes(to.path) || to.path.startsWith('/order/') || to.path.startsWith('/pay/')
  if (needsAuth && !localStorage.getItem('accessToken')) {
    return '/login'
  }
})

router.afterEach(() => {
  finishProgress()
})

// Prefetch routes on link hover
function prefetchRoute(path: string) {
  const route = router.resolve(path)
  if (route?.matched?.[0]?.components?.default) {
    const loader = route.matched[0].components.default
    if (typeof loader === 'function') (loader as () => Promise<unknown>)()
  }
}
if (typeof window !== 'undefined') {
  document.addEventListener('mouseover', (e) => {
    const target = e.target as HTMLElement
    const link = target.closest('[data-prefetch]') as HTMLElement | null
    if (link?.dataset.prefetch) prefetchRoute(link.dataset.prefetch)
  })
}

export default router
