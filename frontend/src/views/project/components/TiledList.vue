<template>
  <div v-if="data.visible">
    <a-table :data-source="data.datasource" :columns="data.columns" bordered :pagination="pagination">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'name'">
          <div class="column_name" @click="showInfo(record)">{{ record.name }}</div>
        </template>
        <template v-if="column.key === 'direct'">
          <div>{{ record.direct ? '直接依赖' : '间接依赖' }}</div>
        </template>
        <template v-if="column.key === 'type'">
          <div v-if="record.type === 'opensource'">开源</div>
          <div v-if="record.type === 'business'">商用</div>
          <div v-if="record.type === 'internal'">内部使用</div>
        </template>
      </template>
      <template #emptyText>暂无数据</template>
    </a-table>
    <Drawer ref="drawer"></Drawer>
  </div>
</template>

<script setup>
import { reactive, ref, defineExpose } from 'vue'
import { GetProjectTiled } from '@/api/frontend'
import Drawer from './Drawer.vue'
import { message } from 'ant-design-vue'

const drawer = ref()
const data = reactive({
  visible: false,
  projectInfo: {},
  datasource: [],
  columns: [
    { title: '组织ID', dataIndex: 'cgroupId', key: 'groupId' },
    { title: '工件ID', dataIndex: 'cartifactId', key: 'artifactId' },
    { title: '版本', dataIndex: 'cversion', key: 'version', width: 80 },
    { title: '语言', dataIndex: 'language', key: 'language', width: 80 },
    { title: '依赖方式', dataIndex: 'direct', key: 'direct', width: 90 },
    { title: '依赖层级', dataIndex: 'depth', key: 'depth', width: 90 },
    { title: '依赖范围', dataIndex: 'scope', key: 'scope', width: 90 },
    { title: '组件类型', dataIndex: 'type', key: 'type', width: 90 }
  ]
})
const pagination = reactive({
  current: 1,
  total: 0,
  pageSize: 10,
  showSizeChanger: false,
  onChange: (page, size) => {
    pagination.current = page
    getProjectTiled(data.projectInfo.name, data.projectInfo.version, page, size)
  },
  hideOnSinglePage: true
})
const show = (name, version) => {
  data.visible = true
  // data.projectInfo.groupId = groupId
  // data.projectInfo.artifactId = artifactId
  data.projectInfo.name = name
  data.projectInfo.version = version
  getProjectTiled(name, version)
}
const getProjectTiled = (name, version, number = 1, size = 10) => {
  GetProjectTiled({ name, version, number, size })
    .then((res) => {
      // console.log('GetProjectTiled', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      data.datasource = res.data.content
      pagination.total = res.data.totalElements
    })
    .catch((err) => {
      console.error(err)
    })
}
const hide = () => {
  data.visible = false
}
const showInfo = (record) => {
  drawer.value.open(record, false)
}
defineExpose({ show, hide })
</script>

<style scoped>
.column_name {
  cursor: pointer;
}
.column_name:hover {
  color: #6f005f;
}
</style>
<style scoped src="@/atdv/pagination.css"></style>
