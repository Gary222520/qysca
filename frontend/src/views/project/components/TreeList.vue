<template>
  <div v-if="data.visible">
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
      <template #title="{ title, version, type }">
        <span style="font-weight: bold; margin-right: 10px">{{ title }}</span>
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
import { GetProjectTree } from '@/api/frontend'
import { DownOutlined, ExperimentOutlined } from '@ant-design/icons-vue'
import Drawer from './Drawer.vue'
import { message } from 'ant-design-vue'

const drawer = ref()
const data = reactive({
  visible: true,
  treeData: [],
  expandedKeys: ['0-0'],
  selectedKeys: [],
  language: ''
})
const show = (name, version) => {
  data.visible = true
  GetProjectTree({ name, version })
    .then((res) => {
      // console.log('GetProjectTree', res)
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
      title: item?.name,
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
  data.selectedKeys = []
  if (e.node.key === '0-0') return
  drawer.value.open(e.node, false)
}
defineExpose({ show, hide })
</script>

<style scoped></style>
<style scoped src="@/atdv/tree.css"></style>
