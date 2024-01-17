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
      <template #title="{ title, version, opensource }">
        <span style="font-weight: bold; margin-right: 10px">{{ title }}</span>
        <a-tag>{{ version }}</a-tag>
        <a-tag>{{ opensource ? '开源' : '闭源' }}</a-tag>
      </template>
    </a-tree>
    <Drawer ref="drawer"></Drawer>
  </div>
</template>

<script setup>
import { reactive, ref, defineExpose } from 'vue'
import { GetOpenComponentTree, GetCloseComponentTree } from '@/api/frontend'
import { DownOutlined, ExperimentOutlined } from '@ant-design/icons-vue'
import Drawer from '@/views/application/components/Drawer.vue'
import { message } from 'ant-design-vue'

const drawer = ref()
const data = reactive({
  visible: true,
  treeData: [],
  expandedKeys: [],
  selectedKeys: []
})
const show = (component) => {
  data.visible = true
  const params = {
    groupId: component.groupId,
    artifactId: component.artifactId,
    version: component.version
  }
  if (component.opensource) {
    GetOpenComponentTree(params)
      .then((res) => {
        // console.log('GetOpenComponentTree', res)
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
  } else {
    GetCloseComponentTree(params)
      .then((res) => {
        // console.log('GetCloseComponentTree', res)
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
  drawer.value.open(e.node)
  data.selectedKeys = []
}
defineExpose({ show, hide })
</script>

<style scoped>
/* 树形结构样式 */
:deep(.ant-tree .ant-tree-switcher-noop) {
  display: none !important;
}
:deep(.ant-tree .ant-tree-treenode) {
  height: 40px;
  align-items: center;
}
:deep(.ant-tree .ant-tree-switcher) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
}
:deep(.ant-tree-title) {
  margin-left: 5px;
  font-size: 16px;
}
:deep(.ant-tree-show-line .ant-tree-indent-unit:before) {
  inset-inline-end: 15px;
}
:deep(.ant-tree-show-line .ant-tree-indent-unit-end:before) {
  display: inline-block;
}
:deep(.ant-tree-treenode-switcher-open > .ant-tree-show-line .ant-tree-indent-unit:after) {
  position: absolute;
  top: 50%;
  right: 0;
  width: 50%;
  height: 1px;
  background-color: #d9d9d9;
  content: '';
}
:deep(.ant-tree .ant-tree-indent-unit) {
  width: 30px;
}
</style>
