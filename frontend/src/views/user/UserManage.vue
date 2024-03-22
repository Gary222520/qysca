<template>
  <div class="main">
    <div class="title">用户管理</div>
    <a-card class="content_card">
      <div class="operations">
        <a-button type="primary" @click="addUser"><UserAddOutlined />创建用户</a-button>
      </div>
      <div class="table">
        <a-table :data-source="table.datasource" :columns="table.columns" bordered :pagination="pagination">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'action'">
              <a-tooltip>
                <template #title>更新</template>
                <SyncOutlined
                  :style="{ fontSize: '18px', color: '#6f005f', marginRight: '10px' }"
                  @click="updateUser(record)" />
              </a-tooltip>
              <a-tooltip>
                <template #title>删除</template>
                <a-popconfirm v-model:open="record.popconfirm" title="确定删除该用户吗？">
                  <template #cancelButton>
                    <a-button class="cancel_btn" size="small" @click="record.popconfirm = false">取消</a-button>
                  </template>
                  <template #okButton>
                    <a-button danger type="primary" size="small" @click="deleteUser(record)">删除</a-button>
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
    <AddUserModal ref="addUserModal" @success="getUserList()"></AddUserModal>
    <UpdateUserModal ref="updateUserModal" @success="getUserList()"></UpdateUserModal>
  </div>
</template>

<script setup>
import { UserAddOutlined, SyncOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import AddUserModal from './components/AddUserModal.vue'
import UpdateUserModal from './components/UpdateUserModal.vue'
import { reactive, ref, onMounted } from 'vue'
import { GetUserList, DeleteUser } from '@/api/frontend'
import { message } from 'ant-design-vue'

onMounted(() => {
  getUserList()
})

const addUserModal = ref()
const updateUserModal = ref()

const data = reactive({})

const table = reactive({
  datasource: [],
  columns: [
    { title: '用户编号', dataIndex: 'uid', key: 'uid', width: 150 },
    { title: '用户姓名', dataIndex: 'name', key: 'name', width: 150 },
    { title: '用户邮箱', dataIndex: 'email', key: 'email', width: 150 },
    { title: '用户手机', dataIndex: 'phone', key: 'phone', width: 150 },
    { title: '操作', dataIndex: 'action', key: 'action', width: 150 }
  ]
})

const pagination = reactive({
  current: 1,
  total: 0,
  pageSize: 10,
  showSizeChanger: false,
  onChange: (page, size) => {
    pagination.current = page
    getUserList(page, size)
  },
  hideOnSinglePage: true
})

const getUserList = (number = 1, size = 10) => {
  GetUserList({ number, size })
    .then((res) => {
      // console.log('GetUserList', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      table.datasource = res.data.content
    })
    .catch((err) => {
      console.error(err)
    })
}

const addUser = () => {
  addUserModal.value.open()
}

const updateUser = (record) => {
  updateUserModal.value.open(record)
}

const deleteUser = (record) => {
  record.popconfirm = false
  DeleteUser({ uid: record.uid })
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      console.log('DeleteUser', res)
      getUserList()
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
  width: 750px;
  margin-top: 20px;
}
.cancel_btn:hover {
  border-color: #6f005f;
  color: #6f005f;
}
</style>
<style scoped src="@/atdv/primary-btn.css"></style>
