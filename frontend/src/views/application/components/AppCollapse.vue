<template>
  <div>
    <a-collapse
      v-model:activeKey="data.activeKey"
      collapsible="icon"
      accordion
      @change="(index) => findSubProject(index)">
      <a-collapse-panel v-for="(app, index) in props.appList" :key="index">
        <template #header>
          <div class="collapse_header">
            <div style="display: flex; align-items: center">
              <div style="margin-right: 20px; font-size: 18px; font-weight: bold">{{ app.name }}</div>
              <a-tooltip v-if="app.operation">
                <template #title>刷新</template>
                <RedoOutlined :style="{ fontSize: '18px', color: '#6f005f' }" @click.stop="refreshParent()" />
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
                    @change="() => findSubProject(index, app.selection.current)">
                  </a-select>
                </a-input-group>
              </div>
              <div v-if="app.operation" style="margin-left: 20px">
                <a-tag v-if="app.lock" color="warning">
                  <template #icon><LockOutlined /></template>已上锁
                </a-tag>
                <a-tag v-else color="green">
                  <template #icon><UnlockOutlined /></template>未上锁
                </a-tag>
                <a-tag v-if="app.release" color="success">
                  <template #icon><EyeOutlined /></template>已发布
                </a-tag>
                <a-tag v-else color="processing">
                  <template #icon><EyeInvisibleOutlined /></template>未发布
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
          @refresh="refreshChildren()"></AppCollapse>
      </a-collapse-panel>
    </a-collapse>
    <AddAppModal ref="addAppModal" @root="getProjectList()" @notroot="refreshParent()"></AddAppModal>
    <AddDepModal ref="addDepModal" @success="refreshParent()"></AddDepModal>
    <UpgradeAppModal ref="upgradeAppModal" @success="refreshParent()"></UpgradeAppModal>
    <DeleteAppModal ref="deleteAppModal" @success="refreshParent()"></DeleteAppModal>
  </div>
</template>

<script setup>
import {
  PlusOutlined,
  RocketOutlined,
  RedoOutlined,
  FileTextOutlined,
  FileAddOutlined,
  SyncOutlined,
  WarningOutlined,
  LoadingOutlined,
  ExclamationCircleOutlined,
  LockOutlined,
  UnlockOutlined,
  EyeOutlined,
  EyeInvisibleOutlined
} from '@ant-design/icons-vue'
import { GetSubProject, GetVersionList } from '@/api/frontend'
import { message } from 'ant-design-vue'
import { reactive, ref, defineEmits, defineProps, defineExpose } from 'vue'
import AppCollapse from '@/views/application/components/AppCollapse.vue'
import AddAppModal from './AddAppModal.vue'
import AddDepModal from './AddDepModal.vue'
import UpgradeAppModal from './UpgradeAppModal.vue'
import DeleteAppModal from './DeleteAppModal.vue'

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
    type: Array,
    default: () => []
  }
})

const appCollapse = ref()
const addAppModal = ref()
const addDepModal = ref()
const upgradeAppModal = ref()
const deleteAppModal = ref()

const data = reactive({
  activeKey: '',
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

const findSubProject = async (index, version) => {
  if (!index && index !== 0) return
  const app = props.appList[index]
  app.operation = true
  app.selection = {}
  await getVersionList(app, app.groupId, app.artifactId)
  if (app.versions.includes(version)) app.selection.current = version
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

const refreshParent = () => {
  emit('refresh')
}

const refresh = async () => {
  if (data.currentApp.index >= 0) {
    await findSubProject(data.currentApp.index, data.currentApp.version)
    appCollapse.value[data.currentApp.index].close()
  }
}

const refreshChildren = async () => {
  if (data.currentApp.index >= 0) {
    await findSubProject(data.currentApp.index, data.currentApp.version)
    appCollapse.value[data.currentApp.index].refresh()
  }
}

const addProject = (app, index) => {
  data.currentApp.index = index
  data.currentApp.version = app.selection.current
  addAppModal.value.open(app.id, null)
}

const upgradeProject = (app, index) => {
  data.currentApp.index = index
  data.currentApp.version = app.selection.current
  upgradeAppModal.value.open(app, props.parentApp)
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
  deleteAppModal.value.open(app, props.parentApp)
}

const close = () => {
  data.activeKey = ''
}

defineExpose({ refresh, close })
</script>

<style scoped>
.collapse_header {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.action_icon {
  margin-right: 10px;
}
:deep(.ant-collapse) {
  border-radius: 0;
}
:deep(.ant-collapse-header::before) {
  position: absolute;
  top: 50%;
  left: -17px;
  width: 17px;
  height: 1px;
  background-color: #d9d9d9;
  content: '';
}
:deep(.ant-collapse .ant-collapse-content > .ant-collapse-content-box) {
  padding: 16px 0px 16px 16px;
}
:deep(.ant-collapse > .ant-collapse-item:last-child),
:deep(.ant-collapse > .ant-collapse-item:last-child > .ant-collapse-header),
:deep(.ant-collapse .ant-collapse-item:last-child > .ant-collapse-content) {
  border-radius: 0;
}
</style>
