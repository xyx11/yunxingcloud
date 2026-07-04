import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

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
    { path: '/login', name: 'Login', component: LoginView, meta: { title: '登录' } },
    { path: '/register', name: 'Register', component: RegisterView, meta: { title: '注册' } },
    { path: '/forgot-password', name: 'ForgotPassword', component: ForgotPasswordView, meta: { title: '忘记密码' } },
    { path: '/reset-password', name: 'ResetPassword', component: ResetPasswordView, meta: { title: '重置密码' } },
    {
      path: '/',
      component: HomeView,
      children: [
        { path: '', name: 'Home', component: DashboardView, meta: { title: '仪表盘' } },
        { path: 'departments', name: 'Departments', component: DepartmentView, meta: { title: '部门管理' } },
        { path: 'roles', name: 'Roles', component: RoleView, meta: { title: '角色管理' } },
        { path: 'users', name: 'Users', component: UserManageView, meta: { title: '用户管理' } },
        { path: 'menus', name: 'Menus', component: MenuView, meta: { title: '菜单管理' } },
        { path: 'operlog', name: 'OperLog', component: OperLogView, meta: { title: '操作日志' } },
        { path: 'config', name: 'Config', component: ConfigView, meta: { title: '参数配置' } },
        { path: 'job', name: 'Job', component: JobView, meta: { title: '定时任务' } },
        { path: 'generator', name: 'Generator', component: GenView, meta: { title: '代码生成' } },
        { path: 'swagger', name: 'Swagger', component: SwaggerView, meta: { title: 'API文档' } },
        { path: 'monitor', name: 'Monitor', component: SystemMonitorView, meta: { title: '系统监控' } },
        { path: 'maintenance', name: 'Maintenance', component: MaintenanceView, meta: { title: '数据维护' } },
        { path: 'dict', name: 'Dict', component: DictView, meta: { title: '字典管理' } },
        { path: 'notices', name: 'Notices', component: NoticeView, meta: { title: '通知公告' } },
        { path: 'posts', name: 'Posts', component: PostView, meta: { title: '岗位管理' } },
        { path: 'tickets', name: 'Tickets', component: TicketView, meta: { title: '工单管理' } },
        { path: 'payments', name: 'Payments', component: PaymentView, meta: { title: '支付管理' } },
        { path: 'orders', name: 'Orders', component: OrderView, meta: { title: '订单管理' } },
        { path: 'products', name: 'Products', component: ProductView, meta: { title: '商品管理' } },
        { path: 'inventory', name: 'Inventory', component: InventoryView, meta: { title: '库存管理' } },
        { path: 'warehouses', name: 'Warehouses', component: WarehouseView, meta: { title: '仓库管理' } },
        { path: 'groupbuy', name: 'GroupBuy', component: GroupBuyView, meta: { title: '拼团管理' } },
        { path: 'flashsale', name: 'FlashSale', component: FlashSaleView, meta: { title: '秒杀管理' } },
        { path: 'aftersale', name: 'AfterSale', component: AfterSaleView, meta: { title: '售后管理' } },
        { path: 'articles', name: 'Articles', component: ArticleView, meta: { title: '内容管理' } },
        { path: 'notifications', name: 'Notifications', component: NotificationView, meta: { title: '通知管理' } },
        { path: 'giftcards', name: 'GiftCards', component: GiftCardView, meta: { title: '礼品卡管理' } },
        { path: 'campaigns', name: 'Campaigns', component: CampaignView, meta: { title: '营销活动' } },
        { path: 'tags', name: 'Tags', component: TagView, meta: { title: '标签管理' } },
        { path: 'member-tiers', name: 'MemberTiers', component: MemberTierView, meta: { title: '会员等级' } },
        { path: 'feedback', name: 'Feedback', component: FeedbackView, meta: { title: '客户反馈' } },
        { path: 'loginlog', name: 'LoginLog', component: LoginLogView, meta: { title: '登录日志' } },
        { path: 'online', name: 'OnlineUser', component: OnlineUserView, meta: { title: '在线用户' } },
        { path: 'backup', name: 'Backup', component: BackupView, meta: { title: '数据备份' } },
        { path: 'messages', name: 'Messages', component: MessageView, meta: { title: '站内信' } },
        { path: 'ip-blacklist', name: 'IpBlacklist', component: IpBlacklistView, meta: { title: 'IP黑名单' } },
        { path: 'approval', name: 'Approval', component: ApprovalView, meta: { title: '审批流程' } },
        { path: 'register-approval', name: 'RegisterApproval', component: RegisterApprovalView, meta: { title: '注册审核' } },
        { path: 'oauth2-clients', name: 'OAuth2Clients', component: OAuth2ClientView, meta: { title: 'OAuth2客户端' } },
        { path: 'profile', name: 'Profile', component: ProfileView, meta: { title: '个人中心' } },
        { path: 'reviews', name: 'Reviews', component: ReviewManageView, meta: { title: '评价管理' } },
        { path: 'banners', name: 'Banners', component: BannerView, meta: { title: 'Banner管理' } },
        { path: 'products/import', name: 'ProductImport', component: ProductImportView, meta: { title: '批量导入' } },
        { path: 'skus', name: 'Skus', component: SkuManageView, meta: { title: 'SKU管理' } },
        { path: 'shipments', name: 'Shipments', component: ShipmentView, meta: { title: '物流管理' } },
        { path: 'brands', name: 'Brands', component: BrandView, meta: { title: '品牌管理' } },
        { path: 'categories', name: 'Categories', component: CategoryManageView, meta: { title: '分类管理' } },
        { path: 'coupons', name: 'Coupons', component: CouponView, meta: { title: '优惠券管理' } },
        { path: 'analytics', name: 'Analytics', component: AnalyticsView, meta: { title: '销售分析' } },
        { path: 'media-library', name: 'MediaLibrary', component: MediaLibraryView, meta: { title: '媒体库' } },
        { path: 'system-logs', name: 'SysLogs', component: SysLogView, meta: { title: '系统日志' } },
        { path: 'invoices', name: 'Invoices', component: InvoiceAdminView, meta: { title: '发票管理' } },
        { path: 'email-templates', name: 'EmailTemplates', component: EmailTemplateView, meta: { title: '邮件模板' } },
        { path: 'export-center', name: 'ExportCenter', component: ExportCenterView, meta: { title: '导出中心' } },
        { path: 'activity-logs', name: 'ActivityLogs', component: ActivityLogView, meta: { title: '活动日志' } },
        { path: 'bundles', name: 'Bundles', component: BundleView, meta: { title: '捆绑套餐' } },
        { path: 'seo', name: 'Seo', component: SeoManageView, meta: { title: 'SEO管理' } },
        { path: 'chat-admin', name: 'ChatAdmin', component: ChatAdminView, meta: { title: '客服会话' } },
        { path: 'points-admin', name: 'PointsAdmin', component: PointsAdminView, meta: { title: '积分管理' } },
        { path: 'recommend-config', name: 'RecommendConfig', component: RecommendConfigView, meta: { title: '推荐配置' } },
        { path: 'audit', name: 'Audit', component: AuditView, meta: { title: '审计日志' } },
        { path: 'system-config', name: 'SystemConfig', component: SystemConfigView, meta: { title: '系统配置' } },
      ],
    },
    { path: '/screen', name: 'DataScreen', component: DataScreenView, meta: { title: '数据大屏' } },
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
