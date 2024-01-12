export const API = {
  // 查询所有项目
  PROJECT_LIST: '/qysca/project/list',
  // 查询具体项目的信息
  PROJECT_INFO: '/qysca/project/specific',
  // 查询具体项目具体版本的组件信息-树形展示
  PROJECT_TREE: '/qysca/project/specificTree',
  // 查询具体项目具体版本的组件信息-平铺展示
  PROJECT_TILED: '/qysca/project/specificTable',
  // 新建项目
  ADD_PROJECT: '/qysca/project/save',

  // 更新具体项目具体版本的pom信息
  UPDATE_VERSION: '/qysca/project/specificUpdate',
  // 删除具体项目的具体版本信息
  DELETE_VERSION: '/qysca/project/specificDelete',
  // 版本升级
  UPGRADE_VERSION: '/qysca/project/specificUpgrade',

  // 文件上传
  FILE_UPLOAD: '/qysca/project/upload',

  // test
  SHOW_TREE: '/qysca/components/showTree'
}
