<template>
  <a-modal v-model:open="data.open" title="扫描分析">
    <a-tabs v-model:activeKey="data.activeKey">
      <a-tab-pane key="1" tab="快速扫描">Content of Tab Pane 1</a-tab-pane>
      <a-tab-pane key="2" tab="项目更新">Content of Tab Pane 2</a-tab-pane>
    </a-tabs>
    <!-- <a-steps direction="vertical"></a-steps> -->
  </a-modal>
</template>

<script>
import { reactive } from 'vue'
export default {
  setup() {
    const data = reactive({
      open: false,
      activeKey: '1'
    })
    const open = () => {
      data.open = true
    }
    return { data, open }
  }
}
</script>

<style scoped></style>
