export const API = {
  // 分页获取根项目信息
  PROJECT_LIST: '/qysca/application/findApplicationPage',
  // 模糊查询项目名称
  NAME_LIST: '/qysca/application/searchApplicationName',
  // 根据名称查询项目 并返回项目的最新版本
  FIND_PROJECT: '/qysca/application/findApplication',
  // 查询子项目和子组件
  FIND_SUB_PROJECT: '/qysca/application/findSubApplication',
  // 新增/更新项目
  ADD_PROJECT: '/qysca/application/saveApplication',
  // 新增/更新项目依赖信息
  ADD_DEPENDENCY: '/qysca/application/saveApplicationDependency',
  // 向项目中增加组件
  ADD_PROJECT_COMPONENT: '/qysca/application/saveApplicationComponent',
  // 删除项目中的组件
  DELETE_PROJECT_COMPONENT: '/qysca/application/deleteApplicationComponent',
  // 项目升级
  UPGRADE_PROJECT: '/qysca/application/upgradeApplication',
  // 删除项目某个版本
  DELETE_PROJECT: '/qysca/application/deleteApplicationVersion',
  // 查询具体项目的信息
  PROJECT_INFO: '/qysca/application/findApplicationVersionPage',
  // 项目中有版本正在扫描中的个数
  CHECK_RUNNING: '/qysca/application/checkRunningApplication',
  // 改变应用锁定状态
  CHANGE_LOCK: '/qysca/application/changeLockState',
  // 改变应用发布状态
  CHANGE_RELEASE: '/qysca/application/changeReleaseState',

  // 查询具体项目具体版本的组件信息-树形展示
  PROJECT_TREE: '/qysca/application/findApplicationDependencyTree',
  // 查询具体项目具体版本的组件信息-平铺展示
  PROJECT_TILED: '/qysca/application/findApplicationDependencyTable',
  // 获取项目版本对比树
  COMPARE_TREE: '/qysca/application/getApplicationVersionCompareTree',

  // 获取项目的所有版本
  VERSION_LIST: '/qysca/application/getVersionsList',
  // 获取项目某个版本的信息
  VERSION_INFO: '/qysca/application/findApplicationVersionInfo',
  // 更新项目某个版本的信息
  UPDATE_VERSION: '/qysca/application/updateApplication',
  // 删除项目的某个版本
  DELETE_VERSION: '/qysca/application/deleteApplicationVersion',
  // 版本升级
  UPGRADE_VERSION: '/qysca/application/upgradeApplication',

  // 分页查询组件
  COMPONENT_LIST: '/qysca/components/findComponentsPage',
  // 模糊查询组件名称
  COMPONENT_NAME_LIST: '/qysca/components/searchComponentName',
  // 查询组件详细信息
  COMPONENT_INFO: '/qysca/components/findComponentDetail',
  // 查询组件树形依赖
  COMPONENT_TREE: '/qysca/components/findComponentDependencyTree',
  // 查询组件平铺依赖
  COMPONENT_TILED: '/qysca/components/findComponentDependencyTable',
  // 添加闭源组件
  ADD_COMPONENT: '/qysca/components/saveCloseComponent',
  // 修改闭源组件
  UPDATE_COMPONENT: '/qysca/components/updateCloseComponent',
  // 删除闭源组件
  DELETE_COMPONENT: '/qysca/components/deleteCloseComponent',

  // 文件上传
  FILE_UPLOAD: '/qysca/file/chunk',
  // 文件合并
  FILE_MERGE: '/qysca/file/merge',

  // 导出项目简明依赖信息
  EXPORT_BRIEF: '/qysca/application/exportTableExcelBrief',
  // 导出项目详细依赖信息
  EXPORT_DETAIL: '/qysca/application/exportTableExcelDetail',

  // 新增部门
  CREATE_BU: '/qysca/bu/addBu',
  // 删除部门
  DELETE_BU: '/qysca/bu/deleteBu',
  // 查询部门列表
  GET_BU_LIST: '/qysca/bu/listAllBu',

  // 向应用中增加成员
  ADD_APP_MEMBER: '/qysca/role/addAppMember',
  // 在应用中删除成员
  DELETE_APP_MEMBER: '/qysca/role/deleteAppMember',
  // 在应用中增加Bu Rep
  ADD_BU_REP: '/qysca/role/addBuRep',
  // 在应用中删除BU Rep
  DELETE_BU_REP: '/qysca/role/deleteBuRep',
  // 向部门中添加成员
  ADD_BU_MEMBER: '/qysca/role/addBuMember',
  // 删除部门成员
  DELETE_BU_MEMBER: '/qysca/role/deleteBuMember',
  // 列出部门所有成员
  BU_MEMBER_LIST: '/qysca/role/listBuMember',

  // 获取用户列表
  USER_LIST: '/qysca/user/listAllUser',
  // 用户登录
  LOGIN: '/qysca/user/login',
  // 获取用户信息
  GET_USER_INFO: '/qysca/user/getUserInfo',
  // 用户登出
  LOGOUT: '/qysca/user/logout',
  // 用户注册
  REGISTER: '/qysca/user/register',
  // 更新用户信息
  UPDATE_USER: '/qysca/user/updateUser',
  // 删除用户
  DELETE_USER: '/qysca/user/deleteUser'
}
