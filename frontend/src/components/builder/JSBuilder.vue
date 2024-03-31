<template>
  <div class="main">
    <div class="content">
      <a-card class="card" hoverable @click="selectBuilder('zip')">
        <div class="card_title">
          <img class="img" src="@/assets/zip.png" />
          <div class="name">Zip</div>
        </div>
        <div class="card_text">
          <ul style="margin-bottom: 0">
            <li class="list_item">项目根目录的Zip压缩文件</li>
            <li class="list_item">扫描zip中的依赖文件</li>
          </ul>
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, defineEmits } from 'vue'

const data = reactive({})
const emit = defineEmits(['select'])

const selectBuilder = (builder) => {
  emit('select', builder)
}
</script>

<style scoped>
.content {
  width: 600px;
  display: flex;
}
.card {
  width: 50%;
  margin-right: 10px;
}
.card_title {
  display: flex;
  align-items: center;
}
.img {
  height: 60px;
  margin-left: 10px;
}
.name {
  font-size: 28px;
  font-weight: bold;
  margin-left: 20px;
}
.card_text {
  margin-top: 10px;
}
.list_item {
  margin-top: 5px;
}
</style>
