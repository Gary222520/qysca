<template>
  <div class="main">
    <div class="title">应用管理</div>
    <a-card class="content_card">
      <div class="operations">
        <a-button v-show="permit([ROLE.BU_REP, ROLE.BU_PO])" type="primary" @click="addApplication">
          <PlusOutlined />新建应用
        </a-button>
        <div></div>
        <a-dropdown>
          <a-input-search
            v-model:value="data.search.name"
            placeholder="请输入应用名称"
            style="width: 250px"
            @change="() => getNameList()"
            @search="() => findProject()"></a-input-search>
          <template #overlay>
            <a-menu v-if="menu.items.length" style="max-height: 300px; width: 250px; overflow-y: scroll">
              <a-menu-item v-for="(item, index) in menu.items" :key="index" @click="() => chooseName(item)">
                {{ item }}
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
      <a-collapse
        class="collapse"
        v-model:activeKey="data.activeKey"
        collapsible="icon"
        @change="(index) => changeCollapse(index)">
        <a-collapse-panel v-for="(app, index) in data.appList" :key="index">
          <template #header>
            <div class="collapse_header">
              <div style="display: flex; align-items: center">
                <div style="margin-right: 20px; font-size: 18px; font-weight: bold">{{ app.name }}</div>
                <a-tooltip v-if="app.selection">
                  <template #title>刷新</template>
                  <RedoOutlined :style="{ fontSize: '18px', color: '#00557c' }" @click.stop="refresh(index)" />
                </a-tooltip>
                <div v-if="app.selection" style="font-size: 18px; margin-left: 20px">
                  <a-input-group compact>
                    <a-input
                      value="版本选择"
                      style="width: 90px; cursor: default; background-color: #00557c; color: white; text-align: center"
                      readonly></a-input>
                    <a-select
                      v-model:value="app.selection.current"
                      :options="app.selection.options"
                      style="width: 90px"
                      @change="() => changeVersion(app, app.selection.current)">
                    </a-select>
                  </a-input-group>
                </div>
                <div v-if="app.selection" style="margin: 0 20px; display: flex; align-items: center">
                  <a-tooltip v-if="app.lock">
                    <template #title>点击解锁</template>
                    <a-tag class="warning-tag" @click="lock(app, index)" style="margin-right: 10px; cursor: pointer">
                      <template #icon><LockOutlined /></template>已锁定
                    </a-tag>
                  </a-tooltip>
                  <a-tooltip v-else>
                    <template #title>点击锁定</template>
                    <UnlockOutlined
                      @click.stop="lock(app, index)"
                      :style="{ fontSize: '20px', color: '#00557c', marginRight: '10px' }" />
                  </a-tooltip>
                  <a-tooltip v-if="app.release && !app.releaseStatus">
                    <template #title>取消发布</template>
                    <a-tag class="success-tag" @click="release(app, index)" style="margin-right: 10px; cursor: pointer">
                      <template #icon><EyeOutlined /></template>已发布
                    </a-tag>
                  </a-tooltip>
                  <a-tooltip v-if="!app.release && !app.releaseStatus">
                    <template #title>点击发布</template>
                    <CloudUploadOutlined
                      @click.stop="release(app, index)"
                      :style="{ fontSize: '20px', color: '#00557c', marginRight: '10px' }" />
                  </a-tooltip>
                  <div v-if="app.releaseStatus" style="display: flex; align-items: center">
                    <LoadingOutlined :style="{ fontSize: '20px', color: '#00557c' }" />
                    <div style="margin-left: 10px">{{ app.releaseStatus }}</div>
                  </div>
                </div>
              </div>
              <div style="display: flex; align-items: center">
                <a-tooltip>
                  <template #title>查看详情</template>
                  <FileSearchOutlined
                    @click.stop="showAppDetail(app, true, index)"
                    :style="{ fontSize: '20px', color: '#00557c', marginRight: '10px' }" />
                </a-tooltip>
                <a-tooltip>
                  <template #title>添加组件</template>
                  <PlusOutlined
                    @click.stop="addComponent(app, index)"
                    :style="{ fontSize: '20px', color: '#00557c', marginRight: '10px' }" />
                </a-tooltip>
                <a-tooltip>
                  <template #title>应用升级</template>
                  <RocketOutlined
                    v-show="permit([ROLE.BU_REP, ROLE.BU_PO])"
                    @click.stop="upgradeProject(app, index)"
                    :style="{ fontSize: '20px', color: '#00557c', marginRight: '10px' }" />
                </a-tooltip>
                <a-tooltip>
                  <template #title>删除该版本</template>
                  <DeleteOutlined
                    v-show="permit([ROLE.BU_REP])"
                    @click.stop="deleteVersion(app, index)"
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
      </a-collapse>
      <div v-if="pagination.total" class="pagination">
        <a-pagination
          v-model:current="pagination.current"
          :total="pagination.total"
          v-model:pageSize="pagination.pageSize"
          show-less-items
          :showSizeChanger="false"
          :hideOnSinglePage="true"
          @change="init" />
      </div>
    </a-card>
    <AddAppModal ref="addAppModal" @success="(app) => addAppComplete(app)"></AddAppModal>
    <AddDepModal ref="addDepModal" @success="refresh(data.currentKey)"></AddDepModal>
    <AddComModal ref="addComModal" @success="refresh(data.currentKey)"></AddComModal>
    <UpgradeAppModal ref="upgradeAppModal" @success="refresh(data.currentKey, true)"></UpgradeAppModal>
    <DeleteAppModal ref="deleteAppModal" @success="refresh(data.currentKey, true)"></DeleteAppModal>
    <ReleaseModal ref="releaseModal" @success="refresh(data.currentKey)"></ReleaseModal>
    <Drawer ref="drawer" @refresh="refreshDrawer()"></Drawer>
    <WarnModal ref="warnModal" @ok="refresh(data.currentKey)"></WarnModal>
  </div>
</template>

<script setup>
import {
  PlusOutlined,
  RedoOutlined,
  FileTextOutlined,
  FileAddOutlined,
  FileSearchOutlined,
  FolderOutlined,
  SyncOutlined,
  RocketOutlined,
  LoadingOutlined,
  ExclamationCircleOutlined,
  DeleteOutlined,
  LockOutlined,
  UnlockOutlined,
  EyeOutlined,
  CloudUploadOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { reactive, ref, onMounted } from 'vue'
import {
  GetProjectList,
  GetNameList,
  GetProject,
  GetSubProject,
  GetVersionList,
  GetVersionInfo,
  ChangeLock,
  ChangeRelease
} from '@/api/frontend'
import AddAppModal from './components/AddAppModal.vue'
import AddDepModal from './components/AddDepModal.vue'
import AddComModal from './components/AddComModal.vue'
import UpgradeAppModal from './components/UpgradeAppModal.vue'
import DeleteAppModal from './components/DeleteAppModal.vue'
import ReleaseModal from './components/ReleaseModal.vue'
import Drawer from './components/Drawer.vue'
import AppCollapse from '@/views/application/components/AppCollapse.vue'
import WarnModal from '@/components/WarnModal.vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ROLE, permit } from '@/utils/util.js'

onMounted(async () => {
  await getProjectList()
})

const addAppModal = ref()
const addDepModal = ref()
const addComModal = ref()
const upgradeAppModal = ref()
const deleteAppModal = ref()
const releaseModal = ref()
const drawer = ref()
const appCollapse = ref()
const warnModal = ref()

const router = useRouter()
const store = useStore()
const data = reactive({
  preKey: [],
  activeKey: [],
  currentKey: '',
  appList: [],
  currentApp: {},
  search: {
    name: ''
  }
})
const menu = reactive({
  items: []
})
const pagination = reactive({
  current: 1,
  total: 0,
  pageSize: 8,
  showSizeChanger: false,
  hideOnSinglePage: true
})

const getProjectList = async () => {
  await GetProjectList({ number: 1, size: 8 })
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      // console.log('GetProjectList', res)
      data.appList = res.data.content
    })
    .catch((err) => {
      console.error(err)
    })
}

const getNameList = async () => {
  await GetNameList({ name: data.search.name })
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      // console.log('GetNameList', res)
      menu.items = res.data
    })
    .catch((err) => {
      console.error(err)
    })
}

const chooseName = (name) => {
  data.search.name = name
}

const findProject = async () => {
  data.activeKey = []
  if (data.search.name) {
    await GetProject({ name: data.search.name })
      .then((res) => {
        if (res.code !== 200) {
          message.error(res.message)
          return
        }
        // console.log('GetProject', res)
        data.appList = [res.data]
      })
      .catch((err) => {
        console.error(err)
      })
  } else {
    getProjectList()
  }
}

const changeCollapse = async (index) => {
  if (index === undefined) return
  data.currentKey = data.activeKey.filter((key) => !data.preKey.includes(key))[0]
  data.preKey = data.activeKey
  if (data.currentKey === undefined) return
  data.currentApp = data.appList[data.currentKey]
  await findSubProject(data.currentApp)
}

const changeVersion = async (app, version) => {
  if (version === undefined) {
    app.selection = {}
    await getVersionList(app, app.name)
    if (app.versions.length === 0) {
      getProjectList()
      return
    }
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

const refresh = async (index, versionChange = false) => {
  if (index === undefined || index === null || index === '') {
    getProjectList()
    return
  }
  data.currentKey = index
  const app = data.appList[index]
  if (versionChange) await changeVersion(app)
  else await changeVersion(app, app.version)
}

const refreshDrawer = async () => {
  await refresh(data.currentKey)
  drawer.value.open(data.currentApp, true)
}

const addApplication = () => {
  addAppModal.value.open()
}

const addAppComplete = async (app) => {
  await getProjectList()
  if (app) addDepModal.value.open(app, true)
}

const addComponent = (app, index) => {
  data.currentKey = index
  addComModal.value.open(app)
}

const upgradeProject = (app, index) => {
  data.currentKey = index
  upgradeAppModal.value.open(app)
}

const deleteVersion = (app, index) => {
  data.currentKey = index
  deleteAppModal.value.open(app)
}

const showAppDetail = (app, isProject, index) => {
  data.currentKey = index
  drawer.value.open(app, isProject)
}

const lock = async (app, index) => {
  if (!permit([ROLE.BU_REP])) {
    message.error('权限不足')
    return
  }
  data.currentKey = index
  ChangeLock({ name: app.name, version: app.version })
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      // console.log('ChangeLock', res)
      refresh(index)
    })
    .catch((err) => {
      console.error(err)
    })
}

const release = async (app, index) => {
  data.currentKey = index
  if (!permit([ROLE.BU_PO, ROLE.APP_LEADER, ROLE.APP_MEMBER])) {
    message.error('权限不足')
    return
  }
  app.releaseStatus = app.release ? '取消发布中...' : '发布中...'
  if (app.release) {
    ChangeRelease({ name: app.name, version: app.version })
      .then((res) => {
        if (res.code !== 200) {
          app.releaseStatus = null
          refresh(index)
          message.error(res.message)
          return
        }
        // console.log('ChangeRelease', res)
        if (!res.data || res.data.length === 0) {
          message.success('取消发布成功')
        } else {
          warnModal.value.open(res.data, '有以下应用依赖该版本：')
        }
        app.releaseStatus = null
        refresh(index)
      })
      .catch((err) => {
        app.releaseStatus = null
        refresh(index)
        console.error(err)
      })
  } else {
    app.releaseStatus = null
    releaseModal.value.open(app)
  }
}
</script>

<style scoped>
.main {
  position: relative;
  height: 100%;
}
.title {
  font-weight: bold;
  font-size: 20px;
  margin-bottom: 15px;
}
.content_card {
  position: absolute;
  width: 100%;
  height: calc(100% - 30px);
  overflow-y: scroll;
}
.operations {
  display: flex;
  justify-content: space-between;
}
.collapse {
  margin-top: 20px;
  border-radius: 0;
}
.collapse_header {
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.action_icon {
  margin-right: 10px;
}
.btn:hover {
  border-color: #00557c;
  color: #00557c;
}
.pagination {
  display: flex;
  justify-content: right;
}
:deep(.ant-collapse) {
  border-radius: 0;
  border-top: 0;
}
:deep(.ant-collapse .ant-collapse-content > .ant-collapse-content-box) {
  padding: 16px 0px 16px 32px;
}
:deep(.ant-collapse > .ant-collapse-item:last-child),
:deep(.ant-collapse > .ant-collapse-item:last-child > .ant-collapse-header),
:deep(.ant-collapse .ant-collapse-item:last-child > .ant-collapse-content) {
  border-radius: 0;
}
:deep(.ant-collapse > .ant-collapse-item > .ant-collapse-header) {
  display: flex;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.04);
  border-top: 1px solid #d9d9d9;
}
</style>
<style scoped src="@/atdv/pagination.css"></style>
<style scoped src="@/atdv/input.css"></style>
<style scoped src="@/atdv/input-search.css"></style>
<style scoped src="@/atdv/select.css"></style>
<style scoped src="@/atdv/primary-btn.css"></style>
<style scoped src="@/atdv/delete-btn.css"></style>
<style scoped src="@/atdv/row-selection.css"></style>
<style scoped src="@/atdv/tag.css"></style>
