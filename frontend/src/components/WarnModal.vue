<template>
  <a-modal v-model:open="data.open">
    <template #title>
      <div style="font-size: 20px">删除失败</div>
    </template>
    <div class="content">
      <p>{{ data.warnText }}</p>
      <a-table :data-source="table.datasource" :columns="table.columns" :pagination="false"></a-table>
    </div>
    <template #footer>
      <a-button class="btn" @click="close()">确定</a-button>
    </template>
  </a-modal>
</template>

<script setup>
import { reactive, defineExpose, defineEmits } from 'vue'
import { DeleteProject } from '@/api/frontend'
import { WarningOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'

const emit = defineEmits(['ok'])
const store = useStore()

const data = reactive({
  open: false,
  warnText: ''
})
const table = reactive({
  datasource: [],
  columns: [
    { title: '应用名称', dataIndex: 'name', key: 'name' },
    { title: '版本', dataIndex: 'version', key: 'version' }
  ]
})
const open = (appList, warnText) => {
  data.open = true
  data.warnText = warnText
  table.datasource = appList
}
const close = () => {
  data.open = false
  emit('ok')
}
defineExpose({ open })
</script>

<style scoped>
.btn:hover {
  border-color: #6f005f;
  color: #6f005f;
}
</style>
