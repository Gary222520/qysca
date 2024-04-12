<template>
  <a-drawer v-model:open="data.open" width="800px" :closable="false" placement="right">
    <template #title>
      <div style="font-size: 20px">{{ data.isProject ? '应用详情' : '组件详情' }}</div>
    </template>
    <a-spin :spinning="data.spinning" tip="应用信息加载中，请稍等...">
      <div>
        <span style="font-size: 18px; font-weight: bold; margin-right: 20px">{{ data.detail?.name }}</span>
        <a-tooltip v-if="data.isProject">
          <template #title>刷新</template>
          <RedoOutlined :style="{ fontSize: '18px', color: '#6f005f' }" @click.stop="refresh()" />
        </a-tooltip>
        <a-button v-if="!data.isProject" class="btn" type="primary" @click="showDetail()">
          <FileTextOutlined />查看依赖信息
        </a-button>
        <a-button type="primary" style="margin-left: 30px" @click="exportSBOM">导出SBOM</a-button>
      </div>
      <div class="relative">
        <div class="drawer_title">基本信息</div>
      </div>
      <a-descriptions>
        <a-descriptions-item label="应用类型">{{ data.detail?.type }}</a-descriptions-item>
        <a-descriptions-item label="应用描述">{{ data.detail?.description }}</a-descriptions-item>
      </a-descriptions>

      <div class="relative">
        <div class="drawer_title">扫描信息</div>
      </div>
      <a-descriptions>
        <a-descriptions-item label="语言">
          <a-tag v-for="(item, index) in data.detail?.language" :key="index">{{ item }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="构建工具">{{ data.detail?.builder }}</a-descriptions-item>
        <a-descriptions-item label="扫描对象">{{ data.detail?.scanner }}</a-descriptions-item>
        <a-descriptions-item label="扫描时间">{{ data.detail?.time }}</a-descriptions-item>
      </a-descriptions>

      <div class="relative" v-if="data.isProject">
        <div class="drawer_title">成员信息</div>
      </div>
      <div style="margin-bottom: 10px" v-if="data.isProject">
        <a-button type="primary" @click="addAppMember()"><PlusOutlined />添加成员</a-button>
      </div>
      <div style="margin-bottom: 20px" v-if="data.isProject">
        <a-table :data-source="table.datasource" :columns="table.columns" bordered :pagination="false">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'action'">
              <a-tooltip>
                <template #title>删除</template>
                <a-popconfirm v-model:open="record.popconfirm" title="确定删除该成员吗？">
                  <template #cancelButton>
                    <a-button class="cancel_btn" size="small" @click="record.popconfirm = false">取消</a-button>
                  </template>
                  <template #okButton>
                    <a-button danger type="primary" size="small" @click="deleteMember(record)">删除</a-button>
                  </template>
                  <DeleteOutlined :style="{ fontSize: '18px', color: '#ff4d4f' }" />
                </a-popconfirm>
              </a-tooltip>
            </template>
          </template>
          <template #emptyText>暂无成员</template>
        </a-table>
      </div>

      <div class="relative" v-if="data.isProject && !hasChildren">
        <div class="drawer_title">
          应用状态
          <a-tag v-if="data.detail.state === 'CREATED'" color="processing" :bordered="false" class="label">
            暂未扫描
          </a-tag>
          <a-tag v-if="data.detail.state === 'RUNNING'" color="warning" :bordered="false" class="label">扫描中</a-tag>
          <a-tag v-if="data.detail.state === 'SUCCESS'" color="success" :bordered="false" class="label">扫描成功</a-tag>
          <a-tag v-if="data.detail.state === 'FAILED'" color="error" :bordered="false" class="label">扫描失败</a-tag>
        </div>
      </div>
      <div style="margin-top: 10px" v-if="data.isProject && !hasChildren">
        <div style="display: flex" v-if="data.detail.state === 'CREATED'">
          <a-button class="btn" type="primary" @click="addDependency()"> <FileAddOutlined />上传扫描文件</a-button>
        </div>
        <div style="display: flex" v-if="data.detail.state === 'SUCCESS'">
          <a-tooltip placement="bottom">
            <template #title>组件、漏洞、许可证...</template>
            <a-button class="btn" type="primary" @click="showDetail()"><FileTextOutlined />查看扫描结果</a-button>
          </a-tooltip>
          <a-button class="btn" type="primary" @click="updateDependency()"><SyncOutlined />重新扫描</a-button>
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
      </div>
      <AddDepModal ref="addDepModal" @success="refresh()"></AddDepModal>
      <AddMember ref="addMember" @success="getAppMember()"></AddMember>
    </a-spin>
  </a-drawer>
</template>

<script setup>
import {
  PlusOutlined,
  DeleteOutlined,
  RedoOutlined,
  FolderOutlined,
  FileTextOutlined,
  FileAddOutlined,
  SyncOutlined,
  LoadingOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue'
import { reactive, ref, defineExpose, defineEmits, computed } from 'vue'
import { useRouter } from 'vue-router'
import { GetComponentInfo, GetVersionInfo, DeleteAppMember, ExportSBOM } from '@/api/frontend'
import { message } from 'ant-design-vue'
import AddDepModal from './AddDepModal.vue'
import AddMember from './AddMember.vue'
import { arrToString } from '@/utils/util.js'

const router = useRouter()
const addDepModal = ref()
const addMember = ref()
const emit = defineEmits(['refresh'])

const data = reactive({
  spinning: false,
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
const hasChildren = computed(() => {
  if (data.detail.childApplication.length > 0) return true
  if (data.detail.childComponent?.java) return true
  if (data.detail.childComponent?.python) return true
  if (data.detail.childComponent?.go) return true
  if (data.detail.childComponent?.javaScript) return true
  return false
})
const table = reactive({
  datasource: [],
  columns: [
    { title: '成员编号', dataIndex: 'uid', key: 'uid', width: 150 },
    { title: '成员名称', dataIndex: 'name', key: 'name', width: 200 },
    { title: '角色', dataIndex: 'role', key: 'role', width: 150 },
    { title: '操作', dataIndex: 'action', key: 'action', width: 150 }
  ]
})

const open = (detail, isProject) => {
  data.open = true
  data.detail = detail
  data.isProject = isProject
  getAppMember()
}
const close = () => {
  data.open = false
}

const getAppMember = async () => {
  await GetVersionInfo({ name: data.detail.name, version: data.detail.version })
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      // console.log('GetVersionInfo', res)
      table.datasource = res.data.users
    })
    .catch((err) => {
      console.error(err)
    })
}

const addAppMember = () => {
  addMember.value.open(data.detail)
}

const deleteMember = (record) => {
  const params = {
    name: data.detail.name,
    version: data.detail.version,
    uid: record.uid,
    role: record.role
  }
  DeleteAppMember(params)
    .then((res) => {
      // console.log('DeleteAppMember', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      message.success('删除应用成员成功')
      getAppMember()
    })
    .catch((e) => {
      message.error(e)
    })
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

const exportSBOM = () => {
  ExportSBOM({ name: data.detail.name, version: data.detail.version })
    .then((res) => {
      // console.log('ExportSBOM', res)
      const reader = new FileReader()
      reader.readAsText(res.data, 'utf-8')
      reader.onload = () => {
        try {
          const result = JSON.parse(reader.result)
          if (result.code && result.code !== 200) {
            message.error(result.message)
          } else {
            downloadSBOM(res.data, `${data.detail.name}-${data.detail.version}-SBOM`)
            message.success('导出成功')
          }
        } catch (e) {
          downloadSBOM(res.data, `${data.detail.name}-${data.detail.version}-SBOM`)
          message.success('导出成功')
        }
      }
    })
    .catch((e) => {
      message.error(e)
    })
}
const downloadSBOM = (data, fileName) => {
  const blob = new Blob([data])
  const a = document.createElement('a')
  a.style.display = 'none'
  a.href = URL.createObjectURL(blob)
  if (hasChildren.value) a.download = `${fileName}.zip`
  else a.download = `${fileName}.json`
  a.click()
  a.remove()
}

const showDetail = () => {
  router.push({
    path: '/home/appDetail',
    query: {
      // groupId: data.detail.groupId,
      // artifactId: data.detail.artifactId,
      name: data.detail.name,
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
  font-size: 16px;
  font-weight: bold;
  margin: 15px 0;
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
  font-size: 16px;
  font-weight: normal;
  margin-left: 10px;
}
.btn {
  margin-left: 10px;
}
.cancel_btn:hover {
  border-color: #6f005f;
  color: #6f005f;
}
:deep(.ant-descriptions .ant-descriptions-item-label) {
  font-weight: bold;
}
</style>
<style scoped src="@/atdv/pagination.css"></style>
<style scoped src="@/atdv/primary-btn.css"></style>
<style scoped src="@/atdv/description.css"></style>
<style scoped src="@/atdv/spin.css"></style>
