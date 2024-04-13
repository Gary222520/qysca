<template>
  <div class="main">
    <a-card class="header_card">
      <div class="card_header">
        <LeftOutlined :style="{ fontSize: '18px' }" @click="back" />
        <div style="margin-left: 10px; font-weight: bold">组件依赖信息</div>
      </div>
      <div class="card_title">
        组件名称：
        <div style="font-weight: bold; margin-right: 30px">{{ data.component.name }}</div>
        版本：
        <div style="margin-right: 30px">{{ data.component.version }}</div>
        语言：
        <div style="margin-right: 30px">
          <div v-if="data.component.language instanceof Array">
            <a-tag v-for="(item, index) in data.component.language" :key="index">{{ item }}</a-tag>
          </div>
          <div v-else>
            <a-tag>{{ data.component.language }}</a-tag>
          </div>
        </div>
      </div>
    </a-card>
    <a-card class="content_card">
      <a-tabs v-model:activeKey="data.activeKey">
        <a-tab-pane key="1" :tab="`组件（${count.component}）`" forceRender>
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
            </div>
            <div style="margin-top: 20px">
              <TreeList ref="treeList"></TreeList>
              <TiledList ref="tiledList"></TiledList>
            </div>
          </div>
        </a-tab-pane>
        <a-tab-pane key="2" :tab="`漏洞（${count.vulnerability}）`" forceRender>
          <div class="content">
            <Vulnerablity ref="vulnerablity" @setCount="(data) => setCount(data)"></Vulnerablity>
          </div>
        </a-tab-pane>
        <a-tab-pane key="3" :tab="`许可证（${count.license}）`" forceRender>
          <div class="content">
            <License ref="license" @setCount="(data) => setCount(data)"></License>
          </div>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, defineExpose } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LeftOutlined, ApartmentOutlined, UnorderedListOutlined, ExportOutlined } from '@ant-design/icons-vue'
import { GetVersionList, GetVersionInfo } from '@/api/frontend'
import TreeList from './components/TreeList.vue'
import TiledList from './components/TiledList.vue'
import Vulnerablity from '@/views/project/components/Vulnerability.vue'
import License from '@/views/project/components/License.vue'
import { message } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()
onMounted(async () => {
  data.component = route.query
  data.component.language = JSON.parse(data.component.language)
  getCount(data.component.name, data.component.version, data.component.language)
  changeMode('tree')
})

const treeList = ref()
const tiledList = ref()
const vulnerablity = ref()
const license = ref()

const data = reactive({
  activeKey: '1',
  component: {},
  mode: 'tree',
  search: {
    name: ''
  }
})
const count = reactive({
  component: 0,
  vulnerability: 0,
  license: 0
})

const back = () => {
  router.back()
}
const getCount = async (name, version, language) => {
  await tiledList.value.getCount(name, version, language).then((res) => {
    count.component = res
  })
  vulnerablity.value.show(name, version, false)
  license.value.show(name, version, false)
}
const setCount = ({ type, value }) => {
  count[type] = value
}

const changeMode = (mode) => {
  data.mode = mode
  if (mode === 'tree') {
    treeList.value.show(data.component)
    tiledList.value.hide()
  }
  if (mode === 'tiled') {
    treeList.value.hide()
    tiledList.value.show(data.component)
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
  height: 100px;
}
.content_card {
  position: absolute;
  width: 100%;
  height: calc(100% - 100px);
  top: 115px;
  overflow-y: scroll;
}
.card_header {
  display: flex;
  align-items: center;
  font-size: 18px;
  margin-top: 15px;
}
.card_title {
  display: flex;
  align-items: center;
  margin-top: 5px;
  font-size: 16px;
}
.content {
  margin-top: 10px;
}
.content_header {
  display: flex;
  justify-content: space-between;
}
:deep(.ant-card .ant-card-body) {
  padding: 4px 20px;
}
</style>
<style scoped src="@/atdv/primary-btn.css"></style>
<style scoped src="@/atdv/input.css"></style>
<style scoped src="@/atdv/input-search.css"></style>
<style scoped src="@/atdv/radio-btn.css"></style>
<style scoped src="@/atdv/select.css"></style>
<style scoped src="@/atdv/tab.css"></style>
