import { request } from './request'
import { API } from './backend'

// 查询所有项目
export const GetProjectList = () => request('get', API.PROJECT_LIST, {})
// 查询具体项目的信息
export const GetProjectInfo = (params) => request('get', API.PROJECT_INFO, { params })
// 查询具体项目具体版本的组件信息-树形展示
export const GetProjectTree = (params) => request('get', API.PROJECT_TREE, { params })
// 查询具体项目具体版本的组件信息-平铺展示
export const GetProjectTiled = (params) => request('get', API.PROJECT_TILED, { params })
// 新建项目
export const AddProject = (data) => request('post', API.ADD_PROJECT, { data })

// 更新具体项目具体版本的pom信息
export const UpdateVersion = (data) => request('post', API.UPDATE_VERSION, { data })
// 删除具体项目的具体版本信息
export const DeleteVersion = (params) => request('post', API.DELETE_VERSION, { params })
// 版本升级
export const UpgradeVersion = (data) => request('post', API.UPGRADE_VERSION, { data })

// test
export const ShowTree = (data) => request('post', API.SHOW_TREE, { data })
