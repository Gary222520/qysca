<template>
  <div>
    <div class="title">项目管理</div>
    <a-card style="height: 100%">
      <div class="content">
        <a-button @click="addProject"><PlusOutlined />新建项目</a-button>
        <a-collapse class="collapse" v-model:activeKey="data.activeKey" :bordered="false" accordion>
          <a-collapse-panel v-for="(project, index) in data.projects" class="collapse_panel" :key="index">
            <template #header>
              <div class="collapse_header">
                <div style="margin-right: 10px; font-size: 18px">{{ project.name }}</div>
                <a-button style="margin-left: 10px; display: flex; align-items: center">
                  <RocketOutlined :style="{ fontSize: '18px', color: '#fff' }" />版本升级
                </a-button>
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
                        <SyncOutlined :style="{ fontSize: '18px', color: '#6f005f' }" />
                      </a-tooltip>
                    </div>
                    <div class="action_icon">
                      <a-tooltip>
                        <template #title>删除</template>
                        <DeleteOutlined :style="{ fontSize: '18px', color: '#ff4d4f' }" />
                      </a-tooltip>
                    </div>
                  </div>
                </template>
              </template>
            </a-table>
          </a-collapse-panel>
        </a-collapse>
      </div>
    </a-card>
    <AddModal ref="addModal"></AddModal>
  </div>
</template>

<script>
import {
  PlusOutlined,
  FileTextOutlined,
  SyncOutlined,
  DeleteOutlined,
  UpSquareOutlined,
  RocketOutlined
} from '@ant-design/icons-vue'
import { reactive, ref } from 'vue'
import AddModal from './components/AddModal.vue'
import { useRouter } from 'vue-router'
export default {
  components: {
    PlusOutlined,
    FileTextOutlined,
    SyncOutlined,
    DeleteOutlined,
    UpSquareOutlined,
    RocketOutlined,
    AddModal
  },
  setup() {
    const addModal = ref()
    const router = useRouter()
    const data = reactive({
      columns: [
        { title: '版本', dataIndex: 'version', key: 'version', width: 120 },
        { title: '语言', dataIndex: 'language', key: 'language', width: 120 },
        { title: '构建工具', dataIndex: 'build', key: 'build', width: 120 },
        { title: '扫描对象', dataIndex: 'target', key: 'target', width: 120 },
        { title: '最近一次扫描时间', dataIndex: 'time', key: 'time', width: 210 },
        { title: '备注', dataIndex: 'comment', key: 'comment' },
        { title: '操作', dataIndex: 'action', key: 'action', width: 150 }
      ],
      projects: [
        {
          name: '项目1',
          data: [
            { version: '2.0.1', language: 'java', target: 'pom.xml', build: 'maven', time: 'xxx', comment: 'xxxxx' },
            { version: '2.0.0', language: 'java', target: 'pom.xml', build: 'maven', time: 'xxx', comment: 'xxxxx' },
            { version: '1.0.0', language: 'java', target: 'pom.xml', build: 'maven', time: 'xxx', comment: 'xxxxx' }
          ]
        },
        {
          name: '项目2',
          data: [
            { version: '2.0.0', language: 'python', target: 'zip', build: 'pip', time: 'xxx', comment: 'xxxxx' },
            { version: '1.0.0', language: 'python', target: 'zip', build: 'pip', time: 'xxx', comment: 'xxxxx' }
          ]
        }
      ],
      activeKey: '0',
      detail: false
    })
    const addProject = () => {
      addModal.value.open()
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
    return { addModal, data, addProject, showDetail }
  }
}
</script>

<style lang="less" scoped>
.title {
  font-weight: bold;
  font-size: 24px;
  margin-bottom: 15px;
}
:deep(.ant-btn) {
  border: 0;
  background-color: #6f005f;
  color: #fff;
}
:deep(.ant-btn):hover {
  opacity: 0.8;
  color: #fff;
}
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
  font-weight: bold;
}
.action_icon {
  margin-right: 10px;
}
:deep(.ant-pagination .ant-pagination-item-active) {
  border-color: #6f005f;
  color: #6f005f;
}
:deep(.ant-pagination .ant-pagination-item-active a) {
  color: #6f005f;
}
</style>
