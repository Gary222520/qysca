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
  } else if (to.path === '/' || to.path === '/home') next('/home/application')
  else next()
})

export default router
