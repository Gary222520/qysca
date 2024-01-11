<template>
  <div class="main">
    <div class="title">组件管理</div>
    <a-card class="content_card">
      <div class="operations">
        <a-button type="primary" @click="addComponent"><PlusOutlined />添加组件</a-button>
        <div>
          <a-input
            v-model:value="data.search.group"
            addon-before="groupId"
            placeholder="输入groupId..."
            style="width: 200px; margin-right: 10px"></a-input>
          <a-input
            v-model:value="data.search.artifact"
            addon-before="artifactId"
            placeholder="输入artifactId..."
            style="width: 200px; margin-right: 10px"></a-input>
          <a-input
            v-model:value="data.search.version"
            addon-before="version"
            placeholder="输入版本号..."
            style="width: 200px; margin-right: 10px"></a-input>
          <a-input v-model:value="data.search.name" placeholder="请输入组件名称" style="width: 200px"></a-input>
          <a-button type="primary" style="margin-left: 10px"><ExportOutlined />导出Excel</a-button>
        </div>
      </div>
      <a-table :data-source="data.datasource" :columns="data.columns" bordered>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <div class="column_name" @click="showInfo(record)">{{ record.name }}</div>
          </template>
        </template>
      </a-table>
      <Drawer ref="drawer"></Drawer>
    </a-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { PlusOutlined, ExportOutlined } from '@ant-design/icons-vue'
import Drawer from '../application/components/Drawer.vue'

const drawer = ref()
const data = reactive({
  search: {
    name: '',
    group: '',
    artifact: '',
    version: ''
  },
  datasource: [
    { name: '组件1', version: '2.4.3', language: 'java', depend: '直接依赖' },
    { name: '组件2', version: '2.4.3', language: 'java', depend: '间接依赖' },
    { name: '组件3', version: '5.4.3', language: 'java', depend: '间接依赖' },
    { name: '组件4', version: '0.2.6', language: 'java', depend: '直接依赖' },
    { name: '组件5', version: '1.2.5', language: 'java', depend: '间接依赖' },
    { name: '组件6', version: '1.18.20', language: 'java', depend: '间接依赖' },
    { name: '组件7', version: '2.1.3', language: 'java', depend: '直接依赖' }
  ],
  columns: [
    { title: '组件名称', dataIndex: 'name', key: 'name' },
    { title: '版本', dataIndex: 'version', key: 'version' },
    { title: '语言', dataIndex: 'language', key: 'language' },
    { title: '依赖方式', dataIndex: 'depend', key: 'depend' }
  ]
})
const addComponent = () => {}
const showInfo = (record) => {
  drawer.value.open({ name: record.name })
}
</script>

<style scoped>
.main {
  position: relative;
  height: 100%;
}
.title {
  font-weight: bold;
  font-size: 24px;
  margin-bottom: 15px;
}
.content_card {
  position: absolute;
  width: 100%;
  height: calc(100% - 32px);
  overflow-y: scroll;
}
.operations {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.column_name {
  cursor: pointer;
}
.column_name:hover {
  color: #6f005f;
}
</style>
<style scoped src="@/atdv/primary-btn.css"></style>
<style scoped src="@/atdv/input.css"></style>
<style scoped src="@/atdv/input-search.css"></style>
<style scoped src="@/atdv/delete-btn.css"></style>
<style scoped src="@/atdv/pagination.css"></style>
