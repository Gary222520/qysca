const Home = () => import('@/views/Home.vue')
const Login = () => import('@/views/user/Login.vue')

const Application = () => import('@/views/application/Application.vue')
const AppDetail = () => import('@/views/project/AppDetail.vue')
const Compare = () => import('@/views/project/Compare.vue')
const Component = () => import('@/views/component/Component.vue')
const Dependency = () => import('@/views/component/Dependency.vue')

const BuManage = () => import('@/views/bu/BuManage.vue')

const UserManage = () => import('@/views/user/UserManage.vue')

const routes = [
  { path: '/', name: '/', component: Home },
  { path: '/login', name: 'login', component: Login },
  {
    path: '/home',
    name: 'home',
    meta: { title: '首页' },
    component: Home,
    children: [
      {
        path: 'application',
        name: 'application',
        meta: { menu: 'application', title: '应用管理' },
        component: Application
      },
      {
        path: 'appDetail',
        name: 'appDetail',
        meta: { menu: 'application', title: '项目详情' },
        component: AppDetail
      },
      {
        path: 'compare',
        name: 'compare',
        meta: { menu: 'compare', title: '快照对比' },
        component: Compare
      },
      {
        path: 'component',
        name: 'component',
        meta: { menu: 'component', title: '组件管理' },
        component: Component
      },
      {
        path: 'dependency',
        name: 'dependency',
        meta: { menu: 'component', title: '组件依赖' },
        component: Dependency
      },
      {
        path: 'buManage',
        name: 'buManage',
        meta: { menu: 'buManage', title: '部门管理' },
        component: BuManage
      },
      {
        path: 'userManage',
        name: 'userManage',
        meta: { menu: 'userManage', title: '用户管理' },
        component: UserManage
      }
    ]
  }
]

export default routes
