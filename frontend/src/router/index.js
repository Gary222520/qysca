import { createRouter, createWebHashHistory } from 'vue-router'
import routes from './routeTable'

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (!sessionStorage.getItem('token')) {
    if (to.path === '/login') next()
    else next('/login')
  } else if (to.path === '/' || to.path === '/home' || to.path === '/login') {
    const permission = JSON.parse(sessionStorage.getItem('user'))?.userBuAppRoles
    if (!permission) {
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
      next('/login')
      return
    }
    if (permission.some((item) => item.role === 'Admin')) next('/home/userManage')
    else next('/home/application')
  } else next()
})

export default router
