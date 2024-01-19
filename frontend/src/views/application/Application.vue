<template>
  <div class="main">
    <div class="title">项目管理</div>
    <a-card class="content_card">
      <div class="operations">
        <a-button type="primary" @click="addProject"><PlusOutlined />新建项目</a-button>
        <a-input-search
          v-model:value="data.search.name"
          placeholder="请输入项目名称"
          style="width: 250px"
          @change="(e) => getProjectList()"></a-input-search>
      </div>
      <a-collapse
        class="collapse"
        v-model:activeKey="data.activeKey"
        :bordered="false"
        accordion
        @change="showProjectInfo">
        <a-collapse-panel v-for="project in data.projects" class="collapse_panel" :key="project.id">
          <template #header>
            <div class="collapse_header">
              <div style="display: flex; align-items: center">
                <div style="margin-right: 10px; font-size: 18px">{{ project.name }}</div>
                <a-tooltip>
                  <template #title>刷新</template>
                  <RedoOutlined :style="{ fontSize: '18px', color: '#6f005f' }" @click.stop="getProjectInfo(project)" />
                </a-tooltip>
              </div>
              <div style="display: flex; align-items: center">
                <a-button
                  type="primary"
                  style="margin-right: 10px; display: flex; align-items: center"
                  @click.stop="upgrade(project)">
                  <RocketOutlined :style="{ fontSize: '18px', color: '#fff' }" />版本升级
                </a-button>
                <a-button
                  v-if="!project.compare"
                  type="primary"
                  style="margin-right: 10px"
                  @click.stop="compare(project, true)">
                  <SwapOutlined :style="{ fontSize: '18px', color: '#fff' }" />版本对比
                </a-button>
                <a-button
                  v-if="project.compare"
                  type="primary"
                  danger
                  style="margin-right: 10px"
                  @click.stop="compare(project, false)">
                  <CloseOutlined :style="{ fontSize: '16px', color: '#fff' }" />取消
                </a-button>
                <a-button type="primary" danger @click.stop="deleteProject(project)" :disabled="project.analysing">
                  <WarningOutlined />删除
                </a-button>
              </div>
            </div>
          </template>
          <a-table
            :data-source="project.content"
            :columns="data.columns"
            bordered
            :pagination="project.pagination"
            :row-selection="data.rowSelection">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'action'">
                <div style="display: flex" v-if="record.state === 'SUCCESS'">
                  <div class="action_icon">
                    <a-tooltip>
                      <template #title>详情</template>
                      <FileTextOutlined :style="{ fontSize: '18px', color: '#6f005f' }" @click="showDetail(record)" />
                    </a-tooltip>
                  </div>
                  <div class="action_icon">
                    <a-tooltip>
                      <template #title>更新</template>
                      <SyncOutlined
                        :style="{ fontSize: '18px', color: '#6f005f' }"
                        @click="changeVersion(project, record)" />
                    </a-tooltip>
                  </div>
                  <div class="action_icon">
                    <a-tooltip>
                      <template #title>删除</template>
                      <a-popconfirm v-model:open="record.popconfirm" title="确定删除这个版本吗？">
                        <template #cancelButton>
                          <a-button class="cancel_btn" size="small" @click="record.popconfirm = false">取消</a-button>
                        </template>
                        <template #okButton>
                          <a-button danger type="primary" size="small" @click="deleteVersion(project, record)">
                            删除
                          </a-button>
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
                      <a-button class="cancel_btn" size="small" @click="retry(project, record)">重试</a-button>
                    </template>
                    <template #okButton>
                      <a-button danger type="primary" size="small" @click="deleteVersion(project, record)">
                        删除
                      </a-button>
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
      <div class="pagination">
        <a-pagination
          v-model:current="pagination.current"
          :total="pagination.total"
          v-model:pageSize="pagination.pageSize"
          show-less-items
          :showSizeChanger="false"
          @change="getProjectList" />
      </div>
    </a-card>
    <AddModal ref="addModal" @success="getProjectList"></AddModal>
    <ChangeModal ref="changeModal" @success="getProjectInfo"></ChangeModal>
    <UpgradeModal ref="upgradeModal" @success="getProjectInfo"></UpgradeModal>
    <DeleteModal ref="deleteModal" @success="getProjectList"></DeleteModal>
  </div>
</template>

<script setup>
import {
  PlusOutlined,
  FileTextOutlined,
  SyncOutlined,
  DeleteOutlined,
  UpSquareOutlined,
  RocketOutlined,
  WarningOutlined,
  LoadingOutlined,
  SwapOutlined,
  CloseOutlined,
  RedoOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { reactive, ref, onMounted } from 'vue'
import { GetProjectList, GetProjectInfo, DeleteVersion, CheckRunning } from '@/api/frontend'
import AddModal from './components/AddModal.vue'
import ChangeModal from './components/ChangeModal.vue'
import UpgradeModal from './components/UpgradeModal.vue'
import DeleteModal from './components/DeleteModal.vue'
import { useRouter } from 'vue-router'

onMounted(async () => {
  await getProjectList()
  // console.log(data.projects)
})

const addModal = ref()
const changeModal = ref()
const upgradeModal = ref()
const deleteModal = ref()
const router = useRouter()
const data = reactive({
  columns: [
    { title: '版本', dataIndex: 'version', key: 'version', width: 120 },
    { title: '语言', dataIndex: 'language', key: 'language', width: 120 },
    { title: '构建工具', dataIndex: 'builder', key: 'builder', width: 120 },
    { title: '扫描对象', dataIndex: 'scanner', key: 'scanner', width: 150 },
    { title: '最近一次扫描时间', dataIndex: 'time', key: 'time', width: 210 },
    { title: '备注', dataIndex: 'note', key: 'note' },
    { title: '操作', dataIndex: 'action', key: 'action', width: 150 }
  ],
  projects: [],
  activeKey: '',
  detail: false,
  search: {
    name: ''
  },
  rowSelection: null
})
const pagination = reactive({
  current: 1,
  total: 0,
  pageSize: 5
})
const getProjectList = async (page = 1, size = 5) => {
  let projectList = []
  const params = {
    name: data.search.name,
    number: page,
    size
  }
  await GetProjectList(params)
    .then((res) => {
      // console.log('GetProjectList', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      projectList = res.data.content
      pagination.total = res.data.totalElements
    })
    .catch((err) => {
      console.error(err)
    })
  data.projects = []
  projectList?.forEach(({ id, name }) => {
    const project = {
      id,
      name
    }
    data.projects.push(project)
  })
  if (projectList.length > 0) {
    data.activeKey = projectList[0].id
    showProjectInfo(data.activeKey)
  }
}
const showProjectInfo = async (key) => {
  const project = data.projects.find((item) => item.id === key)
  if (project) await getProjectInfo(project)
}
const getProjectInfo = async (project, number = 1, size = 5) => {
  const params = {
    name: project.name,
    number,
    size
  }
  await CheckRunning({ name: project.name })
    .then((res) => {
      // console.log('CheckRunning', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      project.analysing = res.data !== 0
    })
    .catch((err) => {
      console.error(err)
    })
  await GetProjectInfo(params)
    .then((res) => {
      // console.log('GetProjectInfo', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      project.content = res.data.content
      project.content.forEach((item, index) => {
        item.key = index
      })
      project.content?.forEach((version) => {
        if (version.state === 'RUNNING') project.analysing = true
      })
      project.pagination = {
        current: number,
        total: res.data.totalElements,
        pageSize: size,
        onChange: (page, size) => {
          getProjectInfo(project, page, size)
        }
      }
    })
    .catch((err) => {
      console.error(err)
    })
}
const addProject = () => {
  addModal.value.open()
}
const upgrade = async (project) => {
  await showProjectInfo(project.id)
  upgradeModal.value.open(project)
}
const deleteProject = (project) => {
  deleteModal.value.open(project)
}
const showDetail = (record) => {
  router.push({
    path: '/home/appDetail',
    query: {
      name: record.name,
      version: record.version
    }
  })
}
const compare = (project, value) => {
  project.compare = value
  if (value) {
    data.rowSelection = {
      columnTitle: ' ',
      selectedRowKeys: [],
      onChange: (selectedRowKeys, selectedRows) => {
        data.rowSelection.selectedRowKeys = selectedRowKeys
        if (selectedRows.length === 2) {
          router.push({
            path: '/home/compare',
            query: {
              name: project.name,
              currentVersion: selectedRows[0].version,
              compareVersion: selectedRows[1].version
            }
          })
        }
      },
      getCheckboxProps: (record) => {
        return { disabled: record.state !== 'SUCCESS' }
      }
    }
  } else {
    data.rowSelection.selectedRowKeys = []
    data.rowSelection = null
  }
}
const changeVersion = (project, record) => {
  changeModal.value.open(project, record)
}
const retry = (project, record) => {
  record.popconfirm = false
  changeVersion(project, record)
}
const deleteVersion = (project, record) => {
  DeleteVersion({ name: record.name, version: record.version })
    .then((res) => {
      // console.log('DeleteVersion', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      message.success('删除版本成功')
      getProjectInfo(project)
    })
    .catch((e) => {
      message.error(e)
    })
  record.popconfirm = false
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
.pagination {
  display: flex;
  justify-content: right;
}
.cancel_btn:hover {
  border-color: #6f005f;
  color: #6f005f;
}
/* 折叠面板样式 */
.collapse {
  margin-top: 20px;
  background: transparent;
}
.collapse_panel,
:deep(.ant-collapse-borderless > .ant-collapse-item:last-child) {
  margin-bottom: 24px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  background-color: rgba(0, 0, 0, 0.04);
}
:deep(.ant-collapse > .ant-collapse-item > .ant-collapse-header) {
  display: flex;
  align-items: center;
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
</style>
<style scoped src="@/atdv/pagination.css"></style>
<style scoped src="@/atdv/input.css"></style>
<style scoped src="@/atdv/input-search.css"></style>
<style scoped src="@/atdv/primary-btn.css"></style>
<style scoped src="@/atdv/delete-btn.css"></style>
<style scoped src="@/atdv/row-selection.css"></style>
