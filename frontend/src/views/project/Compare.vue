<template>
  <div class="main">
    <a-card class="header_card">
      <div class="card_header">
        <LeftOutlined :style="{ fontSize: '18px' }" @click="back" />
        <div style="margin-left: 10px; font-weight: bold">版本对比</div>
      </div>
      <div class="card_title">
        当前版本：
        <div style="margin-right: 30px">
          <a-select
            v-model:value="data.currentVersion"
            show-search
            :options="data.versionOptions"
            :filter-option="filterOption"
            style="width: 100px"
            @change="changeVersion"></a-select>
        </div>
        对比版本：
        <div style="margin-right: 30px">
          <a-select
            v-model:value="data.compareVersion"
            show-search
            :options="data.versionOptions"
            :filter-option="filterOption"
            style="width: 100px"
            @change="changeVersion"></a-select>
        </div>
      </div>
    </a-card>
    <a-card class="content_card">
      <div class="content">
        <!-- <div class="content_header">
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
        </div> -->
        <div>
          <CompareTree ref="compareTree"></CompareTree>
          <!-- <TiledList ref="tiledList"></TiledList> -->
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, defineExpose } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LeftOutlined, ApartmentOutlined, UnorderedListOutlined, ExportOutlined } from '@ant-design/icons-vue'
import { GetVersionList } from '@/api/frontend'
import CompareTree from './components/CompareTree.vue'
import TiledList from './components/TiledList.vue'
import { message } from 'ant-design-vue'

onMounted(async () => {
  // data.groupId = route.query.groupId
  // data.artifactId = route.query.artifactId
  data.name = route.query.name
  data.currentVersion = route.query.currentVersion || ''
  data.compareVersion = route.query.compareVersion || ''
  await getVersions(data.name)
  compare()
})

const router = useRouter()
const route = useRoute()
const compareTree = ref()
const data = reactive({
  groupId: '',
  artifactId: '',
  currentVersion: '',
  compareVersion: '',
  versionOptions: [],
  mode: 'tree',
  search: {
    name: ''
  }
})
const back = () => {
  router.back()
}
const getVersions = async (name) => {
  await GetVersionList({ name })
    .then((res) => {
      // console.log('GetVersionList', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      data.versionOptions = res.data?.map((option) => {
        return { label: option, value: option, key: option }
      })
      if (data.versionOptions && data.versionOptions.length > 0) {
        if (data.currentVersion === '') data.currentVersion = data.versionOptions[0].value
        if (data.compareVersion === '') {
          const index = data.versionOptions.findIndex((option) => option.value === data.currentVersion)
          data.compareVersion = data.versionOptions[index + 1]?.value || data.versionOptions[index]?.value
        }
      }
    })
    .catch((err) => {
      console.error(err)
    })
}
const changeVersion = (value) => {
  compare()
}
const compare = () => {
  compareTree.value.show(data.name, data.currentVersion, data.compareVersion)
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
