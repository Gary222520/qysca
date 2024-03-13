<template>
  <a-collapse collapsible="icon" @change="showAppInfo">
    <a-collapse-panel v-for="(app, index) in appList" :key="index">
      <template #header></template>
      <AppCollapse v-if="hasChild(app)" ref="appCollapse" :app-list="data.subAppList"></AppCollapse>
    </a-collapse-panel>
  </a-collapse>
</template>

<script setup>
import { reactive, ref, defineProps } from 'vue'
import AppCollapse from '@/views/application/components/AppCollapse.vue'

const props = defineProps({
  appList: {
    type: Array,
    default: () => []
  }
})

const appCollapse = ref()

const data = reactive({
  subAppList: []
})

const showAppInfo = (index) => {}

const hasChild = (app) => {
  return false
}
</script>

<style scoped>
:deep(.ant-collapse-header::before) {
  position: absolute;
  top: 50%;
  left: -17px;
  width: 17px;
  height: 1px;
  background-color: #d9d9d9;
  content: '';
}
:deep(.ant-collapse .ant-collapse-content > .ant-collapse-content-box) {
  padding: 16px 0px 16px 16px;
}
:deep(.ant-collapse > .ant-collapse-item:last-child),
:deep(.ant-collapse > .ant-collapse-item:last-child > .ant-collapse-header),
:deep(.ant-collapse .ant-collapse-item:last-child > .ant-collapse-content) {
  border-radius: 0;
}
</style>
