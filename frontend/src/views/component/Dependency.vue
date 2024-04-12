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
          <!-- <div v-if="data.mode === 'tiled'">
            <a-input-search
              v-model:value="data.search.name"
              placeholder="请输入组件名称"
              style="width: 250px"></a-input-search>
            <a-button type="primary" style="margin-left: 10px"><ExportOutlined />导出Excel</a-button>
          </div> -->
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
import { GetVersionList, GetVersionInfo } from '@/api/frontend'
import TreeList from './components/TreeList.vue'
import TiledList from './components/TiledList.vue'
import { message } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()
onMounted(async () => {
  data.component = route.query
  changeMode('tree')
})

const treeList = ref()
const tiledList = ref()
const data = reactive({
  component: {},
  mode: 'tree',
  search: {
    name: ''
  }
})
const back = () => {
  router.back()
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
