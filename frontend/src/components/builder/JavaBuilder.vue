<template>
  <div>
    <div class="main" v-if="data.step === 0">
      <div class="content">
        <a-card class="card" hoverable @click="handleMaven()">
          <div class="card_title">
            <img class="img" src="@/assets/maven.png" />
            <div class="name">maven</div>
          </div>
          <div class="card_text">
            <ul style="margin-bottom: 0">
              <li class="list_item">maven构建并管理的应用</li>
              <li class="list_item">pom.xml记录依赖信息</li>
            </ul>
          </div>
        </a-card>
        <a-card class="card" hoverable @click="handleGradle()">
          <div class="card_title">
            <img class="img" src="@/assets/gradle.png" />
            <div class="name">gradle</div>
          </div>
          <div class="card_text">
            <ul>
              <li class="list_item">gradle构建并管理的应用</li>
              <li class="list_item">settings.gradle记录依赖信息</li>
            </ul>
          </div>
        </a-card>
      </div>
    </div>
    <div class="main" v-if="data.step === 1 && data.tool === 'maven'">
      <!-- <div class="back">
        <RollbackOutlined :style="{ fontSize: '18px', marginRight: '5px' }" @click="back()" />返回选择工具
      </div> -->
      <div class="content">
        <a-card class="card" hoverable @click="selectBuilder('maven')">
          <div class="card_title">
            <img class="img" src="@/assets/pom.png" />
            <div class="name">pom.xml</div>
          </div>
          <div class="card_text">
            <ul style="margin-bottom: 0">
              <li class="list_item">pom.xml记录应用依赖信息</li>
              <li class="list_item">扫描pom.xml依赖文件</li>
            </ul>
          </div>
        </a-card>
        <a-card class="card" hoverable @click="selectBuilder('zip')">
          <div class="card_title">
            <img class="img" src="@/assets/zip.png" />
            <div class="name">zip</div>
          </div>
          <div class="card_text">
            <ul style="margin-bottom: 0">
              <li class="list_item">应用根目录的zip压缩文件</li>
              <li class="list_item">扫描zip中的依赖文件</li>
            </ul>
          </div>
        </a-card>
      </div>
      <div class="content" style="margin-top: 10px">
        <a-card class="card" hoverable @click="selectBuilder('jar')">
          <div class="card_title">
            <img class="img" src="@/assets/jar.png" />
            <div class="name">jar</div>
          </div>
          <div class="card_text">
            <ul style="margin-bottom: 0">
              <li class="list_item">应用构建完成的jar包</li>
              <li class="list_item">扫描jar包中的依赖文件</li>
            </ul>
          </div>
        </a-card>
      </div>
    </div>
    <div class="main" v-if="data.step === 1 && data.tool === 'gradle'">
      <div class="content">
        <a-card class="card" hoverable @click="selectBuilder('gradle')">
          <div class="card_title">
            <img class="img" src="@/assets/zip.png" />
            <div class="name">zip</div>
          </div>
          <div class="card_text">
            <ul>
              <li class="list_item">应用根目录的zip压缩文件</li>
              <li class="list_item">扫描zip中的依赖文件</li>
            </ul>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, defineEmits, defineExpose, onBeforeUnmount } from 'vue'
import { RollbackOutlined } from '@ant-design/icons-vue'

onBeforeUnmount(() => {
  data.step = 0
  data.tool = ''
})

const data = reactive({
  step: 0,
  tool: ''
})
const emit = defineEmits(['select'])

const handleMaven = () => {
  data.step = 1
  data.tool = 'maven'
  selectBuilder('tool')
}
const handleGradle = () => {
  data.step = 1
  data.tool = 'gradle'
  selectBuilder('tool')
}
const back = () => {
  data.step = 0
  data.tool = ''
}

const selectBuilder = (builder) => {
  emit('select', builder)
}

defineExpose({ back })
</script>

<style scoped>
.main {
  position: relative;
}
.content {
  width: 600px;
  display: flex;
}
.card {
  width: 290px;
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
