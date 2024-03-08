<template>
  <div class="main">
    <a-card class="header_card">
      <div class="card_header">
        <LeftOutlined :style="{ fontSize: '18px' }" @click="back" />
        <div style="margin-left: 10px; font-weight: bold">项目详情</div>
      </div>
      <div class="card_title">
        项目名称：
        <div style="font-weight: bold; margin-right: 30px">{{ data.versionInfo.name }}</div>
        版本编号：
        <div style="margin-right: 30px">
          <a-select
            v-model:value="data.selectedVersion"
            show-search
            :options="data.versionOptions"
            :filter-option="filterOption"
            style="width: 100px"
            @change="changeVersion"></a-select>
        </div>
        最近一次扫描时间：
        <div>{{ data.versionInfo.time }}</div>
        <a-button type="primary" style="margin-left: 30px" @click="compare">版本对比</a-button>
      </div>
    </a-card>
    <a-card class="content_card">
      <div class="content">
        <div class="content_header">
          <a-radio-group v-model:value="data.mode">
            <a-tooltip>
              <template #title>树形展示</template>
              <a-radio-button value="tree" @click="changeMode('tree')" style="width: 60px">
                <ApartmentOutlined :style="{ fontSize: '16px' }" />
              </a-radio-button>
            </a-tooltip>
            <a-tooltip>
              <template #title>平铺展示</template>
              <a-radio-button value="tiled" @click="changeMode('tiled')" style="width: 60px">
                <UnorderedListOutlined :style="{ fontSize: '16px' }" />
              </a-radio-button>
            </a-tooltip>
          </a-radio-group>
          <div v-if="data.mode === 'tiled'">
            <!-- <a-input-search
              v-model:value="data.search.name"
              placeholder="请输入组件名称"
              style="width: 250px"></a-input-search> -->
            <a-checkbox v-model:checked="data.detail">详细导出</a-checkbox>
            <a-button type="primary" style="margin-left: 10px" @click="exportExcel">
              <ExportOutlined />导出Excel
            </a-button>
          </div>
        </div>
        <div style="margin-top: 20px">
          <TreeList ref="treeList"></TreeList>
          <TiledList ref="tiledList"></TiledList>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, defineExpose } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LeftOutlined, ApartmentOutlined, UnorderedListOutlined, ExportOutlined } from '@ant-design/icons-vue'
import { GetVersionList, GetVersionInfo, ExportBrief, ExportDetail } from '@/api/frontend'
import TreeList from './components/TreeList.vue'
import TiledList from './components/TiledList.vue'
import { message } from 'ant-design-vue'

onMounted(async () => {
  const groupId = route.query.groupId
  const artifactId = route.query.artifactId
  const version = route.query.version
  await getVersionInfo(groupId, artifactId, version)
})

const router = useRouter()
const route = useRoute()
const treeList = ref()
const tiledList = ref()
const data = reactive({
  versionInfo: {},
  selectedVersion: '',
  versionOptions: [],
  mode: 'tree',
  search: {
    name: ''
  },
  detail: false
})
const back = () => {
  router.back()
}
const getVersionInfo = async (groupId, artifactId, version) => {
  data.selectedVersion = version
  await GetVersionList({ groupId, artifactId })
    .then((res) => {
      // console.log('GetVersionList', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      data.versionOptions = res.data.map((option) => {
        return { label: option, value: option, key: option }
      })
      GetVersionInfo({ groupId, artifactId, version })
        .then((res) => {
          // console.log('GetVersionInfo', res)
          if (res.code !== 200) {
            message.error(res.message)
            return
          }
          data.versionInfo = res.data
          changeMode('tree')
        })
        .catch((err) => {
          console.error(err)
        })
    })
    .catch((err) => {
      console.error(err)
    })
}
const changeVersion = (value) => {
  router.push({
    path: '/home/appDetail',
    query: {
      groupId: data.versionInfo.groupId,
      artifactId: data.versionInfo.artifactId,
      version: value
    }
  })
  getVersionInfo(data.versionInfo.groupId, data.versionInfo.artifactId, value)
}
const compare = () => {
  router.push({
    path: '/home/compare',
    query: {
      groupId: data.versionInfo.groupId,
      artifactId: data.versionInfo.artifactId,
      currentVersion: data.selectedVersion
    }
  })
}
const changeMode = (mode) => {
  data.mode = mode
  if (mode === 'tree') {
    treeList.value.show(data.versionInfo.groupId, data.versionInfo.artifactId, data.selectedVersion)
    tiledList.value.hide()
  }
  if (mode === 'tiled') {
    treeList.value.hide()
    tiledList.value.show(data.versionInfo.groupId, data.versionInfo.artifactId, data.selectedVersion)
  }
}
const filterOption = (input, option) => {
  return option.value.toLowerCase().indexOf(input.toLowerCase()) >= 0
}
const exportExcel = () => {
  const params = {
    groupId: data.versionInfo.groupId,
    artifactId: data.versionInfo.artifactId,
    version: data.versionInfo.version
  }
  if (!data.detail) {
    ExportBrief(params)
      .then((res) => {
        // console.log('ExportBrief', res)
        const reader = new FileReader()
        reader.readAsText(res.data, 'utf-8')
        reader.onload = () => {
          try {
            const result = JSON.parse(reader.result)
            if (result.code !== 200) {
              message.error(result.message)
            } else {
              downloadExcel(res.data, `${data.versionInfo.name}-${data.versionInfo.version}-dependencyTable-brief`)
              message.success('导出成功')
            }
          } catch (e) {
            downloadExcel(res.data, `${data.versionInfo.name}-${data.versionInfo.version}-dependencyTable-brief`)
            message.success('导出成功')
          }
        }
      })
      .catch((err) => {
        console.error(err)
      })
  } else {
    ExportDetail(params)
      .then((res) => {
        // console.log('ExportDetail', res)
        const reader = new FileReader()
        reader.readAsText(res.data, 'utf-8')
        reader.onload = () => {
          try {
            const result = JSON.parse(reader.result)
            if (result.code !== 200) {
              message.error(result.message)
            } else {
              downloadExcel(res.data, `${data.versionInfo.name}-${data.versionInfo.version}-dependencyTable-detail`)
              message.success('导出成功')
            }
          } catch (e) {
            downloadExcel(res.data, `${data.versionInfo.name}-${data.versionInfo.version}-dependencyTable-detail`)
            message.success('导出成功')
          }
        }
      })
      .catch((err) => {
        console.error(err)
      })
  }
}
const downloadExcel = (data, fileName) => {
  const xlsx = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  const blob = new Blob([data], { type: xlsx })
  const a = document.createElement('a')
  a.style.display = 'none'
  a.href = URL.createObjectURL(blob)
  a.download = `${fileName}.xlsx`
  a.click()
  a.remove()
}
</script>

<style lang="less" scoped>
.main {
  position: relative;
  height: 100%;
}
.header_card {
  position: absolute;
  width: 100%;
  height: 120px;
}
.content_card {
  position: absolute;
  width: 100%;
  height: calc(100% - 120px);
  top: 135px;
  overflow-y: scroll;
}
.card_header {
  display: flex;
  align-items: center;
  font-size: 18px;
}
.card_title {
  display: flex;
  align-items: center;
  margin-top: 10px;
  font-size: 16px;
}
.content_header {
  display: flex;
  justify-content: space-between;
}
</style>
<style scoped src="@/atdv/primary-btn.css"></style>
<style scoped src="@/atdv/input.css"></style>
<style scoped src="@/atdv/input-search.css"></style>
<style scoped src="@/atdv/radio-btn.css"></style>
<style scoped src="@/atdv/select.css"></style>
<style scoped src="@/atdv/checkbox.css"></style>
