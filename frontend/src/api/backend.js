export const API = {
  // 分页获取项目信息
  PROJECT_LIST: '/qysca/project/findProjectInfoPage',
  // 查询具体项目的信息
  PROJECT_INFO: '/qysca/project/findProjectVersionPage',
  // 新建项目
  ADD_PROJECT: '/qysca/project/saveProject',
  // 删除项目
  DELETE_PROJECT: '',

  // 查询具体项目具体版本的组件信息-树形展示
  PROJECT_TREE: '/qysca/project/specificTree',
  // 查询具体项目具体版本的组件信息-平铺展示
  PROJECT_TILED: '/qysca/project/specificTable',

  // 获取项目的所有版本
  VERSION_LIST: '',
  // 获取项目某个版本的信息
  VERSION_INFO: '',
  // 更新项目某个版本的信息
  UPDATE_VERSION: '/qysca/project/specificUpdate',
  // 删除项目的某个版本
  DELETE_VERSION: '/qysca/project/specificDelete',
  // 版本升级
  UPGRADE_VERSION: '/qysca/project/specificUpgrade',

  // 分页查询开源组件
  OPEN_COMPONENT_LIST: '/qysca/components/findOpenComponentsPage',
  // 分页查询闭源组件
  CLOSE_COMPONENT_LIST: '/qysca/components/findCloseComponentsPage',
  // 添加闭源组件
  ADD_COMPONENT: '/qysca/components/saveCloseComponent',

  // 文件上传
  FILE_UPLOAD: '/qysca/file/chunk',
  // 文件合并
  FILE_MERGE: '/qysca/file/merge',

  // test
  SHOW_TREE: '/qysca/components/showTree'
}
