<template>
  <div class="main">
    <div class="title">应用管理</div>
    <a-card class="content_card">
      <div class="operations">
        <a-button type="primary" @click="addApplication"><PlusOutlined />新建应用</a-button>
        <a-dropdown>
          <a-input-search
            v-model:value="data.search.name"
            placeholder="请输入项目名称"
            style="width: 250px; margin-right: 10px"
            @change="() => getNameList()"
            @search="() => findProject()"></a-input-search>
          <template #overlay>
            <a-menu v-if="menu.items.length">
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
        accordion
        @change="(index) => findSubProject(index)">
        <a-collapse-panel v-for="(app, index) in data.appList" :key="index">
          <template #header>
            <div class="collapse_header">
              <div style="display: flex; align-items: center">
                <div style="margin-right: 20px; font-size: 18px; font-weight: bold">{{ app.name }}</div>
                <a-tooltip v-if="app.operation">
                  <template #title>刷新</template>
                  <RedoOutlined :style="{ fontSize: '18px', color: '#6f005f' }" @click.stop="refresh()" />
                </a-tooltip>
                <div v-if="app.operation" style="font-size: 18px; margin-left: 20px">
                  <a-input-group compact>
                    <a-input
                      value="版本选择"
                      style="width: 90px; cursor: default; background-color: #6f005f; color: white; text-align: center"
                      readonly></a-input>
                    <a-select
                      v-model:value="app.selection.current"
                      :options="app.selection.options"
                      style="width: 100px"
                      @change="() => changeVersion(index, app.selection.current)">
                    </a-select>
                  </a-input-group>
                </div>
                <div v-if="app.operation" style="margin-left: 20px">
                  <a-tag color="purple">
                    <template #icon><FolderOutlined /></template>项目
                  </a-tag>
                  <a-tag v-if="app.lock" color="warning">
                    <template #icon><LockOutlined /></template>已上锁
                  </a-tag>
                  <a-tag v-if="app.release" color="success">
                    <template #icon><EyeOutlined /></template>已发布
                  </a-tag>
                </div>
              </div>
              <div v-if="app.operation" style="display: flex; align-items: center">
                <a-button type="primary" @click.stop="() => addProject(app, index)" style="margin-right: 10px">
                  <PlusOutlined />添加项目
                </a-button>
                <a-button type="primary" @click.stop="() => upgradeProject(app, index)" style="margin-right: 10px">
                  <RocketOutlined />项目升级
                </a-button>
                <a-button type="primary" danger @click.stop="() => deleteVersion(app)" style="margin-right: 10px">
                  <DeleteOutlined />删除该版本
                </a-button>
              </div>
            </div>
          </template>
          <!-- <div>
            <a-button v-if="!app.showInfo" type="primary" @click="() => (app.showInfo = true)">查看项目信息</a-button>
            <a-button v-else type="primary" @click="() => (app.showInfo = false)">收起项目信息</a-button>
          </div> -->
          <a-table
            :data-source="[app]"
            :columns="table.columns"
            :pagination="false"
            bordered
            style="margin-right: 8px; margin-bottom: 8px">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'action'">
                <div style="display: flex" v-if="record.state === 'CREATED'">
                  <div class="action_icon">
                    <a-tooltip>
                      <template #title>添加依赖信息</template>
                      <FileAddOutlined
                        :style="{ fontSize: '18px', color: '#6f005f' }"
                        @click="addDependency(app, index)" />
                    </a-tooltip>
                  </div>
                </div>
                <div style="display: flex" v-if="record.state === 'SUCCESS'">
                  <div class="action_icon">
                    <a-tooltip>
                      <template #title>组件依赖信息</template>
                      <FileTextOutlined :style="{ fontSize: '18px', color: '#6f005f' }" @click="showDetail(record)" />
                    </a-tooltip>
                  </div>
                  <div class="action_icon">
                    <a-tooltip>
                      <template #title>更新依赖信息</template>
                      <SyncOutlined
                        :style="{ fontSize: '18px', color: '#6f005f' }"
                        @click="updateDependency(app, index)" />
                    </a-tooltip>
                  </div>
                </div>
                <div style="display: flex; align-items: center" v-if="record.state === 'RUNNING'">
                  <LoadingOutlined :style="{ fontSize: '18px', color: '#6f005f' }" />
                  <div style="margin-left: 10px">扫描分析中...</div>
                </div>
                <div style="display: flex; align-items: center" v-if="record.state === 'FAILED'">
                  <a-popconfirm v-model:open="record.popconfirm" title="扫描出错，请重试">
                    <template #cancelButton>
                      <a-button class="cancel_btn" size="small" @click="retry(record, index)">重试</a-button>
                    </template>
                    <template #okButton></template>
                    <ExclamationCircleOutlined :style="{ fontSize: '18px', color: '#ff4d4f' }" />
                    <span style="margin-left: 10px; color: #ff4d4f; cursor: pointer">扫描失败</span>
                  </a-popconfirm>
                </div>
              </template>
            </template>
            <template #emptyText>暂无数据</template>
          </a-table>
          <AppCollapse
            ref="appCollapse"
            :parent-app="app"
            :app-list="app.subAppList"
            :com-list="app.subComList"
            @refresh="(version) => refreshChildren(version)"></AppCollapse>
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
    <AddAppModal ref="addAppModal" @root="getProjectList()" @notroot="refresh(data.currentApp.version)"></AddAppModal>
    <AddDepModal ref="addDepModal" @success="refresh(data.currentApp.version)"></AddDepModal>
    <UpgradeAppModal ref="upgradeAppModal" @success="refresh()"></UpgradeAppModal>
    <DeleteAppModal ref="deleteAppModal" @success="refreshChildren()"></DeleteAppModal>
  </div>
</template>

<script setup>
import {
  PlusOutlined,
  RedoOutlined,
  FileTextOutlined,
  FileAddOutlined,
  FolderOutlined,
  SyncOutlined,
  RocketOutlined,
  LoadingOutlined,
  ExclamationCircleOutlined,
  DeleteOutlined,
  LockOutlined,
  EyeOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { reactive, ref, onMounted } from 'vue'
import { GetProjectList, GetNameList, GetProject, GetSubProject, GetVersionList } from '@/api/frontend'
import AddAppModal from './components/AddAppModal.vue'
import AddDepModal from './components/AddDepModal.vue'
import UpgradeAppModal from './components/UpgradeAppModal.vue'
import DeleteAppModal from './components/DeleteAppModal.vue'
import AppCollapse from '@/views/application/components/AppCollapse.vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'

onMounted(async () => {
  await getProjectList()
})

const addAppModal = ref()
const addDepModal = ref()
const upgradeAppModal = ref()
const deleteAppModal = ref()
const appCollapse = ref()

const router = useRouter()
const store = useStore()
const data = reactive({
  activeKey: [],
  appList: [],
  currentApp: {
    index: 0,
    version: ''
  },
  search: {
    name: ''
  }
})
const menu = reactive({
  items: []
})
const table = reactive({
  datasource: [],
  columns: [
    { title: '组织ID', dataIndex: 'groupId', key: 'groupId', width: 120 },
    { title: '工件ID', dataIndex: 'artifactId', key: 'artifactId', width: 120 },
    { title: '项目类型', dataIndex: 'type', key: 'type', width: 120 },
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
const pagination = reactive({
  current: 1,
  total: 0,
  pageSize: 5,
  showSizeChanger: false,
  hideOnSinglePage: true
})

const getProjectList = async (page = 1, size = 5) => {
  // data.activeKey = []
  await GetProjectList({ number: page, size })
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

const getVersionList = async (app, groupId, artifactId) => {
  await GetVersionList({ groupId, artifactId })
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

const changeVersion = async (index, version) => {
  await findSubProject(index, version)
  appCollapse.value[data.currentApp.index].close()
}

const findSubProject = async (index, version) => {
  if (!index && index !== 0) return
  const app = data.appList[index]
  if (!app) return
  app.operation = true
  app.selection = {}
  await getVersionList(app, app.groupId, app.artifactId)
  if (version) app.selection.current = version
  else app.selection.current = app.version
  data.currentApp.index = index
  data.currentApp.version = app.selection.current
  await GetSubProject({ groupId: app.groupId, artifactId: app.artifactId, version: app.selection.current })
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      // console.log('GetSubProject', res)
      app.subAppList = res.data.subProject
      app.subComList = res.data.subComponent
    })
    .catch((err) => {
      console.error(err)
    })
}

const refresh = async (version) => {
  await getProjectList()
  await findSubProject(data.currentApp.index, version)
  appCollapse.value[data.currentApp.index]?.close()
}

const refreshChildren = async (version) => {
  await getProjectList()
  await findSubProject(data.currentApp.index, data.currentApp.version)
  appCollapse.value[data.currentApp.index]?.refresh(version)
}

const addApplication = () => {
  addAppModal.value.open()
}

const addProject = (app, index) => {
  data.currentApp.index = index
  data.currentApp.version = app.selection.current
  addAppModal.value.open(app.id, null)
}

const upgradeProject = (app, index) => {
  data.currentApp.index = index
  data.currentApp.version = app.selection.current
  upgradeAppModal.value.open(app)
}

const addDependency = (app, index) => {
  data.currentApp.index = index
  data.currentApp.version = app.selection.current
  addDepModal.value.open(app, true)
}

const updateDependency = (app, index) => {
  data.currentApp.index = index
  data.currentApp.version = app.selection.current
  addDepModal.value.open(app, false)
}

const retry = (record, index) => {
  record.popconfirm = false
  updateDependency(record, index)
}

const deleteVersion = (app) => {
  deleteAppModal.value.open(app)
}

const showDetail = (record) => {
  router.push({
    path: '/home/appDetail',
    query: {
      groupId: record.groupId,
      artifactId: record.artifactId,
      version: record.version
    }
  })
}
</script>

<style scoped>
.main {
  position: relative;
  height: 100%;
}
.title {
  font-weight: bold;
  font-size: 24px;
  margin-bottom: 15px;
}
.content_card {
  position: absolute;
  width: 100%;
  height: calc(100% - 32px);
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
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.action_icon {
  margin-right: 10px;
}
.cancel_btn:hover {
  border-color: #6f005f;
  color: #6f005f;
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
  padding: 16px 0px 16px 16px;
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
