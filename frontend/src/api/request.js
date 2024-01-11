import axios from 'axios'

const request = (method, url, config) => {
  return axios.create({
    method,
    url,
    ...config,
    baseURL: process.env.VUE_APP_API_BASE_URL,
    timeout: 60000
  })
}

export default request
