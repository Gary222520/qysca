import axios from 'axios'

export const baseURL = 'http://localhost:9090'
export const request = (method, url, config) => {
  return axios({
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
