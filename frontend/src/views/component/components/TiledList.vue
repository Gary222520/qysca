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
          <template v-if="column.key === 'type'">
            <div v-if="record.type === 'opensource'">开源</div>
            <div v-if="record.type === 'business'">商用</div>
            <div v-if="record.type === 'internal'">内部使用</div>
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
import { GetComponentTiled } from '@/api/frontend'
import Drawer from '@/views/project/components/Drawer.vue'
import { message } from 'ant-design-vue'

const drawer = ref()
const data = reactive({
  visible: false,
  spinning: false,
  component: {},
  datasource: [],
  columns: [
    { title: '名称', dataIndex: 'cname', key: 'cName' },
    { title: '版本', dataIndex: 'cversion', key: 'cVersion' },
    { title: '语言', dataIndex: 'language', key: 'language' },
    { title: '依赖方式', dataIndex: 'direct', key: 'direct' },
    { title: '依赖层级', dataIndex: 'depth', key: 'depth' },
    { title: '组件类型', dataIndex: 'type', key: 'type' }
  ]
})
const pagination = reactive({
  current: 1,
  total: 0,
  pageSize: 8,
  showSizeChanger: false,
  onChange: (page, size) => {
    pagination.current = page
    getComponentTiled(data.component, page, size)
  },
  hideOnSinglePage: true
})
const show = (component) => {
  data.visible = true
  data.component = component
  getComponentTiled(component)
}
const getCount = async (name, version, language) => {
  let count = 0
  const params = { name, version, language, number: 1, size: 6 }
  if (params.language instanceof Array) params.language = 'app'
  await GetComponentTiled(params).then((res) => {
    if (res.code !== 200) return
    count = res.data.totalElements
  })
  return count
}
const getComponentTiled = (component, number = 1, size = 6) => {
  const params = {
    name: component.name,
    version: component.version,
    language: component.language,
    number,
    size
  }
  if (params.language instanceof Array) params.language = 'app'
  data.spinning = true
  GetComponentTiled(params)
    .then((res) => {
      // console.log('GetComponentTiled', res)
      data.spinning = false
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      data.datasource = res.data.content
      pagination.total = res.data.totalElements
      pagination.current = number
    })
    .catch((err) => {
      data.spinning = false
      console.error(err)
    })
}
const hide = () => {
  data.visible = false
}
const showInfo = (record) => {
  drawer.value.open(record, false)
}
defineExpose({ show, hide, getCount })
</script>

<style scoped>
.column_name {
  cursor: pointer;
}
.column_name:hover {
  color: #00557c;
}
</style>
<style scoped src="@/atdv/pagination.css"></style>
<style scoped src="@/atdv/spin.css"></style>
