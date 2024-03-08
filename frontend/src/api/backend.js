export const API = {
  // 创建新应用
  CREATE_APPLICATION: '/qysca/application/createApplication',
  // 查询应用信息
  APPLICATION_LIST: '/qysca/application/getApplicationList',
  // 查询应用版本的信息
  APPLICATION_INFO: '/qysca/application/getApplicationVersion',
  // 获取应用所有版本号
  APPLICATION_VERSIONS: '/qysca/application/getApplicationVersionList',
  // 删除应用某个版本
  DELETE_APPLICATION_VERSION: '/qysca/application/deleteApplicationVersion',
  // 删除应用
  DELETE_APPLICATION: '/qysca/application/deleteApplication',

  // 在应用中创建项目
  APP_CREATE_PROJECT: '/qysca/application/createProject',
  // 向应用中增加项目
  APP_ADD_PROJECT: '/qysca/application/addProject',
  // 在应用中更新项目
  APP_UPDATE_PROJECT: '/qysca/application/updateProject',
  // 在应用中升级项目
  APP_UPGRADE_PROJECT: '/qysca/application/upgradeProject',
  // 在应用中删除项目
  APP_DELETE_PROJECT: '/qysca/application/deleteProject',

  // 分页获取项目信息
  PROJECT_LIST: '/qysca/project/findProjectPage',
  // 查询具体项目的信息
  PROJECT_INFO: '/qysca/project/findProjectVersionPage',
  // 新建项目
  ADD_PROJECT: '/qysca/project/saveProject',
  // 删除项目
  DELETE_PROJECT: '/qysca/project/deleteProject',
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
  EXPORT_DETAIL: '/qysca/project/exportTableExcelDetail'
}
