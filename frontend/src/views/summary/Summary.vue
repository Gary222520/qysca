<template>
  <div class="main">
    <div class="title">统计概览</div>
    <div class="content">
      <div style="display: flex; width: 100%; height: 100%">
        <a-card class="number-card" hoverable>应用总数</a-card>
        <a-card class="number-card" hoverable>组件总数</a-card>
        <a-card class="number-card" hoverable>漏洞总数</a-card>
        <a-card class="number-card" hoverable>许可证总数</a-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import {} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { reactive, ref, onMounted } from 'vue'
import { AppStatistic, ComStatistic, VulStatistic, LicenseStatistic } from '@/api/frontend'

onMounted(() => {
  getStatistics()
})

const data = reactive({})

const count = reactive({
  app: 0,
  com: 0,
  vul: 0,
  license: 0
})

const getStatistics = () => {
  AppStatistic()
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      console.log('AppStatistic', res)
    })
    .catch((err) => {
      console.error(err)
    })
  ComStatistic()
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      console.log('ComStatistic', res)
    })
    .catch((err) => {
      console.error(err)
    })
  VulStatistic()
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      console.log('VulStatistic', res)
    })
    .catch((err) => {
      console.error(err)
    })
  LicenseStatistic()
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      console.log('LicenseStatistic', res)
    })
    .catch((err) => {
      console.error(err)
    })
}
</script>

<style scoped>
.main {
  position: relative;
  height: 100%;
}
.title {
  font-weight: bold;
  font-size: 20px;
  margin-bottom: 15px;
}
.content {
  position: absolute;
  width: 100%;
  height: calc(100% - 30px);
  overflow-y: scroll;
}
.number-card {
  width: 25%;
  height: 15%;
  margin-right: 10px;
}
</style>
<style scoped src="@/atdv/pagination.css"></style>
<style scoped src="@/atdv/input.css"></style>
<style scoped src="@/atdv/input-search.css"></style>
<style scoped src="@/atdv/select.css"></style>
<style scoped src="@/atdv/primary-btn.css"></style>
<style scoped src="@/atdv/delete-btn.css"></style>
<style scoped src="@/atdv/row-selection.css"></style>
