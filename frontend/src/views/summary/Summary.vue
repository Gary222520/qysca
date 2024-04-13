<template>
  <div class="main">
    <div class="title">
      统计概览
      <a-tooltip>
        <template #title>{{ `当前部门：${buName}` }}</template>
        <a-tag style="font-weight: normal; margin-left: 10px">{{ buName }}</a-tag>
      </a-tooltip>
    </div>
    <div class="content">
      <div style="display: flex; width: 100%; height: 15%; margin-bottom: 10px">
        <a-card class="number-card" hoverable>
          <div style="display: flex; justify-content: space-between">
            <div>
              <div class="number-title">应用总数</div>
              <div class="number-count">{{ count.app }}</div>
            </div>
            <ScheduleOutlined :style="{ fontSize: '30px', color: '#6f005f' }" />
          </div>
        </a-card>
        <a-card class="number-card" hoverable>
          <div style="display: flex; justify-content: space-between">
            <div>
              <div class="number-title">组件总数</div>
              <div class="number-count">{{ count.com }}</div>
            </div>
            <AppstoreOutlined :style="{ fontSize: '30px', color: '#6f005f' }" />
          </div>
        </a-card>
        <a-card class="number-card" hoverable>
          <div style="display: flex; justify-content: space-between">
            <div>
              <div class="number-title">漏洞总数</div>
              <div class="number-count">{{ count.vul }}</div>
            </div>
            <BugOutlined :style="{ fontSize: '30px', color: '#6f005f' }" />
          </div>
        </a-card>
        <a-card class="number-card" hoverable>
          <div style="display: flex; justify-content: space-between">
            <div>
              <div class="number-title">许可证总数</div>
              <div class="number-count">{{ count.license }}</div>
            </div>
            <VerifiedOutlined :style="{ fontSize: '30px', color: '#6f005f' }" />
          </div>
        </a-card>
      </div>
      <div style="display: flex; width: 100%; height: 40%; margin-bottom: 10px">
        <a-card class="vul-card"><VulChart ref="vulChart"></VulChart></a-card>
        <a-card class="license-card"><LicenseChart ref="licenseChart"></LicenseChart></a-card>
      </div>
      <div style="display: flex; width: 100%; height: 40%; margin-bottom: 10px">
        <a-card class="app-vul-card"><AppVulChart ref="appVulChart"></AppVulChart></a-card>
        <a-card class="app-license-card"><AppLicenseChart ref="appLicenseChart"></AppLicenseChart></a-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ScheduleOutlined, AppstoreOutlined, BugOutlined, VerifiedOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { reactive, ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { AppStatistic, ComStatistic, VulStatistic, LicenseStatistic } from '@/api/frontend'
import VulChart from './components/VulChart.vue'
import LicenseChart from './components/LicenseChart.vue'
import AppVulChart from './components/AppVulChart.vue'
import AppLicenseChart from './components/AppLicenseChart.vue'

onMounted(() => {
  getStatistics()
})

const vulChart = ref()
const licenseChart = ref()
const appVulChart = ref()
const appLicenseChart = ref()
const store = useStore()

const data = reactive({})

const count = reactive({
  app: 0,
  com: 0,
  vul: 0,
  license: 0
})
const buName = computed(() => {
  return store.getters.permission[0]?.buName
})

const getStatistics = () => {
  AppStatistic()
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      // console.log('AppStatistic', res)
      count.app = res.data
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
      // console.log('ComStatistic', res)
      count.com = res.data
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
      // console.log('VulStatistic', res)
      count.vul = res.data.totalNumber
      vulChart.value.draw(res.data.categoryCountMap)
      appVulChart.value.draw(res.data.compareDTOList)
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
      // console.log('LicenseStatistic', res)
      count.license = res.data.totalNumber
      licenseChart.value.draw(res.data.licenseTypeNumberMap)
      appLicenseChart.value.draw(res.data.compareDTOList)
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
  display: flex;
  align-items: center;
}
.content {
  position: absolute;
  width: 100%;
  height: calc(100% - 30px);
  overflow-y: scroll;
}
.number-card {
  width: 25%;
  height: 100%;
  margin-right: 10px;
}
.number-title {
  font-size: 14px;
  font-weight: bold;
}
.number-count {
  font-size: 24px;
}
.vul-card {
  width: 40%;
  height: 100%;
  margin-right: 10px;
}
.license-card {
  width: 60%;
  height: 100%;
  margin-right: 10px;
}
.app-vul-card {
  width: 50%;
  height: 100%;
  margin-right: 10px;
}
.app-license-card {
  width: 50%;
  height: 100%;
  margin-right: 10px;
}
:deep(.ant-card .ant-card-body) {
  padding: 20px;
}
</style>
<style scoped src="@/atdv/pagination.css"></style>
<style scoped src="@/atdv/input.css"></style>
<style scoped src="@/atdv/input-search.css"></style>
<style scoped src="@/atdv/select.css"></style>
<style scoped src="@/atdv/primary-btn.css"></style>
<style scoped src="@/atdv/delete-btn.css"></style>
<style scoped src="@/atdv/row-selection.css"></style>
