<template>
  <div style="height: 90%">
    <div class="title">项目管理</div>
    <a-card class="card" style="height: 100%">
      <div class="content">
        <a-button @click="addProject"><PlusOutlined />新建项目</a-button>
        <a-table :data-source="data.datasource" :columns="data.columns" bordered style="margin-top: 10px"></a-table>
      </div>
    </a-card>
    <AddModal ref="addModal"></AddModal>
  </div>
</template>

<script>
import { PlusOutlined } from '@ant-design/icons-vue'
import { reactive, ref } from 'vue'
import AddModal from './components/AddModal.vue'
export default {
  components: {
    PlusOutlined,
    AddModal
  },
  setup() {
    const addModal = ref()
    const data = reactive({
      datasource: [
        { name: '项目1', version: '3.0', language: 'java', time: 'xxx', comment: 'xxxxx' },
        { name: '项目1', version: '2.0', language: 'java', time: 'xxx', comment: 'xxxxx' },
        { name: '项目1', version: '1.0', language: 'java', time: 'xxx', comment: 'xxxxx' },
        { name: '项目2', version: '2.0', language: 'python', time: 'xxx', comment: 'xxxxx' },
        { name: '项目2', version: '1.0', language: 'python', time: 'xxx', comment: 'xxxxx' }
      ],
      columns: [
        {
          title: '项目名称',
          dataIndex: 'name',
          key: 'name',
          customCell: (_, index) => {
            const project = data.projects.find((item) => item.index === index)
            if (project) {
              return { rowSpan: project.rowSpan }
            } else {
              return { rowSpan: 0 }
            }
          }
        },
        { title: '版本', dataIndex: 'version', key: 'version' },
        { title: '语言', dataIndex: 'language', key: 'language' },
        { title: '最近一次扫描时间', dataIndex: 'time', key: 'time' },
        { title: '备注', dataIndex: 'comment', key: 'comment' },
        { title: '操作', dataIndex: 'action', key: 'action' }
      ],
      projects: [
        { index: 0, rowSpan: 3 },
        { index: 3, rowSpan: 2 }
      ]
    })
    const addProject = () => {
      addModal.value.open()
    }
    return { addModal, data, addProject }
  }
}
</script>

<style lang="less" scoped>
.title {
  font-weight: bold;
  font-size: 24px;
  margin-bottom: 15px;
}
.card {
  height: 100%;
}
:deep(.ant-btn) {
  border: 0;
  background-color: #6f005f;
  color: #fff;
}
:deep(.ant-btn):hover {
  opacity: 0.8;
  color: #fff;
}
</style>
