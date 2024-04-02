<template>
  <div class="main">
    <div class="title">许可证管理</div>
    <a-card class="content_card">
      <div class="operations">
        <div>
          <a-input-search
            v-if="!data.accurate"
            v-model:value="data.search.name"
            placeholder="请输入许可证名称"
            style="width: 250px"
            @change="(e) => getLicenses()"
            @search="(value, e) => getLicenses()"></a-input-search>
        </div>
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
            </template>
          </template>
          <template #emptyText>暂无数据</template>
        </a-table>
      </a-spin>
      <Drawer ref="drawer"></Drawer>
    </a-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import {
  PlusOutlined,
  FileTextOutlined,
  SyncOutlined,
  DeleteOutlined,
  RollbackOutlined,
  LoadingOutlined,
  ExclamationCircleOutlined,
  CheckOutlined,
  CloseOutlined
} from '@ant-design/icons-vue'
import { GetAllLicense } from '@/api/frontend'
import Drawer from '@/components/LicenseDrawer.vue'
import { message } from 'ant-design-vue'

onMounted(() => {
  getLicenses()
})
const drawer = ref()

const data = reactive({
  accurate: false,
  spinning: false,
  search: {
    name: ''
  },
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
  ]
})
const pagination = reactive({
  current: 1,
  total: 0,
  pageSize: 10,
  showSizeChanger: false,
  onChange: (page, size) => {
    pagination.current = page
    getLicenses(page, size)
  },
  hideOnSinglePage: true
})
const getLicenses = (page = 1, size = 10) => {
  const params = {
    ...data.search,
    page,
    size
  }
  GetAllLicense(params)
    .then((res) => {
      // console.log('GetComponents', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      data.datasource = res.data.content
      data.datasource.forEach((item) => {
        if (item.name === '') item.name = '-'
      })
      pagination.total = res.data.totalElements
      pagination.current = page
    })
    .catch((err) => {
      console.error(err)
    })
}
const showInfo = (record) => {
  drawer.value.open(record, true)
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
  justify-content: right;
  margin-bottom: 20px;
}
.column_name {
  cursor: pointer;
}
.column_name:hover {
  color: #6f005f;
}
.action_icon {
  margin-right: 10px;
}
.cancel_btn:hover {
  border-color: #6f005f;
  color: #6f005f;
}
</style>
<style scoped src="@/atdv/primary-btn.css"></style>
<style scoped src="@/atdv/delete-btn.css"></style>
<style scoped src="@/atdv/input.css"></style>
<style scoped src="@/atdv/input-search.css"></style>
<style scoped src="@/atdv/delete-btn.css"></style>
<style scoped src="@/atdv/pagination.css"></style>
<style scoped src="@/atdv/radio-btn.css"></style>
<style scoped src="@/atdv/select.css"></style>
<style scoped src="@/atdv/spin.css"></style>
