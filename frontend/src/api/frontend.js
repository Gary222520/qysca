import { request } from './request'
import { API } from './backend'

// 创建新应用
export const CreateApplication = (data) => request('post', API.CREATE_APPLICATION, { data })
// 查询应用信息
export const GetApplicationList = (params) => request('get', API.APPLICATION_LIST, { params })
// 查询应用某个版本的信息
export const GetApplicationInfo = (params) => request('get', API.APPLICATION_INFO, { params })
// 获取应用所有版本号
export const GetApplicationVersions = (params) => request('get', API.APPLICATION_VERSIONS, { params })
// 删除应用某个版本
export const DeleteApplicationVersion = (params) => request('post', API.DELETE_APPLICATION_VERSION, { params })
// 删除应用
export const DeleteApplication = (params) => request('post', API.DELETE_APPLICATION, { params })

// 在应用中创建项目
export const AppCreateProject = (data) => request('post', API.APP_CREATE_PROJECT, { data })
// 向应用中增加项目
export const AppAddProject = (data) => request('post', API.APP_ADD_PROJECT, { data })
// 在应用中更新项目
export const AppUpdateProject = (data) => request('post', API.APP_UPDATE_PROJECT, { data })
// 在应用中升级项目
export const AppUpgradeProject = (data) => request('post', API.APP_UPGRADE_PROJECT, { data })
// 在应用中删除项目
export const AppDeleteProject = (data) => request('post', API.APP_DELETE_PROJECT, { data })

// 查询所有项目
export const GetProjectList = (params) => request('get', API.PROJECT_LIST, { params })
// 查询具体项目的信息
export const GetProjectInfo = (params) => request('get', API.PROJECT_INFO, { params })
// 新建项目
export const AddProject = (data) => request('post', API.ADD_PROJECT, { data })
// 删除项目
export const DeleteProject = (params) => request('post', API.DELETE_PROJECT, { params })
// 项目中有版本正在扫描中的个数
export const CheckRunning = (params) => request('get', API.CHECK_RUNNING, { params })

// 查询具体项目具体版本的组件信息-树形展示
export const GetProjectTree = (data) => request('post', API.PROJECT_TREE, { data })
// 查询具体项目具体版本的组件信息-平铺展示
export const GetProjectTiled = (data) => request('post', API.PROJECT_TILED, { data })
// 获取项目版本对比树
export const GetCompareTree = (data) => request('post', API.COMPARE_TREE, { data })

// 获取项目的所有版本
export const GetVersionList = (params) => request('get', API.VERSION_LIST, { params })
// 获取项目某个版本的信息
export const GetVersionInfo = (data) => request('post', API.VERSION_INFO, { data })
// 更新具体项目具体版本的pom信息
export const UpdateVersion = (data) => request('post', API.UPDATE_VERSION, { data })
// 删除具体项目的具体版本信息
export const DeleteVersion = (params) => request('post', API.DELETE_VERSION, { params })
// 版本升级
export const UpgradeVersion = (data) => request('post', API.UPGRADE_VERSION, { data })

// 分页查询组件
export const GetComponents = (data) => request('post', API.COMPONENT_LIST, { data })
// 查询组件详细信息
export const GetComponentInfo = (data) => request('post', API.COMPONENT_INFO, { data })
// 查询组件树形依赖
export const GetComponentTree = (data) => request('post', API.COMPONENT_TREE, { data })
// 查询组件平铺依赖
export const GetComponentTiled = (data) => request('post', API.COMPONENT_TILED, { data })
// 添加闭源组件
export const AddComponent = (data) => request('post', API.ADD_COMPONENT, { data })

// 文件合并
export const FileMerge = (data) => request('post', API.FILE_MERGE, { data })

// 导出项目简明依赖信息
export const ExportBrief = (data) => request('post', API.EXPORT_BRIEF, { data, responseType: 'blob' })
// 导出项目详细依赖信息
export const ExportDetail = (data) => request('post', API.EXPORT_DETAIL, { data, responseType: 'blob' })

// 用户登录
export const Login = (data) => request('post', API.LOGIN, { data })
// 用户鉴权
export const Auth = (params) => request('get', API.AUTH, { params })
// 用户注册
export const Register = (data) => request('post', API.REGISTER, { data })
