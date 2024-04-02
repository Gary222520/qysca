<template>
  <div>
    <div class="operation">
      <a-popconfirm v-model:open="data.popconfirm" placement="right">
        <template #title>
          <div style="font-size: 16px">添加许可证</div>
        </template>
        <!-- <template #icon></template> -->
        <template #description>
          <a-input class="input" v-model:value="data.input" placeholder="输入许可证名称以添加..."></a-input>
        </template>
        <template #cancelButton>
          <a-button class="cancel_btn" @click="data.popconfirm = false">取消</a-button>
        </template>
        <template #okButton>
          <a-button class="btn" @click="addLicense()">添加</a-button>
        </template>
        <a-button type="primary" @click="data.popconfirm = true"><PlusOutlined />添加许可证</a-button>
      </a-popconfirm>
    </div>
    <a-spin :spinning="data.spinning" tip="许可证信息加载中，请稍等...">
      <a-table :data-source="data.datasource" :columns="data.columns" bordered :pagination="pagination">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <div class="column_name" @click="showInfo(record)">{{ record.name }}</div>
          </template>
          <template v-if="column.key === 'riskLevel'">
            <a-tag v-if="record.riskLevel === 'high'" color="error">高危</a-tag>
            <a-tag v-if="record.riskLevel === 'medium'" color="warning">中危</a-tag>
            <a-tag v-if="record.riskLevel === 'low'" color="processing">低危</a-tag>
            <!-- <span class="risk-tag high-risk" v-if="record.riskLevel === 'high'">高危</span>
            <span class="risk-tag medium-risk" v-if="record.riskLevel === 'medium'">中危</span>
            <span class="risk-tag low-risk" v-if="record.riskLevel === 'low'">低危</span> -->
          </template>
          <template v-if="column.key === 'isOsiApproved'">
            <CheckOutlined v-if="record.isOsiApproved" :style="{ color: '#52c41a' }" />
            <CloseOutlined v-else :style="{ color: '#ff4d4f' }" />
          </template>
          <template v-if="column.key === 'isFsfApproved'">
            <CheckOutlined v-if="record.isFsfApproved" :style="{ color: '#52c41a' }" />
            <CloseOutlined v-else :style="{ color: '#ff4d4f' }" />
          </template>
          <template v-if="column.key === 'isSpdxApproved'">
            <CheckOutlined v-if="record.isSpdxApproved" :style="{ color: '#52c41a' }" />
            <CloseOutlined v-else :style="{ color: '#ff4d4f' }" />
          </template>
          <template v-if="column.key === 'gplCompatibility'">
            <CheckOutlined v-if="record.gplCompatibility" :style="{ color: '#52c41a' }" />
            <CloseOutlined v-else :style="{ color: '#ff4d4f' }" />
          </template>
          <template v-if="column.key === 'type'">
            <div v-if="record.type === 'opensource'">开源</div>
            <div v-if="record.type === 'business'">商用</div>
            <div v-if="record.type === 'internal'">内部使用</div>
          </template>
          <template v-if="column.key === 'action'">
            <a-tooltip>
              <template #title>详情</template>
              <FileTextOutlined
                :style="{ fontSize: '18px', color: '#6f005f', marginRight: '10px' }"
                @click="showInfo(record)" />
            </a-tooltip>
            <a-tooltip>
              <template #title>删除</template>
              <a-popconfirm v-model:open="record.popconfirm" title="确定删除这个许可证吗？">
                <template #cancelButton>
                  <a-button class="cancel_btn" size="small" @click="record.popconfirm = false">取消</a-button>
                </template>
                <template #okButton>
                  <a-button danger type="primary" size="small" @click="deleteLicense(record)">删除</a-button>
                </template>
                <DeleteOutlined :style="{ fontSize: '18px', color: '#ff4d4f' }" />
              </a-popconfirm>
            </a-tooltip>
          </template>
        </template>
        <template #emptyText>暂无数据</template>
      </a-table>
    </a-spin>
    <Drawer ref="drawer"></Drawer>
  </div>
</template>

<script setup>
import { PlusOutlined, CheckOutlined, CloseOutlined, FileTextOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { reactive, ref, defineExpose, defineEmits, defineProps, onMounted } from 'vue'
import { GetLicenseList, AddLicense, DeleteLicense } from '@/api/frontend'
import Drawer from '@/components/LicenseDrawer.vue'
import { message } from 'ant-design-vue'

const drawer = ref()
const emit = defineEmits(['setCount'])

const data = reactive({
  visible: false,
  spinning: false,
  component: {},
  datasource: [],
  columns: [
    { title: '许可证', dataIndex: 'name', key: 'name' },
    { title: '名称', dataIndex: 'fullName', key: 'fullName' },
    { title: '风险等级', dataIndex: 'riskLevel', key: 'riskLevel' },
    { title: 'OSI认证', dataIndex: 'isOsiApproved', key: 'isOsiApproved' },
    { title: 'FSF许可', dataIndex: 'isFsfApproved', key: 'isFsfApproved' },
    { title: 'SPDX认证', dataIndex: 'isSpdxApproved', key: 'isSpdxApproved' },
    { title: 'GPL兼容性', dataIndex: 'gplCompatibility', key: 'gplCompatibility' },
    { title: '操作', dataIndex: 'action', key: 'action', width: 120 }
  ],
  popconfirm: false,
  input: ''
})
const app = reactive({
  name: '',
  version: ''
})
const pagination = reactive({
  current: 1,
  total: 0,
  pageSize: 10,
  showSizeChanger: false,
  onChange: (page, size) => {
    pagination.current = page
    getLicenseList()
  },
  hideOnSinglePage: true
})
const show = (name, version) => {
  app.name = name
  app.version = version
  getLicenseList()
}
const getLicenseList = (name = app.name, version = app.version, page = 1, size = 10) => {
  data.spinning = true
  GetLicenseList({ name, version, page, size })
    .then((res) => {
      // console.log('GetLicenseList', res)
      data.spinning = false
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      data.datasource = res.data.content
      pagination.total = res.data.totalElements
      pagination.current = page
      emit('setCount', { type: 'license', value: pagination.total })
    })
    .catch((err) => {
      data.spinning = false
      console.error(err)
    })
}
const addLicense = () => {
  data.popconfirm = false
  const params = {
    name: app.name,
    version: app.version,
    licenseName: data.input
  }
  AddLicense(params)
    .then((res) => {
      // console.log('AddLicense', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      message.success('添加成功')
      getLicenseList()
    })
    .catch((err) => {
      console.error(err)
    })
}
const deleteLicense = (record) => {
  record.popconfirm = false
  const params = {
    name: app.name,
    version: app.version,
    licenseName: record.name
  }
  DeleteLicense(params)
    .then((res) => {
      // console.log('DeleteLicense', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      message.success('删除成功')
      getLicenseList()
    })
    .catch((err) => {
      console.error(err)
    })
}
const showInfo = (record) => {
  drawer.value.open(record)
}
defineExpose({ show })
</script>

<style scoped>
.operation {
  margin-bottom: 20px;
}
.column_name {
  cursor: pointer;
}
.column_name:hover {
  color: #6f005f;
}
.risk-tag {
  display: inline-block;
  /* height: 20px; */
  /* line-height: 20px; */
  /* padding: 0 5px; */
  /* border-radius: 3px; */
}
.high-risk {
  color: #ff4d4f;
  /* background-color: #ffccc7; */
}
.medium-risk {
  color: #faad14;
  /* background-color: #ffe58f; */
}
.low-risk {
  color: #1677ff;
  /* background-color: #91caff; */
}
.input {
  width: 200px;
}
.input:hover {
  border-color: #6f005f;
}
.input:focus {
  border-color: #6f005f;
  box-shadow: 0 0 0 2px rgba(111, 0, 95, 0.1);
}
.btn {
  border-color: #6f005f;
  background-color: #6f005f;
  color: #ffffff;
}
.btn:hover {
  border-color: #6f005f;
  background-color: #6f005f;
  color: #ffffff;
  opacity: 0.8;
}
.cancel_btn:hover {
  border-color: #6f005f;
  color: #6f005f;
}
:deep(.ant-popconfirm .ant-popconfirm-description) {
  margin-inline-start: 0;
}
</style>
<style scoped src="@/atdv/pagination.css"></style>
<style scoped src="@/atdv/spin.css"></style>
