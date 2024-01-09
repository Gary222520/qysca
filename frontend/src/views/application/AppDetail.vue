<template>
  <div>
    <a-card style="height: 100%">
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
    <a-card style="height: 100%; margin-top: 10px">
      <div class="content">
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
        <div style="margin-top: 10px">
          <TreeList ref="treeList"></TreeList>
          <TiledList ref="tiledList"></TiledList>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LeftOutlined, ApartmentOutlined, UnorderedListOutlined } from '@ant-design/icons-vue'
import { useStore } from 'vuex'
import TreeList from './components/TreeList.vue'
import TiledList from './components/TiledList.vue'

export default {
  components: {
    LeftOutlined,
    ApartmentOutlined,
    UnorderedListOutlined,
    TreeList,
    TiledList
  },
  setup() {
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
      mode: 'tree'
    })
    const back = () => {
      router.push('/home/application')
    }
    const changeVersion = (value) => {
      data.version = data.project.data.find((item) => item.version === value)
      data.selectedVersion = value
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
      changeVersion(version)
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
    return { treeList, tiledList, data, back, changeVersion, changeMode }
  }
}
</script>

<style lang="less" scoped>
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
:deep(.ant-select-selector:hover) {
  border-color: #6f005f !important;
}
:deep(.ant-select-focused .ant-select-selector) {
  border-color: #6f005f !important;
  box-shadow: 0 0 0 2px rgba(111, 0, 95, 0.1) !important;
}
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
</style>
