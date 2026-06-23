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
const NotFoundView = () => import('@/views/NotFoundView.vue')

const PUBLIC_PATHS = ['/login', '/register', '/forgot-password', '/reset-password']

const router = createRouter({
  history: createWebHistory(),
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
        { path: 'loginlog', name: 'LoginLog', component: LoginLogView, meta: { title: '登录日志' } },
        { path: 'online', name: 'OnlineUser', component: OnlineUserView, meta: { title: '在线用户' } },
        { path: 'backup', name: 'Backup', component: BackupView, meta: { title: '数据备份' } },
        { path: 'messages', name: 'Messages', component: MessageView, meta: { title: '站内信' } },
        { path: 'ip-blacklist', name: 'IpBlacklist', component: IpBlacklistView, meta: { title: 'IP黑名单' } },
        { path: 'approval', name: 'Approval', component: ApprovalView, meta: { title: '审批流程' } },
        { path: 'register-approval', name: 'RegisterApproval', component: RegisterApprovalView, meta: { title: '注册审核' } },
        { path: 'oauth2-clients', name: 'OAuth2Clients', component: OAuth2ClientView, meta: { title: 'OAuth2客户端' } },
        { path: 'profile', name: 'Profile', component: ProfileView, meta: { title: '个人中心' } },
      ],
    },
    { path: '/screen', name: 'DataScreen', component: DataScreenView, meta: { title: '数据大屏' } },
    { path: '/oauth2/consent', name: 'Consent', component: ConsentView, meta: { title: '授权确认' } },
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
