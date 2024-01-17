export const API = {
  // 分页获取项目信息
  PROJECT_LIST: '/qysca/project/findProjectInfoPage',
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

  // 分页查询开源组件
  OPEN_COMPONENT_LIST: '/qysca/components/findOpenComponentsPage',
  // 分页查询闭源组件
  CLOSE_COMPONENT_LIST: '/qysca/components/findCloseComponentsPage',
  // 查询开源组件详细信息
  OPEN_COMPONENT_INFO: '/qysca/components/findOpenComponentDetail',
  // 查询闭源组件详细信息
  CLOSE_COMPONENT_INFO: '/qysca/components/findCloseComponentDetail',
  // 查询开源组件树形依赖
  OPEN_COMPONENT_TREE: '/qysca/components/findOpenComponentDependencyTree',
  // 查询闭源组件树形依赖
  CLOSE_COMPONENT_TREE: '/qysca/components/findCloseComponentDependencyTree',
  // 查询开源组件平铺依赖
  OPEN_COMPONENT_TILED: '/qysca/components/findOpenComponentDependencyTable',
  // 查询闭源组件平铺依赖
  CLOSE_COMPONENT_TILED: '/qysca/components/findCloseComponentDependencyTable',
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
