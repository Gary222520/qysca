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
            <a-input-search
              v-model:value="data.search.name"
              placeholder="请输入组件名称"
              style="width: 250px"></a-input-search>
            <a-button type="primary" style="margin-left: 10px"><ExportOutlined />导出Excel</a-button>
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
import { GetProjectInfo } from '@/api/frontend'
import TreeList from './components/TreeList.vue'
import TiledList from './components/TiledList.vue'
import { message } from 'ant-design-vue'

onMounted(async () => {
  const name = route.query.name
  const version = route.query.version
  await getVersionInfo(name, version)
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
  }
})
const back = () => {
  router.push('/home/application')
}
const getVersionInfo = async (name, version) => {
  data.selectedVersion = version
  await GetProjectInfo({
    name,
    number: 1,
    size: 100
  })
    .then((res) => {
      const options = []
      res.data.content.forEach((item) => {
        if (item.version === version) data.versionInfo = item
        options.push({ label: item.version, value: item.version, key: item.version })
      })
      data.versionOptions = options
      changeMode('tree')
    })
    .catch((err) => {
      message.error(err)
    })
}
const changeVersion = (value) => {
  router.push({
    path: '/home/appDetail',
    query: {
      name: data.versionInfo.name,
      version: value
    }
  })
  getVersionInfo(data.versionInfo.name, value)
}
const changeMode = (mode) => {
  if (mode === 'tree') {
    treeList.value.show(data.versionInfo.name, data.selectedVersion)
    tiledList.value.hide()
  }
  if (mode === 'tiled') {
    treeList.value.hide()
    tiledList.value.show(data.versionInfo.name, data.selectedVersion)
  }
}
const filterOption = (input, option) => {
  return option.value.toLowerCase().indexOf(input.toLowerCase()) >= 0
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
