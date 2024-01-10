<template>
  <div v-if="data.visible">
    <a-table :data-source="data.datasource" :columns="data.columns" bordered>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'name'">
          <div class="column_name" @click="showInfo(record)">{{ record.name }}</div>
        </template>
      </template>
    </a-table>
    <Drawer ref="drawer"></Drawer>
  </div>
</template>

<script setup>
import { reactive, ref, defineExpose } from 'vue'
import Drawer from './Drawer.vue'

const drawer = ref()
const data = reactive({
  visible: false,
  datasource: [
    { name: '组件1', version: '2.4.3', language: 'java', depend: '直接依赖' },
    { name: '组件2', version: '2.4.3', language: 'java', depend: '间接依赖' },
    { name: '组件3', version: '5.4.3', language: 'java', depend: '间接依赖' },
    { name: '组件4', version: '0.2.6', language: 'java', depend: '直接依赖' },
    { name: '组件5', version: '1.2.5', language: 'java', depend: '间接依赖' },
    { name: '组件6', version: '1.18.20', language: 'java', depend: '间接依赖' },
    { name: '组件7', version: '2.1.3', language: 'java', depend: '直接依赖' }
  ],
  columns: [
    { title: '组件名称', dataIndex: 'name', key: 'name' },
    { title: '版本', dataIndex: 'version', key: 'version' },
    { title: '语言', dataIndex: 'language', key: 'language' },
    { title: '依赖方式', dataIndex: 'depend', key: 'depend' }
  ]
})
const show = () => {
  data.visible = true
}
const hide = () => {
  data.visible = false
}
const showInfo = (record) => {
  drawer.value.open({ name: record.name })
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
/* 表格分页样式 */
:deep(.ant-pagination .ant-pagination-item-active) {
  border-color: #6f005f;
  color: #6f005f;
}
:deep(.ant-pagination .ant-pagination-item-active a) {
  color: #6f005f !important;
}
</style>
