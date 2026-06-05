export default {
  app: { name: 'yunxingcloud', subtitle: 'Distributed Microservice Platform' },
  nav: {
    home: 'Home', dashboard: 'Dashboard', users: 'Users', roles: 'Roles',
    departments: 'Departments', menus: 'Menus', operlog: 'Operation Log',
    jobs: 'Jobs', config: 'Config', generator: 'Generator',
    swagger: 'API Docs', profile: 'Profile', logout: 'Logout',
  },
  common: {
    save: 'Save', cancel: 'Cancel', delete: 'Delete', edit: 'Edit', add: 'Add',
    search: 'Search', export: 'Export', import: 'Import', refresh: 'Refresh',
    confirm: 'Confirm', success: 'Success', error: 'Error',
    confirmDelete: 'Confirm delete?', noData: 'No data', loading: 'Loading...',
  },
  login: { title: 'Login', username: 'Username', password: 'Password', captcha: 'Captcha', remember: 'Remember', forgot: 'Forgot?', register: 'Register', noAccount: "Don't have an account?" },
  user: { username: 'Username', nickname: 'Nickname', email: 'Email', source: 'Source', dept: 'Dept', role: 'Role', enabled: 'Status', actions: 'Actions', assignDept: 'Assign Dept', assignRole: 'Assign Role' },
  role: { name: 'Name', code: 'Code', desc: 'Description', perms: 'Permissions' },
  menu: { name: 'Name', type: 'Type', path: 'Path', component: 'Component', icon: 'Icon', sort: 'Sort', visible: 'Visible', parent: 'Parent' },
  operlog: { title: 'Title', bizType: 'Biz Type', method: 'Method', operator: 'Operator', ip: 'IP', url: 'URL', status: 'Status', cost: 'Cost', time: 'Time', detail: 'Detail', clean: 'Clean', batchDelete: 'Batch Delete' },
  job: { name: 'Job Name', group: 'Group', target: 'Target', cron: 'Cron', status: 'Status', concurrent: 'Concurrent', remark: 'Remark', run: 'Run' },
  profile: { token: 'Token', copy: 'Copy', copied: 'Copied!', avatar: 'Change Avatar' },
  footer: 'Distributed Microservice Platform',
}
