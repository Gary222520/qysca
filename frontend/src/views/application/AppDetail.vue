<template>
  <div class="main">
    <a-card class="header_card">
      <div class="card_header">
        <LeftOutlined :style="{ fontSize: '18px' }" @click="back" />
        <div style="margin-left: 10px; font-weight: bold">项目详情</div>
      </div>
      <div class="card_title">
        项目名称：
        <div style="font-weight: bold; margin-right: 30px">{{ data.project.name }}</div>
        版本编号：
        <div style="margin-right: 30px">
          <a-select
            v-model:value="data.selectedVersion"
            :options="data.versionOptions"
            style="width: 100px"
            @change="changeVersion"></a-select>
        </div>
        最近一次扫描时间：
        <div>{{ data.version.time }}</div>
      </div>
    </a-card>
    <a-card class="content_card">
      <div class="content">
        <div class="content_header">
          <a-radio-group v-model:value="data.mode">
            <a-tooltip>
              <template #title>树形展示</template>
              <a-radio-button value="tree" @click="changeMode('tree')">
                <ApartmentOutlined :style="{ fontSize: '16px' }" />
              </a-radio-button>
            </a-tooltip>
            <a-tooltip>
              <template #title>平铺展示</template>
              <a-radio-button value="tiled" @click="changeMode('tiled')">
                <UnorderedListOutlined :style="{ fontSize: '16px' }" />
              </a-radio-button>
            </a-tooltip>
          </a-radio-group>
          <div v-if="data.mode === 'tiled'">
            <a-input-search
              v-model:value="data.search.name"
              placeholder="请输入组件名称"
              style="width: 250px"></a-input-search>
            <a-button class="export_btn"><ExportOutlined />导出Excel</a-button>
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
import { useStore } from 'vuex'
import TreeList from './components/TreeList.vue'
import TiledList from './components/TiledList.vue'

const router = useRouter()
const route = useRoute()
const store = useStore()
const treeList = ref()
const tiledList = ref()
const data = reactive({
  project: {},
  version: {},
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
const changeVersion = (value) => {
  data.version = data.project.data.find((item) => item.version === value)
  data.selectedVersion = value
  router.push({
    path: '/home/appDetail',
    query: {
      name: data.project.name,
      version: data.selectedVersion
    }
  })
}
const changeMode = (mode) => {
  if (mode === 'tree') {
    treeList.value.show()
    tiledList.value.hide()
  }
  if (mode === 'tiled') {
    treeList.value.hide()
    tiledList.value.show()
  }
}
onMounted(() => {
  const name = route.query.name
  const version = route.query.version
  data.project = store.getters.getProjectByName(name)
  data.version = data.project.data.find((item) => item.version === version)
  data.selectedVersion = version
  data.versionOptions = data.project.data.map((item) => {
    const option = {
      label: item.version,
      value: item.version,
      key: item.version
    }
    return option
  })
  changeMode('tree')
})
defineExpose({ open })
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
.export_btn {
  margin-left: 10px;
  background-color: #6f005f;
  color: #fff;
  border-color: #6f005f;
}
.export_btn:hover {
  background-color: #6f005f;
  color: #fff;
  border-color: #6f005f;
  opacity: 0.8;
}
/* 选择框样式 */
:deep(.ant-select-selector:hover) {
  border-color: #6f005f !important;
}
:deep(.ant-select-focused .ant-select-selector) {
  border-color: #6f005f !important;
  box-shadow: 0 0 0 2px rgba(111, 0, 95, 0.1) !important;
}
/* 选项按钮样式 */
:deep(.ant-radio-button-wrapper) {
  text-align: center;
  width: 60px;
}
:deep(.ant-radio-button-wrapper:hover) {
  color: #6f005f;
}
:deep(.ant-radio-button-wrapper-checked:not(.ant-radio-button-wrapper-disabled)) {
  color: #fff;
  border-color: #6f005f;
  background-color: #6f005f;
}
:deep(.ant-radio-button-wrapper-checked:not(.ant-radio-button-wrapper-disabled)::before) {
  background-color: #6f005f;
}
/* 输入框样式 */
:deep(.ant-input:hover) {
  border-color: #6f005f;
}
:deep(.ant-input:focus) {
  border-color: #6f005f;
  box-shadow: 0 0 0 2px rgba(111, 0, 95, 0.1);
}
/* 输入框搜索按钮样式 */
:deep(
    .ant-input-search
      > .ant-input-group
      > .ant-input-group-addon:last-child
      .ant-input-search-button:not(.ant-btn-primary)
  ) {
  width: 50px;
  background-color: #6f005f;
  color: #fff;
  border-color: #6f005f;
}
:deep(
    .ant-input-search
      > .ant-input-group
      > .ant-input-group-addon:last-child
      .ant-input-search-button:not(.ant-btn-primary):hover
  ) {
  opacity: 0.8;
}
</style>
