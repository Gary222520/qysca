import { createStore } from 'vuex'
import { message } from 'ant-design-vue'
import { GetApplicationList, GetApplicationInfo, GetApplicationVersions } from '@/api/frontend'

export default createStore({
  state: {
    applications: [],
    selection: {},
    currentApp: {}
  },
  getters: {
    appList: (state) => {
      return state.applications
    },
    selection: (state) => {
      return state.selection
    },
    currentApp: (state) => {
      return state.currentApp
    }
  },
  mutations: {
    GET_APP_LIST: (state, applications) => {
      state.applications = applications
    },
    GET_APP_VERSIONS: (state, selection) => {
      state.selection = selection
    },
    SET_CURRENT_APP: (state, currentApp) => {
      state.currentApp = currentApp
    }
  },
  actions: {
    getAppList: async (context, params) => {
      return new Promise((resolve, reject) => {
        GetApplicationList(params)
          .then((res) => {
            if (res.code !== 200) {
              message.error(res.message)
              return
            }
            // console.log('GetApplicationList', res)
            context.commit('GET_APP_LIST', res.data)
            resolve(res.data)
          })
          .catch((err) => {
            reject(new Error(err))
          })
      })
    },
    getAppVersions: async (context, params) => {
      return new Promise((resolve, reject) => {
        GetApplicationVersions(params)
          .then((res) => {
            if (res.code !== 200) {
              message.error(res.message)
              return
            }
            // console.log('GetApplicationVersions', res)
            const selection = {}
            selection.options = []
            res.data.forEach((item) => {
              const option = { label: item, value: item, key: item }
              selection.options.push(option)
            })
            selection.current = selection.options[0]?.value
            context.commit('GET_APP_VERSIONS', selection)
            resolve(selection)
          })
          .catch((err) => {
            reject(new Error(err))
          })
      })
    },
    getAppInfo: async (context, params) => {
      return new Promise((resolve, reject) => {
        GetApplicationInfo(params)
          .then((res) => {
            if (res.code !== 200) {
              message.error(res.message)
              return
            }
            // console.log('GetApplicationInfo', res)
            resolve(res)
          })
          .catch((err) => {
            reject(new Error(err))
          })
      })
    }
  },
  modules: {}
})
