<template>
  <div class="main">
    <div class="title">应用管理</div>
    <a-card class="content_card">
      <div class="operations">
        <a-button type="primary" @click="addApplication"><PlusOutlined />新建应用</a-button>
        <!-- <div style="display: flex; align-items: center">
          <a-input
            v-model:value="data.search.groupId"
            addon-before="组织ID"
            placeholder="输入groupId..."
            style="width: 200px; margin-right: 10px"></a-input>
          <a-input
            v-model:value="data.search.artifactId"
            addon-before="工件ID"
            placeholder="输入artifactId..."
            style="width: 200px; margin-right: 10px"></a-input>
          <a-button type="primary" @click="init" style="margin-right: 10px"><SearchOutlined />搜索应用</a-button>
        </div> -->
      </div>
      <a-collapse
        class="collapse"
        v-model:activeKey="data.activeKey"
        :bordered="false"
        collapsible="icon"
        accordion
        @change="showApplicationInfo">
        <a-collapse-panel v-for="app in data.applications" :key="app.name">
          <template #header>
            <div class="collapse_header">
              <div style="display: flex; align-items: center">
                <div style="margin-right: 10px; font-size: 18px">{{ app.name }}</div>
                <a-tooltip>
                  <template #title>刷新</template>
                  <RedoOutlined
                    :style="{ fontSize: '18px', color: '#6f005f' }"
                    @click.stop="showApplicationInfo(app.name)" />
                </a-tooltip>
                <div v-if="app.id === data.currentApp?.id" style="margin-left: 30px; font-size: 18px">
                  <a-input-group compact>
                    <a-input
                      value="版本选择"
                      style="width: 90px; cursor: default; background-color: #6f005f; color: white; text-align: center"
                      readonly></a-input>
                    <a-select
                      v-model:value="selection.current"
                      :options="selection.options"
                      style="width: 100px"
                      @change="changeVersion">
                    </a-select>
                  </a-input-group>
                </div>
              </div>
              <div v-if="app.id === data.currentApp?.id" style="display: flex; align-items: center">
                <a-button type="primary" @click.stop="addProject" style="margin-right: 10px">
                  <PlusOutlined />添加项目
                </a-button>
                <a-button type="primary" @click.stop="upgradeApp" style="margin-right: 10px">
                  <RocketOutlined />应用升级
                </a-button>
                <a-button type="primary" danger @click.stop="deleteVersion()" style="margin-right: 10px">
                  <WarningOutlined />删除该版本
                </a-button>
                <a-button type="primary" danger @click.stop="deleteApplication()">
                  <WarningOutlined />删除应用
                </a-button>
              </div>
            </div>
          </template>
          <a-table :data-source="table.datasource" :columns="table.columns" :pagination="table.pagination" bordered>
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'action'">
                <div style="display: flex" v-if="record.state === 'SUCCESS'">
                  <div class="action_icon">
                    <a-tooltip>
                      <template #title>组件依赖信息</template>
                      <FileTextOutlined :style="{ fontSize: '18px', color: '#6f005f' }" @click="showDetail(record)" />
                    </a-tooltip>
                  </div>
                  <!-- <div class="action_icon">
                    <a-tooltip>
                      <template #title>其他版本</template>
                      <EllipsisOutlined :style="{ fontSize: '18px', color: '#6f005f' }" @click="subApplication()" />
                    </a-tooltip>
                  </div> -->
                  <div class="action_icon">
                    <a-tooltip>
                      <template #title>更新</template>
                      <SyncOutlined
                        :style="{ fontSize: '18px', color: '#6f005f' }"
                        @click="updateProject(app, record)" />
                    </a-tooltip>
                  </div>
                  <div class="action_icon">
                    <a-tooltip>
                      <template #title>升级</template>
                      <RocketOutlined :style="{ fontSize: '18px', color: '#6f005f' }" @click="upgradeProject(record)" />
                    </a-tooltip>
                  </div>
                  <div class="action_icon">
                    <a-tooltip>
                      <template #title>删除</template>
                      <a-popconfirm v-model:open="record.popconfirm" title="确定删除这个项目吗？">
                        <template #cancelButton>
                          <a-button class="cancel_btn" size="small" @click="record.popconfirm = false">取消</a-button>
                        </template>
                        <template #okButton>
                          <a-button danger type="primary" size="small" @click="deleteProject(record)">删除</a-button>
                        </template>
                        <DeleteOutlined :style="{ fontSize: '18px', color: '#ff4d4f' }" />
                      </a-popconfirm>
                    </a-tooltip>
                  </div>
                </div>
                <div style="display: flex; align-items: center" v-if="record.state === 'RUNNING'">
                  <LoadingOutlined :style="{ fontSize: '18px', color: '#6f005f' }" />
                  <div style="margin-left: 10px">扫描分析中...</div>
                </div>
                <div style="display: flex; align-items: center" v-if="record.state === 'FAILED'">
                  <a-popconfirm v-model:open="record.popconfirm" title="扫描出错，请重试或删除">
                    <template #cancelButton>
                      <a-button class="cancel_btn" size="small" @click="retry(record)">重试</a-button>
                    </template>
                    <template #okButton>
                      <a-button danger type="primary" size="small" @click="deleteProject(record)">删除</a-button>
                    </template>
                    <ExclamationCircleOutlined :style="{ fontSize: '18px', color: '#ff4d4f' }" />
                    <span style="margin-left: 10px; color: #ff4d4f; cursor: pointer">扫描失败</span>
                  </a-popconfirm>
                </div>
              </template>
            </template>
            <template #emptyText>暂无数据</template>
          </a-table>
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
    <AddAppModal ref="addAppModal" @success="init"></AddAppModal>
    <AddProModal ref="addProModal" @success="showApplicationInfo(data.currentApp.name)"></AddProModal>
    <UpdateProModal ref="updateProModal" @success="showApplicationInfo(data.currentApp.name)"></UpdateProModal>
    <UpgradeProModal ref="upgradeProModal" @success="showApplicationInfo(data.currentApp.name)"></UpgradeProModal>
    <DeleteAppModal ref="deleteAppModal" @success="init"></DeleteAppModal>
  </div>
</template>

<script setup>
import {
  PlusOutlined,
  SearchOutlined,
  RedoOutlined,
  FileTextOutlined,
  SyncOutlined,
  RocketOutlined,
  EllipsisOutlined,
  BarsOutlined,
  WarningOutlined,
  LoadingOutlined,
  ExclamationCircleOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { reactive, ref, onMounted } from 'vue'
import {
  CreateApplication,
  GetApplicationList,
  GetApplicationInfo,
  GetApplicationVersions,
  DeleteApplication,
  AppDeleteProject
} from '@/api/frontend'
import AddAppModal from './components/AddAppModal.vue'
import AddProModal from './components/AddProModal.vue'
import UpdateProModal from './components/UpdateProModal.vue'
import UpgradeProModal from './components/UpgradeProModal.vue'
import DeleteAppModal from './components/DeleteAppModal.vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'

onMounted(async () => {
  init()
})

const addAppModal = ref()
const addProModal = ref()
const updateProModal = ref()
const upgradeProModal = ref()
const deleteAppModal = ref()

const router = useRouter()
const store = useStore()
const data = reactive({
  activeKey: '',
  applications: [],
  currentApp: null,
  search: {
    groupId: '',
    artifactId: ''
  }
})
const selection = reactive({
  current: '1.0.0',
  options: [
    { label: '1.0.0', value: '1.0.0', key: '1.0.0' },
    { label: '2.0.0', value: '2.0.0', key: '2.0.0' },
    { label: '3.0.0', value: '3.0.0', key: '3.0.0' }
  ]
})
const table = reactive({
  datasource: [],
  columns: [
    { title: '项目名称', dataIndex: 'name', key: 'name', width: 120 },
    { title: '当前版本', dataIndex: 'version', key: 'version', width: 120 },
    { title: '语言', dataIndex: 'language', key: 'language', width: 120 },
    { title: '构建工具', dataIndex: 'builder', key: 'builder', width: 120 },
    { title: '扫描对象', dataIndex: 'scanner', key: 'scanner', width: 150 },
    { title: '最近一次扫描时间', dataIndex: 'time', key: 'time', width: 210 },
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

const init = async () => {
  await getApplicationList()
  data.currentApp = data.applications.length > 0 ? data.applications[0] : null
  store.commit('SET_CURRENT_APP', data.currentApp)
  if (data.currentApp) {
    data.activeKey = data.currentApp.name
    await getApplicationVersions(data.currentApp.groupId, data.currentApp.artifactId)
    await getApplicationInfo(data.currentApp.groupId, data.currentApp.artifactId, selection.current)
  }
}

const showApplicationInfo = async (name) => {
  data.currentApp = data.applications.find((item) => item.name === name)
  store.commit('SET_CURRENT_APP', data.currentApp)
  if (data.currentApp) {
    await getApplicationVersions(data.currentApp.groupId, data.currentApp.artifactId)
    await getApplicationInfo(data.currentApp.groupId, data.currentApp.artifactId, selection.current)
  }
}

const changeVersion = (value) => {
  getApplicationInfo(data.currentApp.groupId, data.currentApp.artifactId, value)
}

const getApplicationList = async (page = 1, size = 5) => {
  await store
    .dispatch('getAppList', { number: page, size })
    .then((res) => {
      data.applications = res.content
    })
    .catch((err) => {
      console.error(err)
    })
}

const getApplicationVersions = async (groupId, artifactId) => {
  await store
    .dispatch('getAppVersions', { groupId, artifactId })
    .then((res) => {
      selection.options = res.options
      selection.current = res.current
    })
    .catch((err) => {
      console.error(err)
    })
}

const getApplicationInfo = async (groupId, artifactId, version, number = 1, size = 5) => {
  await store
    .dispatch('getAppInfo', { groupId, artifactId, version })
    .then((res) => {
      table.datasource = res.data
    })
    .catch((err) => {
      console.error(err)
    })
}

const addApplication = () => {
  addAppModal.value.open()
}

const upgradeApp = () => {
  addAppModal.value.open(
    data.currentApp.groupId,
    data.currentApp.artifactId,
    data.currentApp.version,
    data.currentApp.name
  )
}

const addProject = () => {
  addProModal.value.open()
}

const updateProject = (project, record) => {
  updateProModal.value.open(project, record)
}

const upgradeProject = (record) => {
  upgradeProModal.value.open(record)
}

const retry = (record) => {
  record.popconfirm = false
  updateProject()
}

const deleteProject = (record) => {
  const params = {
    appGroupId: data.currentApp.groupId,
    appArtifactId: data.currentApp.artifactId,
    appVersion: data.currentApp.version,
    groupId: record.groupId,
    artifactId: record.artifactId,
    version: record.version
  }
  AppDeleteProject(params)
    .then((res) => {
      // console.log('AppDeleteProject', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      message.success('删除项目成功')
      showApplicationInfo(data.currentApp.name)
    })
    .catch((e) => {
      message.error(e)
    })
  record.popconfirm = false
}

const deleteVersion = () => {
  deleteAppModal.value.open(false)
}

const deleteApplication = () => {
  deleteAppModal.value.open(true)
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

// const subApplication = () => {
//   router.push({
//     path: '/home/subApplication'
//   })
// }
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
  background: transparent;
}
.collapse_header {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: bold;
}
.action_icon {
  margin-right: 10px;
}
.pagination {
  display: flex;
  justify-content: right;
}
</style>
<style scoped src="@/atdv/collapse.css"></style>
<style scoped src="@/atdv/pagination.css"></style>
<style scoped src="@/atdv/input.css"></style>
<style scoped src="@/atdv/input-search.css"></style>
<style scoped src="@/atdv/select.css"></style>
<style scoped src="@/atdv/primary-btn.css"></style>
<style scoped src="@/atdv/delete-btn.css"></style>
<style scoped src="@/atdv/row-selection.css"></style>
