<template>
  <div>
    <div class="main">
      <div class="title">用户管理</div>
      <a-card class="content_card"></a-card>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const data = reactive({})
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
</style>
