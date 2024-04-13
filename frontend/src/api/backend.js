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
  COMPONENT_LIST: '/qysca/component/findComponentsPage',
  // 模糊查询组件名称
  COMPONENT_NAME_LIST: '/qysca/component/searchComponentName',
  // 查询组件详细信息
  COMPONENT_INFO: '/qysca/component/findComponentDetail',
  // 查询组件树形依赖
  COMPONENT_TREE: '/qysca/component/findComponentDependencyTree',
  // 查询组件平铺依赖
  COMPONENT_TILED: '/qysca/component/findComponentDependencyTable',
  // 添加闭源组件
  ADD_COMPONENT: '/qysca/component/saveCloseComponent',
  // 修改闭源组件
  UPDATE_COMPONENT: '/qysca/component/updateCloseComponent',
  // 删除闭源组件
  DELETE_COMPONENT: '/qysca/component/deleteCloseComponent',

  // 许可证库界面查看许可证列表
  ALL_LICENSE: '/qysca/license/getLicensePage',
  // 获取某个应用的许可证列表
  APP_LICENSE_LIST: '/qysca/license/getAppLicense',
  // 获取某个组件的许可证列表
  COM_LICENSE_LIST: '/qysca/license/getComponentLicense',
  // 查看许可证详细内容
  LICENSE_INFO: '/qysca/license/getLicenseInfo',
  // 在某个应用中增加许可证
  ADD_LICENSE: '/qysca/license/addAppLicense',
  // 删除某个应用许可证
  DELETE_LICENSE: '/qysca/license/deleteAppLicense',
  // 查看应用的许可证冲突信息
  APP_LICENSE_CONFLICT: '/qysca/license/getAppLicenseConflict',
  // 查看组件的许可证冲突信息
  COM_LICENSE_CONFLICT: '/qysca/license/getComponentLicenseConflict',

  // 查看漏洞库界面
  ALL_VUL: '/qysca/vulnerability/getVulnerabilityPage',
  // 获取某个应用的漏洞列表
  APP_VUL_LIST: '/qysca/vulnerability/getApplicationVulnerabilityList',
  // 获取某个组件的漏洞列表
  COM_VUL_LIST: '/qysca/vulnerability/getComponentVulnerabilityList',
  // 查看某个漏洞的详细信息
  VUL_INFO: '/qysca/vulnerability/getVulnerabilityById',
  // 查看某个漏洞类型的详细信息
  VUL_TYPE_INFO: '/qysca/vulnerability/getCweById',
  // 在某个应用中增加漏洞信息
  ADD_VUL: '/qysca/vulnerability/addAppVulnerability',
  // 在某个应用中删除漏洞信息
  DELETE_VUL: '/qysca/vulnerability/deleteAppVulnerability',

  // 查询用户所在部门的应用总数
  STATISTIC_APP: '/qysca/statistics/getApplicationCount',
  // 查询用户所在部门的应用的组件总数
  STATISTIC_COM: '/qysca/statistics/getComponentCount',
  // 查询用户所在部门的应用的漏洞统计
  STATISTIC_VUL: '/qysca/statistics/getVulnerabilityStatistics',
  // 查询用户所在部门的应用的许可证统计
  STATISTIC_LICENSE: '/qysca/statistics/getLicenseStatistics',

  // 文件上传
  FILE_UPLOAD: '/qysca/file/chunk',
  // 文件合并
  FILE_MERGE: '/qysca/file/merge',

  // 导出项目简明依赖信息
  EXPORT_BRIEF: '/qysca/application/exportTableExcelBrief',
  // 导出项目详细依赖信息
  EXPORT_DETAIL: '/qysca/application/exportTableExcelDetail',
  // 导出SBOM
  EXPORT_SBOM: '/qysca/application/exportSBOM',
  // 导出报告
  EXPORT_REPORT: '/qysca/application/exportHtml',

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
