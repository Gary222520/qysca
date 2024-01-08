const Home = () => import('@/views/Home.vue')
const Summary = () => import('@/views/summary/Summary.vue')
const Scan = () => import('@/views/scan/Scan.vue')
const Application = () => import('@/views/application/Application.vue')

const routes = [
  { path: '/', name: 'home', component: Home },
  {
    path: '/home',
    name: 'home',
    breadcrumb: '首页',
    component: Home,
    children: [
      { path: 'summary', name: 'summary', breadcrumb: '统计概览', component: Summary },
      { path: 'scan', name: 'scan', breadcrumb: '扫描分析', component: Scan },
      { path: 'application', name: 'application', breadcrumb: '项目管理', component: Application }
    ]
  }
]

export default routes
