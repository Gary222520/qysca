<template>
  <div>
    <a-collapse v-model:activeKey="data.activeKey" collapsible="icon" @change="(index) => changeCollapse(index)">
      <a-collapse-panel v-for="(app, index) in props.appList" :key="index">
        <template #header>
          <div class="collapse_header">
            <div style="display: flex; align-items: center">
              <div style="margin-right: 20px; font-size: 18px; font-weight: bold">{{ app.name }}</div>
              <a-tag>{{ app.version }}</a-tag>
              <a-tag v-for="(lang, i) in app.language" :key="i">{{ lang }}</a-tag>
            </div>
            <div style="display: flex; align-items: center">
              <a-tooltip>
                <template #title>查看详情</template>
                <FileSearchOutlined
                  @click.stop="showAppDetail(app, false, index)"
                  :style="{ fontSize: '20px', color: '#00557c', marginRight: '10px' }" />
              </a-tooltip>
              <a-tooltip v-if="!props.parentApp.groupId">
                <template #title>删除组件</template>
                <DeleteOutlined
                  @click.stop="deleteComponent(app, true)"
                  :style="{ fontSize: '20px', color: '#ef0137', marginRight: '10px' }" />
              </a-tooltip>
            </div>
          </div>
        </template>
        <AppCollapse
          ref="appCollapse"
          :parent-app="app"
          :app-list="app.subAppList"
          :com-list="app.subComList"
          @refresh="refresh(data.currentKey)"></AppCollapse>
      </a-collapse-panel>
      <a-collapse-panel :showArrow="false" v-for="(com, index) in allComList" :key="props.appList.length + index">
        <template #header>
          <div class="collapse_header">
            <div style="display: flex; align-items: center">
              <div style="margin: 0 20px; font-size: 18px; font-weight: bold">{{ com.name }}</div>
              <a-tag>{{ com.version }}</a-tag>
              <a-tag>{{ com.language }}</a-tag>
            </div>
            <div style="display: flex; align-items: center">
              <a-tooltip>
                <template #title>查看详情</template>
                <FileSearchOutlined
                  @click.stop="showComDetail(com)"
                  :style="{ fontSize: '20px', color: '#00557c', marginRight: '10px' }" />
              </a-tooltip>
              <a-tooltip v-if="!props.parentApp.groupId">
                <template #title>删除组件</template>
                <DeleteOutlined
                  @click.stop="deleteComponent(com, false)"
                  :style="{ fontSize: '20px', color: '#ef0137', marginRight: '10px' }" />
              </a-tooltip>
            </div>
          </div>
        </template>
      </a-collapse-panel>
    </a-collapse>
    <AddAppModal ref="addAppModal" @success="refresh(data.currentKey)"></AddAppModal>
    <AddComModal ref="addComModal" @success="refresh(data.currentKey)"></AddComModal>
    <UpgradeAppModal ref="upgradeAppModal" @success="refresh(data.currentKey, true)"></UpgradeAppModal>
    <DeleteAppModal ref="deleteAppModal" @success="refreshParent()"></DeleteAppModal>
    <DeleteComModal ref="deleteComModal" @success="refreshParent()"></DeleteComModal>
    <Drawer ref="drawer" @refresh="refreshDrawer()"></Drawer>
    <ComDrawer ref="comDrawer"></ComDrawer>
  </div>
</template>

<script setup>
import {
  PlusOutlined,
  RocketOutlined,
  RedoOutlined,
  FileTextOutlined,
  FileAddOutlined,
  FileSearchOutlined,
  FolderOutlined,
  DeleteOutlined,
  SyncOutlined,
  WarningOutlined,
  LoadingOutlined,
  ExclamationCircleOutlined,
  LockOutlined,
  UnlockOutlined,
  EyeOutlined,
  EyeInvisibleOutlined
} from '@ant-design/icons-vue'
import { GetSubProject, GetVersionList, GetVersionInfo } from '@/api/frontend'
import { message } from 'ant-design-vue'
import { reactive, ref, defineEmits, defineProps, defineExpose, computed } from 'vue'
import AppCollapse from '@/views/application/components/AppCollapse.vue'
import AddAppModal from './AddAppModal.vue'
import AddComModal from './AddComModal.vue'
import UpgradeAppModal from './UpgradeAppModal.vue'
import DeleteAppModal from './DeleteAppModal.vue'
import DeleteComModal from './DeleteComModal.vue'
import Drawer from './Drawer.vue'
import ComDrawer from '@/views/project/components/Drawer.vue'
import { useRouter } from 'vue-router'

const emit = defineEmits(['refresh'])

const props = defineProps({
  parentApp: {
    type: Object,
    default: () => {}
  },
  appList: {
    type: Array,
    default: () => []
  },
  comList: {
    type: Object,
    default: () => {}
  }
})
const allComList = computed(() => {
  let res = []
  if (props.comList?.java) res = res.concat(props.comList.java)
  if (props.comList?.python) res = res.concat(props.comList.python)
  if (props.comList?.go) res = res.concat(props.comList.go)
  if (props.comList?.javaScript) res = res.concat(props.comList.javaScript)
  return res
})

const appCollapse = ref()
const addAppModal = ref()
const addComModal = ref()
const upgradeAppModal = ref()
const deleteAppModal = ref()
const deleteComModal = ref()
const drawer = ref()
const comDrawer = ref()
const router = useRouter()

const data = reactive({
  preKey: [],
  activeKey: [],
  currentKey: '',
  currentApp: {
    index: -1,
    version: ''
  }
})

const table = reactive({
  datasource: [],
  columns: [
    { title: '组织ID', dataIndex: 'groupId', key: 'groupId', width: 120 },
    { title: '工件ID', dataIndex: 'artifactId', key: 'artifactId', width: 120 },
    { title: '应用类型', dataIndex: 'type', key: 'type', width: 120 },
    { title: '语言', dataIndex: 'language', key: 'language', width: 120 },
    { title: '构建工具', dataIndex: 'builder', key: 'builder', width: 120 },
    { title: '扫描对象', dataIndex: 'scanner', key: 'scanner', width: 150 },
    { title: '最近一次更新时间', dataIndex: 'time', key: 'time', width: 210 },
    { title: '备注', dataIndex: 'description', key: 'description' },
    { title: '操作', dataIndex: 'action', key: 'action', width: 150 }
  ],
  pagination: {
    current: 1,
    total: 0,
    pageSize: 5,
    showSizeChanger: false,
    hideOnSinglePage: true
  }
})

const changeCollapse = async (index) => {
  if (index >= props.appList.length) return
  if (index === undefined) return
  data.currentKey = data.activeKey.filter((key) => !data.preKey.includes(key))[0]
  data.preKey = data.activeKey
  if (data.currentKey === undefined) return
  data.currentApp = props.appList[data.currentKey]
  await findSubProject(data.currentApp)
}

const changeVersion = async (app, version) => {
  if (version === undefined) {
    app.selection = {}
    await getVersionList(app, app.name)
    if (!app.versions || app.versions.length === 0) return
    version = app.versions[0]
  }
  await GetVersionInfo({ name: app.name, version })
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      // console.log('GetVersionInfo', res)
      Object.assign(app, res.data.applicationDO)
    })
    .catch((err) => {
      console.error(err)
    })
  data.currentApp = app
  await findSubProject(app)
  const index = data.activeKey.indexOf(String(data.currentKey))
  if (index < 0) return
  // console.log(appCollapse.value, index)
  appCollapse.value[index]?.close()
}

const getVersionList = async (app, name) => {
  await GetVersionList({ name })
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      // console.log('GetVersionList', res)
      app.selection.options = []
      app.versions = res.data
      res.data.forEach((item) => {
        const option = { label: item, value: item, key: item }
        app.selection.options.push(option)
      })
    })
    .catch((err) => {
      console.error(err)
    })
}

const findSubProject = async (app) => {
  app.selection = {}
  await getVersionList(app, app.name)
  app.selection.current = app.version
  await GetSubProject({ name: app.name, version: app.version })
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      // console.log('GetSubProject', res)
      app.subAppList = res.data.subApplication
      app.subComList = res.data.subComponent
    })
    .catch((err) => {
      console.error(err)
    })
  data.currentApp = app
}

const refreshParent = () => {
  emit('refresh')
}

const refresh = async (index, versionChange = false) => {
  data.currentKey = index
  const app = props.appList[index]
  if (versionChange) await changeVersion(app)
  else await changeVersion(app, app.version)
}

const refreshDrawer = async () => {
  refresh(data.currentKey)
  drawer.value.open(data.currentApp, true)
}

const addComponent = (app, index) => {
  data.currentKey = index
  addComModal.value.open(app)
}

const upgradeProject = (app, index) => {
  data.currentKey = index
  upgradeAppModal.value.open(app, props.parentApp)
}

const deleteVersion = (app, index) => {
  data.currentKey = index
  deleteAppModal.value.open(app, props.parentApp)
}

const showAppDetail = (app, isProject, index) => {
  data.currentKey = index
  drawer.value.open(app, isProject)
}

const showComDetail = (com) => {
  comDrawer.value.open(com, true)
}

const deleteComponent = (com, isProject) => {
  const comInfo = { ...com }
  if (isProject) comInfo.language = 'app'
  deleteComModal.value.open(comInfo, props.parentApp)
}

const close = () => {
  data.activeKey = []
  data.preKey = []
}

defineExpose({ refresh, close })
</script>

<style scoped>
.collapse_header {
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.action_icon {
  margin-right: 10px;
}
:deep(.ant-collapse) {
  border-radius: 0;
  border-top: 0;
}
:deep(.ant-collapse-header::before) {
  position: absolute;
  top: 50%;
  left: -33px;
  width: 33px;
  height: 1px;
  background-color: #d9d9d9;
  content: '';
}
:deep(.ant-collapse .ant-collapse-content > .ant-collapse-content-box) {
  padding: 16px 0px 16px 32px;
}
:deep(.ant-collapse > .ant-collapse-item:last-child),
:deep(.ant-collapse > .ant-collapse-item:last-child > .ant-collapse-header),
:deep(.ant-collapse .ant-collapse-item:last-child > .ant-collapse-content) {
  border-radius: 0;
}
</style>
