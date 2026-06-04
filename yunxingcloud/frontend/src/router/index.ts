import { createRouter, createWebHashHistory } from 'vue-router'
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
const SwaggerView = () => import('@/views/SwaggerView.vue')
const NotFoundView = () => import('@/views/NotFoundView.vue')

const PUBLIC_PATHS = ['/login', '/register', '/forgot-password', '/reset-password']

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/login', name: 'Login', component: LoginView },
    { path: '/register', name: 'Register', component: RegisterView },
    { path: '/forgot-password', name: 'ForgotPassword', component: ForgotPasswordView },
    { path: '/reset-password', name: 'ResetPassword', component: ResetPasswordView },
    {
      path: '/',
      component: HomeView,
      children: [
        { path: '', name: 'Home', component: DashboardView },
        { path: 'departments', name: 'Departments', component: DepartmentView },
        { path: 'roles', name: 'Roles', component: RoleView },
        { path: 'users', name: 'Users', component: UserManageView },
        { path: 'menus', name: 'Menus', component: MenuView },
        { path: 'operlog', name: 'OperLog', component: OperLogView },
        { path: 'config', name: 'Config', component: ConfigView },
        { path: 'job', name: 'Job', component: JobView },
        { path: 'generator', name: 'Generator', component: GenView },
        { path: 'swagger', name: 'Swagger', component: SwaggerView },
      ],
    },
    { path: '/oauth2/consent', name: 'Consent', component: ConsentView },
    { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFoundView },
  ],
})

router.beforeEach(async (to, _from, next) => {
  if (PUBLIC_PATHS.includes(to.path)) {
    next()
    return
  }

  const authStore = useAuthStore()
  if (!authStore.isAuthenticated) {
    try {
      await authStore.fetchUser()
      next()
    } catch {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      return
    }
  }
  next()
})

export default router
