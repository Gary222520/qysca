const Home = () => import('@/views/Home.vue')
const Summary = () => import('@/views/summary/Summary.vue')
const Scan = () => import('@/views/scan/Scan.vue')
const Application = () => import('@/views/application/Application.vue')
const Project = () => import('@/views/project/Project.vue')
const AppDetail = () => import('@/views/project/AppDetail.vue')
const Compare = () => import('@/views/project/Compare.vue')
const Component = () => import('@/views/component/Component.vue')
const Dependency = () => import('@/views/component/Dependency.vue')

const routes = [
  { path: '/', name: '/', component: Home },
  {
    path: '/home',
    name: 'home',
    meta: { title: '首页' },
    component: Home,
    children: [
      {
        path: 'summary',
        name: 'summary',
        meta: { menu: 'summary', title: '统计概览' },
        component: Summary
      },
      {
        path: 'scan',
        name: 'scan',
        meta: { menu: 'scan', title: '扫描分析' },
        component: Scan
      },
      {
        path: 'application',
        name: 'application',
        meta: { menu: 'application', title: '应用管理' },
        component: Application
      },
      {
        path: 'project',
        name: 'project',
        meta: { menu: 'project', title: '项目管理' },
        component: Project
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
      }
    ]
  }
]

export default routes
