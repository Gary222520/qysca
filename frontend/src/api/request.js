import axios from 'axios'

export const baseURL = 'http://localhost:9090'

const instance = axios.create({})
instance.interceptors.response.use(
  (response) => {
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
    timeout: 60000
  })
}
