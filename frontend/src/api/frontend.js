import { request } from './request'
import { API } from './backend'

export const postRequest = (data) => request('post', API.TEST, { data })
export const getRequest = (params) => request('get', API.TEST, { params })
