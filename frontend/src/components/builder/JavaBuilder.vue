<template>
  <div class="main">
    <div class="content">
      <a-card class="card" hoverable @click="selectBuilder('maven')">
        <div class="card_title">
          <img class="img" src="@/assets/maven.png" />
          <div class="name">Maven</div>
        </div>
        <div class="card_text">
          <ul style="margin-bottom: 0">
            <li class="list_item">maven构建并管理的项目</li>
            <li class="list_item">pom.xml记录依赖信息</li>
          </ul>
        </div>
      </a-card>
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
    <div class="content" style="margin-top: 10px">
      <a-card class="card" hoverable @click="selectBuilder('zip')">
        <div class="card_title">
          <img class="img" src="@/assets/gradle.png" />
          <div class="name">Gradle</div>
        </div>
        <div class="card_text">
          <ul>
            <li class="list_item">gradle构建并管理的项目</li>
            <li class="list_item">settings.gradle记录依赖信息</li>
          </ul>
        </div>
      </a-card>
      <a-card class="card" hoverable @click="selectBuilder('jar')">
        <div class="card_title">
          <img class="img" src="@/assets/jar.png" />
          <div class="name">Jar</div>
        </div>
        <div class="card_text">
          <ul style="margin-bottom: 0">
            <li class="list_item">项目构建完成的jar包</li>
            <li class="list_item">扫描jar包中的依赖文件</li>
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
