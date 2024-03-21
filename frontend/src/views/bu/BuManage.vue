<template>
  <div class="main">
    <div class="title">部门管理</div>
    <a-card class="content_card">
      <div class="operations">
        <a-button type="primary" @click="addBu()"><PlusOutlined />创建部门</a-button>
      </div>
      <div class="table">
        <a-table :data-source="table.datasource" :columns="table.columns" bordered :pagination="false">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'action'">
              <a-tooltip>
                <template #title>成员详情</template>
                <FileTextOutlined
                  :style="{ fontSize: '18px', color: '#6f005f', marginRight: '10px' }"
                  @click="showInfo(record)" />
              </a-tooltip>
              <a-tooltip>
                <template #title>删除</template>
                <a-popconfirm v-model:open="record.popconfirm" title="确定删除这个部门吗？">
                  <template #cancelButton>
                    <a-button class="cancel_btn" size="small" @click="record.popconfirm = false">取消</a-button>
                  </template>
                  <template #okButton>
                    <a-button danger type="primary" size="small" @click="deleteBu(record)">删除</a-button>
                  </template>
                  <DeleteOutlined :style="{ fontSize: '18px', color: '#ff4d4f', marginRight: '10px' }" />
                </a-popconfirm>
              </a-tooltip>
            </template>
          </template>
          <template #emptyText>暂无部门</template>
        </a-table>
      </div>
    </a-card>
    <AddBuModal ref="addBuModal" @success="getBuList()"></AddBuModal>
    <BuMember ref="buMember"></BuMember>
  </div>
</template>

<script setup>
import { PlusOutlined, FileTextOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import AddBuModal from './components/AddBuModal.vue'
import BuMember from './components/BuMember.vue'
import { reactive, ref, onMounted } from 'vue'
import { GetBuList, DeleteBu } from '@/api/frontend'
import { message } from 'ant-design-vue'

onMounted(() => {
  getBuList()
})

const addBuModal = ref()
const buMember = ref()

const data = reactive({})

const table = reactive({
  datasource: [],
  columns: [
    { title: '部门名称', dataIndex: 'name', key: 'name', width: 150 },
    { title: '操作', dataIndex: 'action', key: 'action', width: 150 }
  ]
})

const getBuList = () => {
  GetBuList()
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      // console.log('GetBuList', res)
      table.datasource = res.data.map((item) => {
        return { name: item }
      })
    })
    .catch((err) => {
      console.error(err)
    })
}

const addBu = () => {
  addBuModal.value.open()
}

const showInfo = (record) => {
  buMember.value.open(record)
}

const deleteBu = (record) => {
  record.popconfirm = false
  DeleteBu({ name: record.name })
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      // console.log('DeleteBu', res)
      getBuList()
    })
    .catch((err) => {
      console.error(err)
    })
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
}
.table {
  width: 300px;
  margin-top: 20px;
}
.cancel_btn:hover {
  border-color: #6f005f;
  color: #6f005f;
}
</style>
<style scoped src="@/atdv/primary-btn.css"></style>
