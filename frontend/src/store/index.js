import { createStore } from 'vuex'
import { message } from 'ant-design-vue'
import { Login, GetUserInfo } from '@/api/frontend'

export default createStore({
  state: {},
  getters: {
    user: (state) => {
      return JSON.parse(sessionStorage.getItem('user')).user
    },
    permission: (state) => {
      return JSON.parse(sessionStorage.getItem('user')).userBuAppRoles
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
            GetUserInfo()
              .then((res) => {
                if (res.code !== 200) {
                  message.error(res.message)
                  return
                }
                // console.log('GetUserInfo', res)
                sessionStorage.setItem('user', JSON.stringify(res.data))
                resolve(res.data)
              })
              .catch((err) => {
                sessionStorage.removeItem('token')
                sessionStorage.removeItem('user')
                reject(new Error(err))
              })
          })
          .catch((err) => {
            sessionStorage.removeItem('token')
            sessionStorage.removeItem('user')
            reject(new Error(err))
          })
      })
    }
  },
  modules: {}
})
