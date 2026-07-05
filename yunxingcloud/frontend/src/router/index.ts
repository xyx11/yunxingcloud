import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { i18n } from '@/locales'

// 布局组件同步加载（首屏必需）
import HomeView from '@/views/HomeView.vue'

// 页面组件懒加载
const LoginView = () => import('@/views/LoginView.vue')
const DashboardView = () => import('@/views/DashboardView.vue')
const ConsentView = () => import('@/views/ConsentView.vue')
const RegisterView = () => import('@/views/RegisterView.vue')
const ForgotPasswordView = () => import('@/views/ForgotPasswordView.vue')
const ReviewManageView = () => import('@/views/ReviewManageView.vue')
const BannerView = () => import('@/views/BannerView.vue')
const ProductImportView = () => import('@/views/ProductImportView.vue')
const SkuManageView = () => import('@/views/SkuManageView.vue')
const ShipmentView = () => import('@/views/ShipmentView.vue')
const BrandView = () => import('@/views/BrandView.vue')
const CategoryManageView = () => import('@/views/CategoryManageView.vue')
const CouponView = () => import('@/views/CouponView.vue')
const AnalyticsView = () => import('@/views/AnalyticsView.vue')
const MediaLibraryView = () => import('@/views/MediaLibraryView.vue')
const SysLogView = () => import('@/views/SysLogView.vue')
const InvoiceAdminView = () => import('@/views/InvoiceAdminView.vue')
const EmailTemplateView = () => import('@/views/EmailTemplateView.vue')
const ExportCenterView = () => import('@/views/ExportCenterView.vue')
const ActivityLogView = () => import('@/views/ActivityLogView.vue')
const BundleView = () => import('@/views/BundleView.vue')
const SeoManageView = () => import('@/views/SeoManageView.vue')
const ChatAdminView = () => import('@/views/ChatAdminView.vue')
const PointsAdminView = () => import('@/views/PointsAdminView.vue')
const RecommendConfigView = () => import('@/views/RecommendConfigView.vue')
const AuditView = () => import('@/views/AuditView.vue')
const SystemConfigView = () => import('@/views/SystemConfigView.vue')
const ResetPasswordView = () => import('@/views/ResetPasswordView.vue')
const DepartmentView = () => import('@/views/DepartmentView.vue')
const RoleView = () => import('@/views/RoleView.vue')
const UserManageView = () => import('@/views/UserManageView.vue')
const MenuView = () => import('@/views/MenuView.vue')
const OperLogView = () => import('@/views/OperLogView.vue')
const ConfigView = () => import('@/views/ConfigView.vue')
const JobView = () => import('@/views/JobView.vue')
const GenView = () => import('@/views/GenView.vue')
const SystemMonitorView = () => import('@/views/SystemMonitorView.vue')
const SwaggerView = () => import('@/views/SwaggerView.vue')
const ProfileView = () => import('@/views/ProfileView.vue')
const MaintenanceView = () => import('@/views/MaintenanceView.vue')
const DictView = () => import('@/views/DictView.vue')
const NoticeView = () => import('@/views/NoticeView.vue')
const PostView = () => import('@/views/PostView.vue')
const LoginLogView = () => import('@/views/LoginLogView.vue')
const OnlineUserView = () => import('@/views/OnlineUserView.vue')
const BackupView = () => import('@/views/BackupView.vue')
const MessageView = () => import('@/views/MessageView.vue')
const IpBlacklistView = () => import('@/views/IpBlacklistView.vue')
const ApprovalView = () => import('@/views/ApprovalView.vue')
const RegisterApprovalView = () => import('@/views/RegisterApprovalView.vue')
const OAuth2ClientView = () => import('@/views/OAuth2ClientView.vue')
const DataScreenView = () => import('@/views/DataScreenView.vue')
const OAuth2CallbackView = () => import('@/views/OAuth2CallbackView.vue')
const TicketView = () => import('@/views/TicketView.vue')
const PaymentView = () => import('@/views/PaymentView.vue')
const ProductView = () => import('@/views/ProductView.vue')
const OrderView = () => import('@/views/OrderView.vue')
const InventoryView = () => import('@/views/InventoryView.vue')
const WarehouseView = () => import('@/views/WarehouseView.vue')
const GroupBuyView = () => import('@/views/GroupBuyView.vue')
const FlashSaleView = () => import('@/views/FlashSaleView.vue')
const AfterSaleView = () => import('@/views/AfterSaleView.vue')
const ArticleView = () => import('@/views/ArticleView.vue')
const NotificationView = () => import('@/views/NotificationView.vue')
const GiftCardView = () => import('@/views/GiftCardView.vue')
const CampaignView = () => import('@/views/CampaignView.vue')
const TagView = () => import('@/views/TagView.vue')
const MemberTierView = () => import('@/views/MemberTierView.vue')
const FeedbackView = () => import('@/views/FeedbackView.vue')
const NotFoundView = () => import('@/views/NotFoundView.vue')

const PUBLIC_PATHS = ['/login', '/register', '/forgot-password', '/reset-password', '/oauth2/callback']

const router = createRouter({
  history: createWebHistory('/admin/'),
  routes: [
    { path: '/login', name: 'Login', component: LoginView, meta: { title: 'nav.login' } },
    { path: '/register', name: 'Register', component: RegisterView, meta: { title: 'nav.register' } },
    { path: '/forgot-password', name: 'ForgotPassword', component: ForgotPasswordView, meta: { title: 'nav.forgotPassword' } },
    { path: '/reset-password', name: 'ResetPassword', component: ResetPasswordView, meta: { title: 'nav.resetPassword' } },
    {
      path: '/',
      component: HomeView,
      children: [
        { path: '', name: 'Home', component: DashboardView, meta: { title: 'nav.dashboard' } },
        { path: 'departments', name: 'Departments', component: DepartmentView, meta: { title: 'nav.departments' } },
        { path: 'roles', name: 'Roles', component: RoleView, meta: { title: 'nav.roles' } },
        { path: 'users', name: 'Users', component: UserManageView, meta: { title: 'nav.users' } },
        { path: 'menus', name: 'Menus', component: MenuView, meta: { title: 'nav.menus' } },
        { path: 'operlog', name: 'OperLog', component: OperLogView, meta: { title: 'nav.operlog' } },
        { path: 'config', name: 'Config', component: ConfigView, meta: { title: 'nav.config' } },
        { path: 'job', name: 'Job', component: JobView, meta: { title: 'nav.jobs' } },
        { path: 'generator', name: 'Generator', component: GenView, meta: { title: 'nav.generator' } },
        { path: 'swagger', name: 'Swagger', component: SwaggerView, meta: { title: 'nav.swagger' } },
        { path: 'monitor', name: 'Monitor', component: SystemMonitorView, meta: { title: 'nav.monitor' } },
        { path: 'maintenance', name: 'Maintenance', component: MaintenanceView, meta: { title: 'nav.maintenance' } },
        { path: 'dict', name: 'Dict', component: DictView, meta: { title: 'nav.dict' } },
        { path: 'notices', name: 'Notices', component: NoticeView, meta: { title: 'nav.notice' } },
        { path: 'posts', name: 'Posts', component: PostView, meta: { title: 'nav.posts' } },
        { path: 'tickets', name: 'Tickets', component: TicketView, meta: { title: 'nav.tickets' } },
        { path: 'payments', name: 'Payments', component: PaymentView, meta: { title: 'nav.payments' } },
        { path: 'orders', name: 'Orders', component: OrderView, meta: { title: 'nav.orders' } },
        { path: 'products', name: 'Products', component: ProductView, meta: { title: 'nav.products' } },
        { path: 'inventory', name: 'Inventory', component: InventoryView, meta: { title: 'nav.inventory' } },
        { path: 'warehouses', name: 'Warehouses', component: WarehouseView, meta: { title: 'nav.warehouses' } },
        { path: 'groupbuy', name: 'GroupBuy', component: GroupBuyView, meta: { title: '拼团管理' } },
        { path: 'flashsale', name: 'FlashSale', component: FlashSaleView, meta: { title: '秒杀管理' } },
        { path: 'aftersale', name: 'AfterSale', component: AfterSaleView, meta: { title: 'nav.aftersale' } },
        { path: 'articles', name: 'Articles', component: ArticleView, meta: { title: '内容管理' } },
        { path: 'notifications', name: 'Notifications', component: NotificationView, meta: { title: '通知管理' } },
        { path: 'giftcards', name: 'GiftCards', component: GiftCardView, meta: { title: '礼品卡管理' } },
        { path: 'campaigns', name: 'Campaigns', component: CampaignView, meta: { title: 'nav.campaigns' } },
        { path: 'tags', name: 'Tags', component: TagView, meta: { title: 'nav.tags' } },
        { path: 'member-tiers', name: 'MemberTiers', component: MemberTierView, meta: { title: 'nav.memberTiers' } },
        { path: 'feedback', name: 'Feedback', component: FeedbackView, meta: { title: '客户反馈' } },
        { path: 'loginlog', name: 'LoginLog', component: LoginLogView, meta: { title: 'nav.loginlog' } },
        { path: 'online', name: 'OnlineUser', component: OnlineUserView, meta: { title: 'nav.online' } },
        { path: 'backup', name: 'Backup', component: BackupView, meta: { title: 'nav.backup' } },
        { path: 'messages', name: 'Messages', component: MessageView, meta: { title: '站内信' } },
        { path: 'ip-blacklist', name: 'IpBlacklist', component: IpBlacklistView, meta: { title: 'nav.ipBlacklist' } },
        { path: 'approval', name: 'Approval', component: ApprovalView, meta: { title: '审批流程' } },
        { path: 'register-approval', name: 'RegisterApproval', component: RegisterApprovalView, meta: { title: '注册审核' } },
        { path: 'oauth2-clients', name: 'OAuth2Clients', component: OAuth2ClientView, meta: { title: 'OAuth2客户端' } },
        { path: 'profile', name: 'Profile', component: ProfileView, meta: { title: 'nav.profile' } },
        { path: 'reviews', name: 'Reviews', component: ReviewManageView, meta: { title: 'nav.reviews' } },
        { path: 'banners', name: 'Banners', component: BannerView, meta: { title: 'Banner管理' } },
        { path: 'products/import', name: 'ProductImport', component: ProductImportView, meta: { title: 'nav.productImport' } },
        { path: 'skus', name: 'Skus', component: SkuManageView, meta: { title: 'nav.skus' } },
        { path: 'shipments', name: 'Shipments', component: ShipmentView, meta: { title: '物流管理' } },
        { path: 'brands', name: 'Brands', component: BrandView, meta: { title: 'nav.brands' } },
        { path: 'categories', name: 'Categories', component: CategoryManageView, meta: { title: 'nav.categories' } },
        { path: 'coupons', name: 'Coupons', component: CouponView, meta: { title: '优惠券管理' } },
        { path: 'analytics', name: 'Analytics', component: AnalyticsView, meta: { title: 'nav.analytics' } },
        { path: 'media-library', name: 'MediaLibrary', component: MediaLibraryView, meta: { title: 'nav.mediaLibrary' } },
        { path: 'system-logs', name: 'SysLogs', component: SysLogView, meta: { title: 'nav.systemLogs' } },
        { path: 'invoices', name: 'Invoices', component: InvoiceAdminView, meta: { title: 'nav.invoices' } },
        { path: 'email-templates', name: 'EmailTemplates', component: EmailTemplateView, meta: { title: 'nav.emailTemplates' } },
        { path: 'export-center', name: 'ExportCenter', component: ExportCenterView, meta: { title: 'nav.exportCenter' } },
        { path: 'activity-logs', name: 'ActivityLogs', component: ActivityLogView, meta: { title: '活动日志' } },
        { path: 'bundles', name: 'Bundles', component: BundleView, meta: { title: 'nav.bundles' } },
        { path: 'seo', name: 'Seo', component: SeoManageView, meta: { title: 'nav.seo' } },
        { path: 'chat-admin', name: 'ChatAdmin', component: ChatAdminView, meta: { title: '客服会话' } },
        { path: 'points-admin', name: 'PointsAdmin', component: PointsAdminView, meta: { title: 'nav.pointsAdmin' } },
        { path: 'recommend-config', name: 'RecommendConfig', component: RecommendConfigView, meta: { title: 'nav.recommendConfig' } },
        { path: 'audit', name: 'Audit', component: AuditView, meta: { title: 'nav.audit' } },
        { path: 'system-config', name: 'SystemConfig', component: SystemConfigView, meta: { title: 'nav.systemConfig' } },
      ],
    },
    { path: '/screen', name: 'DataScreen', component: DataScreenView, meta: { title: 'nav.dataScreen' } },
    { path: '/oauth2/consent', name: 'Consent', component: ConsentView, meta: { title: '授权确认' } },
    { path: '/oauth2/callback', name: 'OAuth2Callback', component: OAuth2CallbackView, meta: { title: '第三方登录' } },
    { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFoundView, meta: { title: '404' } },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

router.afterEach((to) => {
  document.title = (to.meta.title as string) ? `${to.meta.title} - yunxingcloud` : 'yunxingcloud'
})

router.beforeEach(async (to, _from, next) => {
  if (PUBLIC_PATHS.includes(to.path)) {
    next()
    return
  }

  const authStore = useAuthStore()
  if (authStore.isAuthenticated) { next(); return }

  const token = localStorage.getItem('accessToken')
  if (!token) {
    next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    return
  }
  try {
    await authStore.fetchUser()
    next()
  } catch {
    next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
  }
})

export default router
