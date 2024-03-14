<template>
  <a-drawer v-model:open="data.open" width="800px" :closable="false" placement="right">
    <template #title>
      <div style="font-size: 24px">{{ data.isProject ? '应用详情' : '组件详情' }}</div>
    </template>
    <div>
      <span style="font-size: 20px; font-weight: bold; margin-right: 20px">{{ data.detail?.name }}</span>
      <a-tooltip>
        <template #title>刷新</template>
        <RedoOutlined :style="{ fontSize: '18px', color: '#6f005f' }" @click.stop="refresh()" />
      </a-tooltip>
      <a-tag v-if="data.isProject" color="purple" style="margin-left: 20px">
        <template #icon><FolderOutlined /></template>应用
      </a-tag>
    </div>
    <div class="relative">
      <div class="drawer_title" style="margin-bottom: 20px">基本信息</div>
    </div>
    <a-descriptions>
      <a-descriptions-item label="组织ID">{{ data.detail?.groupId }}</a-descriptions-item>
      <a-descriptions-item label="工件ID">{{ data.detail?.artifactId }}</a-descriptions-item>
      <a-descriptions-item label="应用类型">{{ data.detail?.type }}</a-descriptions-item>
      <a-descriptions-item label="应用描述" span="3">{{ data.detail?.description }}</a-descriptions-item>
    </a-descriptions>

    <div class="relative">
      <div class="drawer_title">应用扫描信息</div>
    </div>
    <a-descriptions>
      <a-descriptions-item label="语言">{{ data.detail?.language }}</a-descriptions-item>
      <a-descriptions-item label="构建工具">{{ data.detail?.builder }}</a-descriptions-item>
      <a-descriptions-item label="扫描对象">{{ data.detail?.scanner }}</a-descriptions-item>
      <a-descriptions-item label="扫描时间">{{ data.detail?.time }}</a-descriptions-item>
    </a-descriptions>

    <div class="relative">
      <div class="drawer_title">应用状态</div>
    </div>
    <a-descriptions>
      <a-descriptions-item>
        <template #label>
          <div v-if="data.detail.state === 'CREATED'" class="label">暂未扫描</div>
          <div v-if="data.detail.state === 'RUNNING'" class="label">扫描中</div>
          <div v-if="data.detail.state === 'SUCCESS'" class="label">扫描成功</div>
          <div v-if="data.detail.state === 'FAILED'" class="label">扫描失败</div>
        </template>
        <div style="display: flex" v-if="data.detail.state === 'CREATED'">
          <a-button class="btn" type="primary" @click="addDependency()"> <FileAddOutlined />添加依赖信息</a-button>
        </div>
        <div style="display: flex" v-if="data.detail.state === 'SUCCESS'">
          <a-button class="btn" type="primary" @click="showDetail()"> <FileTextOutlined />查看依赖信息</a-button>
          <a-button class="btn" type="primary" @click="updateDependency()"> <SyncOutlined />更新依赖信息</a-button>
        </div>
        <div style="display: flex; align-items: center; height: 32px" v-if="data.detail.state === 'RUNNING'">
          <LoadingOutlined :style="{ fontSize: '18px', color: '#6f005f' }" />
          <div style="margin-left: 10px">扫描分析中...</div>
        </div>
        <div style="display: flex; align-items: center; height: 32px" v-if="data.detail.state === 'FAILED'">
          <a-popconfirm v-model:open="data.popconfirm" title="扫描出错，请重试">
            <template #cancelButton>
              <a-button class="cancel_btn" size="small" @click="retry()">重试</a-button>
            </template>
            <template #okButton></template>
            <ExclamationCircleOutlined :style="{ fontSize: '18px', color: '#ff4d4f' }" />
            <span style="margin-left: 10px; color: #ff4d4f; cursor: pointer">扫描失败</span>
          </a-popconfirm>
        </div>
      </a-descriptions-item>
    </a-descriptions>
    <AddDepModal ref="addDepModal" @success="refresh()"></AddDepModal>
  </a-drawer>
</template>

<script setup>
import {
  RedoOutlined,
  FolderOutlined,
  FileTextOutlined,
  FileAddOutlined,
  SyncOutlined,
  LoadingOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue'
import { reactive, ref, defineExpose, defineEmits } from 'vue'
import { useRouter } from 'vue-router'
import { GetComponentInfo } from '@/api/frontend'
import { message } from 'ant-design-vue'
import AddDepModal from './AddDepModal.vue'

const router = useRouter()
const addDepModal = ref()
const emit = defineEmits(['refresh'])

const data = reactive({
  open: false,
  detail: {},
  isProject: true,
  popconfirm: false,
  dependency: false,
  component: {},
  licenseColumns: [
    { title: '许可证名称', dataIndex: 'licenseName', key: 'licenseName' },
    { title: '许可证地址', dataIndex: 'licenseUrl', key: 'licenseUrl' }
  ],
  developerColumns: [
    { title: '开发者ID', dataIndex: 'developerId', key: 'developerId' },
    { title: '开发者姓名', dataIndex: 'developerName', key: 'developerName' },
    { title: '开发者邮箱', dataIndex: 'developerEmail', key: 'developerEmail' }
  ]
})
const open = (detail, isProject) => {
  data.open = true
  data.detail = detail
  data.isProject = isProject
}
const close = () => {
  data.open = false
}

const addDependency = () => {
  addDepModal.value.open(data.detail, true)
}

const updateDependency = () => {
  addDepModal.value.open(data.detail, false)
}

const retry = () => {
  data.popconfirm = false
  updateDependency()
}

const refresh = () => {
  emit('refresh')
}

const showDetail = () => {
  router.push({
    path: '/home/appDetail',
    query: {
      groupId: data.detail.groupId,
      artifactId: data.detail.artifactId,
      version: data.detail.version
    }
  })
}

defineExpose({ open })
</script>

<style scoped>
.drawer_title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  margin-top: 20px;
  margin-bottom: 10px;
  padding-left: 10px;
}
.drawer_title::before {
  position: absolute;
  display: block;
  content: '';
  width: 3px;
  height: 18px;
  left: 0;
  background-color: #6f005f;
}
.relative {
  position: relative;
  display: flex;
  align-items: center;
}
.label {
  height: 32px;
  font-size: 16px;
  color: #6f005f;
  display: flex;
  align-items: center;
}
.btn {
  margin-left: 10px;
}
</style>
<style scoped src="@/atdv/pagination.css"></style>
<style scoped src="@/atdv/primary-btn.css"></style>
