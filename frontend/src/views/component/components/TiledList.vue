<template>
  <div v-if="data.visible">
    <a-spin :spinning="data.spinning" tip="依赖信息加载中，请稍等...">
      <a-table :data-source="data.datasource" :columns="data.columns" bordered :pagination="pagination">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <div class="column_name" @click="showInfo(record)">{{ record.name }}</div>
          </template>
          <template v-if="column.key === 'direct'">
            <div>{{ record.direct ? '直接依赖' : '间接依赖' }}</div>
          </template>
          <template v-if="column.key === 'opensource'">
            <div>{{ record.opensource ? '开源' : '闭源' }}</div>
          </template>
        </template>
        <template #emptyText>暂无数据</template>
      </a-table>
    </a-spin>
    <Drawer ref="drawer"></Drawer>
  </div>
</template>

<script setup>
import { reactive, ref, defineExpose } from 'vue'
import { GetOpenComponentTiled, GetCloseComponentTiled } from '@/api/frontend'
import Drawer from '@/views/application/components/Drawer.vue'
import { message } from 'ant-design-vue'

const drawer = ref()
const data = reactive({
  visible: false,
  spinning: false,
  component: {},
  datasource: [],
  columns: [
    { title: '组件名称', dataIndex: 'name', key: 'name' },
    { title: '组织ID', dataIndex: 'groupId', key: 'groupId' },
    { title: '工件ID', dataIndex: 'artifactId', key: 'artifactId' },
    { title: '版本', dataIndex: 'version', key: 'version', width: 80 },
    { title: '语言', dataIndex: 'language', key: 'language', width: 80 },
    { title: '依赖方式', dataIndex: 'direct', key: 'direct', width: 90 },
    { title: '依赖层级', dataIndex: 'depth', key: 'depth', width: 90 },
    { title: '依赖范围', dataIndex: 'scope', key: 'scope', width: 90 },
    { title: '是否开源', dataIndex: 'opensource', key: 'opensource', width: 90 },
    { title: '许可证', dataIndex: 'licenses', key: 'licenses' }
  ]
})
const pagination = reactive({
  current: 1,
  total: 0,
  pageSize: 10,
  showSizeChanger: false,
  onchange: (page, size) => {
    pagination.current = page
    getComponentTiled(data.component, page, size)
  }
})
const show = (component) => {
  data.visible = true
  data.component = component
  getComponentTiled(component)
}
const getComponentTiled = (component, number = 1, size = 10) => {
  const params = {
    groupId: component.groupId,
    artifactId: component.artifactId,
    version: component.version,
    number,
    size
  }
  data.spinning = true
  if (component.opensource) {
    GetOpenComponentTiled(params)
      .then((res) => {
        // console.log('GetOpenComponentTiled', res)
        data.spinning = false
        if (res.code !== 200) {
          message.error(res.message)
          return
        }
        data.datasource = res.data.content
        pagination.total = res.data.totalElements
      })
      .catch((err) => {
        data.spinning = false
        console.error(err)
      })
  } else {
    GetCloseComponentTiled(params)
      .then((res) => {
        // console.log('GetCloseComponentTiled', res)
        data.spinning = false
        if (res.code !== 200) {
          message.error(res.message)
          return
        }
        data.datasource = res.data.content
        pagination.total = res.data.totalElements
      })
      .catch((err) => {
        data.spinning = false
        console.error(err)
      })
  }
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
<style scoped src="@/atdv/spin.css"></style>
