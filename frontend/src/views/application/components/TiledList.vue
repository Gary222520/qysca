<template>
  <div v-if="data.visible">平铺展示</div>
</template>

<script>
import { reactive } from 'vue'

export default {
  setup() {
    const data = reactive({
      visible: false
    })
    const show = () => {
      data.visible = true
    }
    const hide = () => {
      data.visible = false
    }
    return { data, show, hide }
  }
}
</script>
