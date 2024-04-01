import axios from 'axios'
import { API } from '@/api/backend'
import { message } from 'ant-design-vue'
import router from '@/router/index'

export const baseURL = 'http://localhost:9090'

const instance = axios.create({})
instance.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem('token')
    if (token) Object.assign(config.headers, { token })
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)
instance.interceptors.response.use(
  (response) => {
    if (
      response.config.url === API.EXPORT_BRIEF ||
      response.config.url === API.EXPORT_DETAIL ||
      response.config.url === API.EXPORT_SBOM
    ) {
      return response
    }
    if (response.data.message === '登录认证失败') {
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
      router.push('/login')
    }
    return response.data
  },
  (error) => {
    return Promise.reject(error)
  }
)

export const request = (method, url, config) => {
  return instance({
    headers: {
      'Content-Type': 'application/json'
    },
    method,
    url,
    ...config,
    baseURL,
    timeout: 600000
  })
}
