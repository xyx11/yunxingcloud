export default {
  app: { name: 'yunxingcloud', subtitle: '分布式微服务平台' },
  nav: {
    home: '首页', dashboard: '仪表盘', users: '用户管理', roles: '角色管理',
    departments: '部门管理', menus: '菜单管理', operlog: '操作日志',
    jobs: '定时任务', config: '参数配置', generator: '代码生成',
    swagger: 'API文档', profile: '个人中心', logout: '退出登录',
  },
  common: {
    save: '保存', cancel: '取消', delete: '删除', edit: '编辑', add: '新增',
    search: '搜索', export: '导出', import: '导入', refresh: '刷新',
    confirm: '确认', success: '操作成功', error: '操作失败',
    confirmDelete: '确认删除?', noData: '暂无数据', loading: '加载中...',
  },
  login: { title: '登录', username: '用户名', password: '密码', captcha: '验证码', remember: '记住密码', forgot: '忘记密码?', register: '立即注册', noAccount: '还没有账号？' },
  user: { username: '用户名', nickname: '昵称', email: '邮箱', source: '来源', dept: '部门', role: '角色', enabled: '状态', actions: '操作', assignDept: '分配部门', assignRole: '分配角色' },
  role: { name: '名称', code: '编码', desc: '描述', perms: '权限' },
  menu: { name: '名称', type: '类型', path: '路由', component: '组件', icon: '图标', sort: '排序', visible: '可见', parent: '上级菜单' },
  operlog: { title: '标题', bizType: '业务类型', method: '请求方法', operator: '操作人', ip: 'IP', url: '请求URL', status: '状态', cost: '耗时', time: '操作时间', detail: '详情', clean: '清空日志', batchDelete: '批量删除' },
  job: { name: '任务名称', group: '任务组', target: '调用目标', cron: 'Cron表达式', status: '状态', concurrent: '并发', remark: '备注', run: '执行' },
  profile: { token: 'Token管理', copy: '复制', copied: '已复制', avatar: '更换头像' },
  footer: '分布式微服务架构平台',
}
