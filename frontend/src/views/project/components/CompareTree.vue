<template>
  <div v-if="data.visible">
    <div style="display: flex; align-items: center; margin-left: 10px; margin-bottom: 10px">
      <div style="background-color: #1677ff; width: 15px; height: 15px"></div>
      <div style="margin-left: 5px; margin-right: 20px; color: #1677ff">新增</div>
      <div style="background-color: #389e0d; width: 15px; height: 15px"></div>
      <div style="margin-left: 5px; margin-right: 20px; color: #389e0d">更新</div>
      <div style="background-color: #ff4d4f; width: 15px; height: 15px"></div>
      <div style="margin-left: 5px; margin-right: 20px; color: #ff4d4f">删除</div>
      <div style="background-color: #000000; width: 15px; height: 15px"></div>
      <div style="margin-left: 5px; margin-right: 20px; color: #000000">不变</div>
    </div>
    <a-tree
      v-model:expandedKeys="data.expandedKeys"
      v-model:selectedKeys="data.selectedKeys"
      show-line
      show-icon
      :tree-data="data.treeData"
      @select="selectNode">
      <template #switcherIcon="{ switcherCls }">
        <DownOutlined :class="switcherCls" :style="{ fontSize: '14px' }" />
      </template>
      <template #icon>
        <ExperimentOutlined :style="{ fontSize: '16px' }" />
      </template>
      <template #title="{ title, version, type, mark }">
        <span v-if="mark === 'CHANGE'" style="font-weight: bold; margin-right: 10px; color: #389e0d">{{ title }}</span>
        <span v-if="mark === 'ADD'" style="font-weight: bold; margin-right: 10px; color: #1677ff">{{ title }}</span>
        <span v-if="mark === 'DELETE'" style="font-weight: bold; margin-right: 10px; color: #ff4d4f">{{ title }}</span>
        <span v-if="mark === 'SAME'" style="font-weight: bold; margin-right: 10px">{{ title }}</span>
        <a-tag>{{ version }}</a-tag>
        <a-tag v-if="type === 'opensource'">开源</a-tag>
        <a-tag v-if="type === 'business'">商用</a-tag>
        <a-tag v-if="type === 'internal'">内部</a-tag>
      </template>
    </a-tree>
    <Drawer ref="drawer"></Drawer>
  </div>
</template>

<script setup>
import { reactive, ref, defineExpose } from 'vue'
import { GetCompareTree } from '@/api/frontend'
import { DownOutlined, ExperimentOutlined } from '@ant-design/icons-vue'
import Drawer from './Drawer.vue'
import { message } from 'ant-design-vue'

const drawer = ref()
const data = reactive({
  visible: true,
  treeData: [],
  expandedKeys: ['0-0'],
  selectedKeys: []
})
const show = (name, toVersion, fromVersion) => {
  if (fromVersion === toVersion) {
    message.info('两个版本不能相同')
    return
  }
  data.visible = true
  GetCompareTree({ name, toVersion, fromVersion })
    .then((res) => {
      // console.log('GetCompareTree', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      const resData = res.data
      data.treeData = []
      if (resData) {
        data.treeData = createTree([resData.tree], '0')
      }
    })
    .catch((err) => {
      console.error(err)
    })
}
const createTree = (arr, preKey) => {
  if (!arr) return []
  const treeData = []
  arr.forEach((item, index) => {
    const key = `${preKey}-${index}`
    const data = {
      ...item,
      title: item?.artifactId,
      key,
      children: createTree(item?.dependencies, key)
    }
    if (data.children.length === 0) delete data.children
    delete data.dependencies
    treeData.push(data)
  })
  return treeData
}
const hide = () => {
  data.visible = false
}
const selectNode = (selectedKeys, e) => {
  drawer.value.open(e.node, false)
  data.selectedKeys = []
}
defineExpose({ show, hide })
</script>

<style scoped></style>
<style scoped src="@/atdv/tree.css"></style>
