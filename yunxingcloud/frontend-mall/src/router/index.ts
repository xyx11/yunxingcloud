import { createRouter, createWebHistory } from 'vue-router'

// 高频路由 — 直接打包进主JS (避免首次访问空白)
import HomeView from '@/views/HomeView.vue'
import CartView from '@/views/CartView.vue'
import ProductListView from '@/views/ProductListView.vue'

const CheckoutView = () => import('@/views/CheckoutView.vue')
const ProductView = () => import('@/views/ProductView.vue')
const SearchView = () => import('@/views/SearchView.vue')
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
const NotificationsView = () => import('@/views/NotificationsView.vue')
const OAuthCallbackView = () => import('@/views/OAuthCallbackView.vue')
const PresaleView = () => import('@/views/PresaleView.vue')
const LiveView = () => import('@/views/LiveView.vue')
const BrandsView = () => import('@/views/BrandsView.vue')
const BrandDetailView = () => import('@/views/BrandDetailView.vue')
const MerchantApplyView = () => import('@/views/MerchantApplyView.vue')
const BundleListView = () => import('@/views/BundleListView.vue')
const CompareView = () => import('@/views/CompareView.vue')
const HelpView = () => import('@/views/HelpView.vue')
const NotFoundView = () => import('@/views/NotFoundView.vue')
const LiveDetailView = () => import('@/views/LiveDetailView.vue')
const PresaleDetailView = () => import('@/views/PresaleDetailView.vue')
const BundleDetailView = () => import('@/views/BundleDetailView.vue')
const PriceAlertView = () => import('@/views/PriceAlertView.vue')
const MyReviewsView = () => import('@/views/MyReviewsView.vue')

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
  '/notifications': '消息通知 - YXCLOUD',
  '/presale': '预售专区 - YXCLOUD',
  '/live': '直播精选 - YXCLOUD',
  '/brands': '品牌专区 - YXCLOUD',
  '/bundles': '超值套餐 - YXCLOUD',
  '/merchant/apply': '商家入驻 - YXCLOUD',
  '/price-alerts': '降价提醒 - YXCLOUD',
  '/compare': '商品对比 - YXCLOUD',
  '/my-reviews': '我的评价 - YXCLOUD',
}

const pageDescriptions: Record<string, string> = {
  '/': 'YXCLOUD 商城是您的一站式购物平台，提供海量商品、限时秒杀、拼团优惠。支持微信/支付宝支付，全国包邮。',
  '/products': '浏览 YXCLOUD 全部商品，按分类筛选，发现心仪好物。',
  '/search': '在 YXCLOUD 搜索商品，查找你想要的。',
  '/cart': '查看 YXCLOUD 购物车中的商品。',
  '/checkout': '结算订单 - YXCLOUD',
  '/orders': '查看我的订单列表 - YXCLOUD',
  '/login': '登录 YXCLOUD 商城账号。',
  '/register': '注册 YXCLOUD 商城新账号。',
  '/profile': '管理个人信息、收货地址和账户设置 - YXCLOUD',
  '/points': '积分中心 - YXCLOUD',
  '/gift-card': '礼品卡 - YXCLOUD',
  '/wishlist': '收藏夹 - YXCLOUD',
  '/coupons': '领取和查看优惠券 - YXCLOUD',
  '/group-buy': '拼团专区，和朋友一起拼更优惠 - YXCLOUD',
  '/flash-sale': '限时秒杀，超低价抢购 - YXCLOUD',
  '/ranking': '热门商品排行榜 - YXCLOUD',
  '/notifications': '消息通知中心 - YXCLOUD',
  '/presale': '预售专区，抢先预订享优惠 - YXCLOUD',
  '/live': '直播精选，主播带你挑好物 - YXCLOUD',
  '/brands': '品牌专区，发现品质好货 - YXCLOUD',
  '/bundles': '超值套餐，搭配购买更划算 - YXCLOUD',
  '/merchant/apply': '商家入驻 - YXCLOUD',
  '/help': '帮助中心 - YXCLOUD',
  '/price-alerts': '降价提醒管理 - YXCLOUD',
  '/my-reviews': '管理我的商品评价 - YXCLOUD',
}

function resolveTitle(path: string): string {
  if (pageTitles[path]) return pageTitles[path]
  if (path.startsWith('/product/')) return '商品详情 - YXCLOUD'
  if (path.startsWith('/order/')) return '订单详情 - YXCLOUD'
  if (path.startsWith('/brand/')) return '品牌商品 - YXCLOUD'
  if (path.startsWith('/pay/')) return '支付订单 - YXCLOUD'
  if (path.startsWith('/bundle/')) return '套餐详情 - YXCLOUD'
  if (path.startsWith('/presale/')) return '预售详情 - YXCLOUD'
  if (path.startsWith('/live/')) return '直播间 - YXCLOUD'
  return 'YXCLOUD 商城'
}

function resolveDescription(path: string): string {
  if (pageDescriptions[path]) return pageDescriptions[path]
  if (path.startsWith('/product/')) return '查看商品详情、评价、规格参数 - YXCLOUD 商城'
  if (path.startsWith('/order/')) return '查看订单详情和物流信息 - YXCLOUD 商城'
  if (path.startsWith('/brand/')) return '浏览品牌商品列表 - YXCLOUD 商城'
  if (path.startsWith('/search')) return '搜索 YXCLOUD 商城海量商品'
  if (path.startsWith('/live/')) return '观看直播带货 - YXCLOUD 商城'
  if (path.startsWith('/presale/')) return '预售商品详情，定金预订更优惠 - YXCLOUD 商城'
  if (path.startsWith('/bundle/')) return '超值套餐搭配，省更多 - YXCLOUD 商城'
  return '品质生活，一站购齐 - YXCLOUD 商城'
}

function updateMetaDescription(path: string) {
  const desc = resolveDescription(path)
  let el = document.querySelector('meta[name="description"]')
  if (!el) {
    el = document.createElement('meta')
    el.setAttribute('name', 'description')
    document.head.appendChild(el)
  }
  el.setAttribute('content', desc)
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
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0, behavior: 'smooth' }
  },
  routes: [
    { path: '/', component: HomeView, meta: { keepAlive: true, transition: 'fade' } },
    { path: '/products', component: ProductListView, meta: { keepAlive: true, transition: 'fade' } },
    { path: '/product/:id', component: ProductView, meta: { transition: 'slide' } },
    { path: '/search', component: SearchView },
    { path: '/cart', component: CartView },
    { path: '/checkout', component: CheckoutView },
    { path: '/orders', component: OrdersView, meta: { keepAlive: true } },
    { path: '/order/:id', component: OrderDetailView, meta: { transition: 'slide' } },
    { path: '/login', component: LoginView },
    { path: '/register', component: RegisterView },
    { path: '/profile', component: ProfileView, meta: { keepAlive: true } },
    { path: '/pay/:id', component: PayView },
    { path: '/order/:id/result', component: PaymentResultView },
    { path: '/points', component: PointsView },
    { path: '/gift-card', component: MallGiftCardView },
    { path: '/logistics', component: LogisticsView },
    { path: '/wishlist', component: WishlistView, meta: { keepAlive: true } },
    { path: '/coupons', component: CouponCenterView, meta: { keepAlive: true } },
    { path: '/after-sale', component: AfterSaleView },
    { path: '/group-buy', component: GroupBuyView },
    { path: '/flash-sale', component: FlashSaleView },
    { path: '/invoices', component: InvoiceView },
    { path: '/recent', component: RecentView, meta: { keepAlive: true } },
    { path: '/ranking', component: RankingView },
    { path: '/notifications', component: NotificationsView },
    { path: '/oauth2/callback', component: OAuthCallbackView },
    { path: '/live', component: LiveView },
    { path: '/live/:id', component: LiveDetailView, meta: { transition: 'slide' } },
    { path: '/brands', component: BrandsView, meta: { keepAlive: true } },
    { path: '/brand/:id', component: BrandDetailView, meta: { transition: 'slide' } },
    { path: '/merchant/apply', component: MerchantApplyView },
    { path: '/bundles', component: BundleListView },
    { path: '/bundle/:id', component: BundleDetailView, meta: { transition: 'slide' } },
    { path: '/presale', component: PresaleView },
    { path: '/presale/:id', component: PresaleDetailView, meta: { transition: 'slide' } },
    { path: '/price-alerts', component: PriceAlertView },
    { path: '/help', component: HelpView },
    { path: '/compare', component: CompareView },
    { path: '/my-reviews', component: MyReviewsView, meta: { transition: 'fade' } },
    { path: '/:pathMatch(.*)*', component: NotFoundView },
  ],
})

router.beforeEach((to, _from) => {
  // SEO: dynamic title
  document.title = resolveTitle(to.path)

  // NProgress-style loading bar
  startProgress()

  // Auth guard
  const protectedPaths = ['/orders', '/profile', '/checkout', '/cart', '/wishlist', '/coupons', '/after-sale', '/invoices', '/points', '/gift-card', '/merchant/apply', '/price-alerts', '/notifications', '/logistics', '/my-reviews']
  const needsAuth = protectedPaths.some(p => to.path === p || to.path.startsWith(p + '/')) || to.path.startsWith('/order/') || to.path.startsWith('/pay/')
  if (needsAuth && !localStorage.getItem('accessToken')) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

function updateOgTags(title: string, description: string) {
  const tags: Record<string, string> = {
    'og:title': title,
    'og:description': description,
    'twitter:title': title,
    'twitter:description': description,
  }
  for (const [prop, content] of Object.entries(tags)) {
    let el = document.querySelector(`meta[property="${prop}"]`) || document.querySelector(`meta[name="${prop}"]`)
    if (!el) {
      el = document.createElement('meta')
      el.setAttribute(prop.startsWith('twitter:') ? 'name' : 'property', prop)
      document.head.appendChild(el)
    }
    el.setAttribute('content', content)
  }
}

router.afterEach((to) => {
  const title = resolveTitle(to.path)
  const desc = resolveDescription(to.path)
  updateMetaDescription(to.path)
  updateOgTags(title, desc)
  finishProgress()
  // Focus management for accessibility
  setTimeout(() => {
    const main = document.getElementById('main-content')
    if (main) main.focus({ preventScroll: true })
    const announcer = document.getElementById('sr-announcer')
    if (announcer) announcer.textContent = resolveTitle(to.path)
  }, 100)
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
  let prefetchTimer: ReturnType<typeof setTimeout> | null = null
  document.addEventListener('mouseover', (e) => {
    const target = e.target as HTMLElement
    const link = target.closest('[data-prefetch]') as HTMLElement | null
    if (link?.dataset.prefetch) {
      if (prefetchTimer) clearTimeout(prefetchTimer)
      prefetchTimer = setTimeout(() => prefetchRoute(link.dataset.prefetch!), 150)
    }
  })
}

export default router
