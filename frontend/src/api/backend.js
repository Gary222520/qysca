export const API = {
  // 分页获取根项目信息
  PROJECT_LIST: '/qysca/project/findRootPage',
  // 模糊查询项目名称
  NAME_LIST: '/qysca/project/searchProjectName',
  // 根据名称查询项目 并返回项目的最新版本
  FIND_PROJECT: '/qysca/project/findProject',
  // 查询子项目和子组件
  FIND_SUB_PROJECT: '/qysca/project/findSubProject',
  // 新增/更新项目
  ADD_PROJECT: '/qysca/project/saveProject',
  // 向项目中增加已有子项目
  ADD_SUBPROJECT: '/qysca/project/addSubProject',
  // 新增/更新项目依赖信息
  ADD_DEPENDENCY: '/qysca/project/saveProjectDependency',
  // 向项目中增加组件
  ADD_PROJECT_COMPONENT: '/qysca/project/saveProjectComponent',
  // 删除项目中的组件
  DELETE_PROJECT_COMPONENT: '/qysca/project/deleteProjectComponent',
  // 项目升级
  UPGRADE_PROJECT: '/qysca/project/upgradeProject',
  // 删除项目
  DELETE_PROJECT: '/qysca/project/deleteProjectVersion',
  // 查询具体项目的信息
  PROJECT_INFO: '/qysca/project/findProjectVersionPage',
  // 项目中有版本正在扫描中的个数
  CHECK_RUNNING: '/qysca/project/checkRunningProject',

  // 查询具体项目具体版本的组件信息-树形展示
  PROJECT_TREE: '/qysca/project/findProjectDependencyTree',
  // 查询具体项目具体版本的组件信息-平铺展示
  PROJECT_TILED: '/qysca/project/findProjectDependencyTable',
  // 获取项目版本对比树
  COMPARE_TREE: '/qysca/project/getProjectVersionCompareTree',

  // 获取项目的所有版本
  VERSION_LIST: '/qysca/project/getVersionsList',
  // 获取项目某个版本的信息
  VERSION_INFO: '/qysca/project/findProjectVersionInfo',
  // 更新项目某个版本的信息
  UPDATE_VERSION: '/qysca/project/updateProject',
  // 删除项目的某个版本
  DELETE_VERSION: '/qysca/project/deleteProjectVersion',
  // 版本升级
  UPGRADE_VERSION: '/qysca/project/upgradeProject',

  // 分页查询组件
  COMPONENT_LIST: '/qysca/components/findComponentsPage',
  // 查询组件详细信息
  COMPONENT_INFO: '/qysca/components/findComponentDetail',
  // 查询组件树形依赖
  COMPONENT_TREE: '/qysca/components/findComponentDependencyTree',
  // 查询组件平铺依赖
  COMPONENT_TILED: '/qysca/components/findComponentDependencyTable',
  // 添加闭源组件
  ADD_COMPONENT: '/qysca/components/saveCloseComponent',

  // 文件上传
  FILE_UPLOAD: '/qysca/file/chunk',
  // 文件合并
  FILE_MERGE: '/qysca/file/merge',

  // 导出项目简明依赖信息
  EXPORT_BRIEF: '/qysca/project/exportTableExcelBrief',
  // 导出项目详细依赖信息
  EXPORT_DETAIL: '/qysca/project/exportTableExcelDetail',

  // 用户登录
  LOGIN: '/qysca/user/login',
  // 用户鉴权
  AUTH: '/qysca/user/auth',
  // 用户注册
  REGISTER: '/qysca/user/register'
}
