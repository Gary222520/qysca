<template>
  <a-drawer v-model:open="data.open" width="600px" title="组件详情" :closable="false" placement="right">
    <p>组件名称：{{ data.component.name }}</p>
    <p>Some contents...</p>
    <p>Some contents...</p>
    <p>Some contents...</p>
  </a-drawer>
</template>

<script setup>
import { reactive, defineExpose } from 'vue'

const data = reactive({
  open: false,
  component: {}
})
const open = (component) => {
  data.open = true
  data.component = component
}
const close = () => {
  data.open = false
}
defineExpose({ open })
</script>

<style scoped></style>
