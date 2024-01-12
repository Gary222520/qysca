<template>
  <div class="main">
    <div class="title">项目管理</div>
    <a-card class="content_card">
      <div class="operations">
        <a-button type="primary" @click="addProject"><PlusOutlined />新建项目</a-button>
        <a-input-search
          v-model:value="data.search.name"
          placeholder="请输入项目名称"
          style="width: 250px"></a-input-search>
      </div>
      <a-collapse class="collapse" v-model:activeKey="data.activeKey" :bordered="false" accordion>
        <a-collapse-panel v-for="(project, index) in data.projects" class="collapse_panel" :key="index">
          <template #header>
            <div class="collapse_header">
              <div style="display: flex; align-items: center">
                <div style="margin-right: 10px; font-size: 18px">{{ project.name }}</div>
                <a-button
                  type="primary"
                  style="margin-left: 10px; display: flex; align-items: center"
                  @click.stop="upgrade(project)">
                  <RocketOutlined :style="{ fontSize: '18px', color: '#fff' }" />版本升级
                </a-button>
              </div>
              <div style="display: flex; align-items: center">
                <a-button type="primary" danger @click.stop="deleteProject(project)">
                  <WarningOutlined />删除
                </a-button>
              </div>
            </div>
          </template>
          <a-table :data-source="project.data" :columns="data.columns" bordered>
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'action'">
                <div style="display: flex">
                  <div class="action_icon">
                    <a-tooltip>
                      <template #title>详情</template>
                      <FileTextOutlined
                        :style="{ fontSize: '18px', color: '#6f005f' }"
                        @click="showDetail(project, record)" />
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
              </template>
            </template>
          </a-table>
        </a-collapse-panel>
      </a-collapse>
      <div class="pagination">
        <a-pagination
          v-model:current="pagination.current"
          :total="pagination.total"
          v-model:pageSize="pagination.pageSize"
          show-less-items
          :showSizeChanger="false" />
      </div>
    </a-card>
    <AddModal ref="addModal"></AddModal>
    <ChangeModal ref="changeModal"></ChangeModal>
    <UpgradeModal ref="upgradeModal"></UpgradeModal>
    <DeleteModal ref="deleteModal"></DeleteModal>
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
  WarningOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { reactive, ref, onMounted } from 'vue'
import { GetProjectList, GetProjectInfo, DeleteVersion } from '@/api/frontend'
import AddModal from './components/AddModal.vue'
import ChangeModal from './components/ChangeModal.vue'
import UpgradeModal from './components/UpgradeModal.vue'
import DeleteModal from './components/DeleteModal.vue'
import { useRouter } from 'vue-router'

onMounted(async () => {
  let projectNames = []
  await GetProjectList()
    .then((res) => {
      console.log('GetProjectList', res)
      projectNames = res.data
    })
    .catch((e) => {
      // message.error(e)
    })
  projectNames.forEach((projectName) => {
    GetProjectInfo({ projectName })
      .then((res) => {
        console.log('GetProjectInfo', res)
      })
      .catch((e) => {
        // message.error(e)
      })
  })
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
    { title: '构建工具', dataIndex: 'tool', key: 'tool', width: 120 },
    { title: '扫描对象', dataIndex: 'target', key: 'target', width: 120 },
    { title: '最近一次扫描时间', dataIndex: 'time', key: 'time', width: 210 },
    { title: '备注', dataIndex: 'comment', key: 'comment' },
    { title: '操作', dataIndex: 'action', key: 'action', width: 150 }
  ],
  projects: [
    {
      name: '项目1',
      data: [
        {
          version: '2.0.1',
          language: 'java',
          target: 'pom.xml',
          tool: 'maven',
          time: 'xxx',
          comment: 'xxxxx',
          popconfirm: false
        },
        {
          version: '2.0.0',
          language: 'java',
          target: 'pom.xml',
          tool: 'maven',
          time: 'xxx',
          comment: 'xxxxx',
          popconfirm: false
        },
        {
          version: '1.0.0',
          language: 'java',
          target: 'pom.xml',
          tool: 'maven',
          time: 'xxx',
          comment: 'xxxxx',
          popconfirm: false
        }
      ]
    },
    {
      name: '项目2',
      data: [
        {
          version: '2.0.0',
          language: 'python',
          target: 'zip',
          tool: 'pip',
          time: 'xxx',
          comment: 'xxxxx',
          popconfirm: false
        },
        {
          version: '1.0.0',
          language: 'python',
          target: 'zip',
          tool: 'pip',
          time: 'xxx',
          comment: 'xxxxx',
          popconfirm: false
        }
      ]
    }
  ],
  activeKey: '0',
  detail: false,
  search: {
    name: ''
  }
})
const pagination = reactive({
  current: 1,
  total: 50,
  pageSize: 5
})
const addProject = () => {
  addModal.value.open()
}
const upgrade = (project) => {
  upgradeModal.value.open(project)
}
const deleteProject = (project) => {
  deleteModal.value.open(project)
}
const showDetail = (project, record) => {
  router.push({
    path: '/home/appDetail',
    query: {
      name: project.name,
      version: record.version
    }
  })
}
const changeVersion = (project, record) => {
  changeModal.value.open(project, record)
}
const deleteVersion = (project, record) => {
  DeleteVersion({ projectName: project.name, version: record.version })
    .then((res) => {
      console.log('DeleteVersion', res)
    })
    .catch((e) => {
      // message.error(e)
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
