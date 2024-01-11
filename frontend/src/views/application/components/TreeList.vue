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
      <template #icon="{ isLeaf }">
        <template v-if="isLeaf"><FileOutlined :style="{ fontSize: '16px' }" /></template>
        <template v-if="!isLeaf"><FolderOpenOutlined :style="{ fontSize: '16px' }" /></template>
      </template>
    </a-tree>
    <Drawer ref="drawer"></Drawer>
  </div>
</template>

<script setup>
import { reactive, ref, defineExpose } from 'vue'
import { DownOutlined, FolderOpenOutlined, FileOutlined } from '@ant-design/icons-vue'
import Drawer from './Drawer.vue'

const drawer = ref()
const data = reactive({
  visible: true,
  treeData: [
    {
      title: 'parent 1',
      key: '0-0',
      isLeaf: false,
      children: [
        {
          title: 'parent 1-0',
          key: '0-0-0',
          isLeaf: false,
          children: [
            { title: 'leaf', key: '0-0-0-0', isLeaf: true },
            { title: 'leaf', key: '0-0-0-1', isLeaf: true },
            { title: 'leaf', key: '0-0-0-2', isLeaf: true }
          ]
        },
        {
          title: 'parent 1-1',
          key: '0-0-1',
          isLeaf: false,
          children: [{ title: 'leaf', key: '0-0-1-0', isLeaf: true }]
        },
        {
          title: 'parent 1-2',
          key: '0-0-2',
          isLeaf: false,
          children: [
            { title: 'leaf', key: '0-0-2-0', isLeaf: true },
            { title: 'leaf', key: '0-0-2-1', isLeaf: true }
          ]
        }
      ]
    },
    {
      title: 'parent 2',
      key: '0-1',
      isLeaf: false,
      children: [
        {
          title: 'parent 2-0',
          key: '0-1-0',
          isLeaf: false,
          children: [
            { title: 'leaf', key: '0-1-0-0', isLeaf: true },
            { title: 'leaf', key: '0-1-0-1', isLeaf: true },
            { title: 'leaf', key: '0-1-0-2', isLeaf: true }
          ]
        },
        {
          title: 'parent 2-1',
          key: '0-1-1',
          isLeaf: false,
          children: [{ title: 'leaf', key: '0-1-1-0', isLeaf: true }]
        },
        {
          title: 'parent 2-2',
          key: '0-1-2',
          isLeaf: false,
          children: [
            { title: 'leaf', key: '0-1-2-0', isLeaf: true },
            { title: 'leaf', key: '0-1-2-1', isLeaf: true }
          ]
        }
      ]
    }
  ],
  expandedKeys: [],
  selectedKeys: []
})
const show = () => {
  data.visible = true
}
const hide = () => {
  data.visible = false
}
const selectNode = (selectedKeys, e) => {
  drawer.value.open({ name: e.node.title })
  // data.selectedKeys = []
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
