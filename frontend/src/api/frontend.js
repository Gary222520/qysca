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
// 新增/更新项目依赖信息
export const AddDependency = (data) => request('post', API.ADD_DEPENDENCY, { data })
// 向项目中增加组件
export const AddProjectComponent = (data) => request('post', API.ADD_PROJECT_COMPONENT, { data })
// 删除项目中的组件
export const DeleteProjectComponent = (data) => request('post', API.DELETE_PROJECT_COMPONENT, { data })
// 项目升级
export const UpgradeProject = (data) => request('post', API.UPGRADE_PROJECT, { data })
// 删除项目某个版本
export const DeleteProject = (data) => request('post', API.DELETE_PROJECT, { data })
// 查询具体项目的信息
export const GetProjectInfo = (params) => request('get', API.PROJECT_INFO, { params })
// 项目中有版本正在扫描中的个数
export const CheckRunning = (params) => request('get', API.CHECK_RUNNING, { params })
// 改变应用锁定状态
export const ChangeLock = (params) => request('post', API.CHANGE_LOCK, { params })
// 改变应用发布状态
export const ChangeRelease = (data) => request('post', API.CHANGE_RELEASE, { data })

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
// 模糊查询组件名称
export const GetComponentNameList = (params) => request('get', API.COMPONENT_NAME_LIST, { params })
// 查询组件详细信息
export const GetComponentInfo = (data) => request('post', API.COMPONENT_INFO, { data })
// 查询组件树形依赖
export const GetComponentTree = (data) => request('post', API.COMPONENT_TREE, { data })
// 查询组件平铺依赖
export const GetComponentTiled = (data) => request('post', API.COMPONENT_TILED, { data })
// 添加闭源组件
export const AddComponent = (data) => request('post', API.ADD_COMPONENT, { data })
// 修改闭源组件
export const UpdateComponent = (data) => request('post', API.UPDATE_COMPONENT, { data })
// 删除闭源组件
export const DeleteComponent = (data) => request('post', API.DELETE_COMPONENT, { data })

// 文件合并
export const FileMerge = (data) => request('post', API.FILE_MERGE, { data })

// 导出项目简明依赖信息
export const ExportBrief = (data) => request('post', API.EXPORT_BRIEF, { data, responseType: 'blob' })
// 导出项目详细依赖信息
export const ExportDetail = (data) => request('post', API.EXPORT_DETAIL, { data, responseType: 'blob' })

// 新增部门
export const CreateBu = (params) => request('get', API.CREATE_BU, { params })
// 删除部门
export const DeleteBu = (params) => request('get', API.DELETE_BU, { params })
// 查询部门列表
export const GetBuList = (params) => request('get', API.GET_BU_LIST, { params })

// 向应用中增加成员
export const AddAppMember = (data) => request('post', API.ADD_APP_MEMBER, { data })
// 在应用中删除成员
export const DeleteAppMember = (data) => request('post', API.DELETE_APP_MEMBER, { data })
// 在部门中增加Bu Rep
export const AddBuRep = (data) => request('post', API.ADD_BU_REP, { data })
// 在部门中删除BU Rep
export const DeleteBuRep = (data) => request('post', API.DELETE_BU_REP, { data })
// 向部门中添加成员
export const AddBuMember = (data) => request('post', API.ADD_BU_MEMBER, { data })
// 删除部门成员
export const DeleteBuMember = (params) => request('post', API.DELETE_BU_MEMBER, { params })
// 列出部门所有成员
export const GetBuMemberList = (params) => request('post', API.BU_MEMBER_LIST, { params })

// 获取用户列表
export const GetUserList = (params) => request('get', API.USER_LIST, { params })
// 用户登录
export const Login = (data) => request('post', API.LOGIN, { data })
// 用户鉴权
export const GetUserInfo = () => request('get', API.GET_USER_INFO, {})
// 用户登出
export const Logout = () => request('get', API.LOGOUT, {})
// 用户注册
export const Register = (data) => request('post', API.REGISTER, { data })
// 更新用户信息
export const UpdateUser = (data) => request('post', API.UPDATE_USER, { data })
// 删除用户
export const DeleteUser = (params) => request('post', API.DELETE_USER, { params })
