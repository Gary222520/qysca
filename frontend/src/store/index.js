import { createStore } from 'vuex'
import { message } from 'ant-design-vue'
import { Login, Auth } from '@/api/frontend'

export default createStore({
  state: {},
  getters: {
    role: (state) => {
      return JSON.parse(sessionStorage.getItem('user')).role
    }
  },
  mutations: {},
  actions: {
    login: async (context, data) => {
      return new Promise((resolve, reject) => {
        Login(data)
          .then((res) => {
            if (res.code !== 200) {
              message.error(res.message)
              return
            }
            // console.log('Login', res)
            const token = res.data
            sessionStorage.setItem('token', token)
            Auth({ token }).then((res) => {
              if (res.code !== 200) {
                message.error(res.message)
                return
              }
              // console.log('Auth', res)
              sessionStorage.setItem('user', JSON.stringify(res.data))
              resolve(res.data)
            })
          })
          .catch((err) => {
            reject(new Error(err))
          })
      })
    }
  },
  modules: {}
})
