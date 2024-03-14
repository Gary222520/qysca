import { request } from './request'
import { API } from './backend'

// 查询所有项目
export const GetProjectList = (params) => request('get', API.PROJECT_LIST, { params })
// 模糊查询项目名称
export const GetNameList = (params) => request('get', API.NAME_LIST, { params })
// 根据名称查询项目 并返回项目的最新版本
export const GetProject = (params) => request('get', API.FIND_PROJECT, { params })
// 查询子项目和子组件
export const GetSubProject = (params) => request('get', API.FIND_SUB_PROJECT, { params })
// 新建项目
export const AddProject = (data) => request('post', API.ADD_PROJECT, { data })
// 向项目中增加已有子项目
export const AddSubProject = (data) => request('post', API.ADD_SUBPROJECT, { data })
// 新增/更新项目依赖信息
export const AddDependency = (data) => request('post', API.ADD_DEPENDENCY, { data })
// 向项目中增加组件
export const AddProjectComponent = (data) => request('post', API.ADD_PROJECT_COMPONENT, { data })
// 删除项目中的组件
export const DeleteProjectComponent = (data) => request('post', API.DELETE_PROJECT_COMPONENT, { data })
// 项目升级
export const UpgradeProject = (data) => request('post', API.UPGRADE_PROJECT, { data })
// 删除项目
export const DeleteProject = (data) => request('post', API.DELETE_PROJECT, { data })
// 查询具体项目的信息
export const GetProjectInfo = (params) => request('get', API.PROJECT_INFO, { params })
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
