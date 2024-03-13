<template>
  <div class="main">
    <div class="title">用户管理</div>
    <a-card class="content_card">
      <div class="operations">
        <a-button type="primary" @click="addUser"><UserAddOutlined />创建用户</a-button>
      </div>
    </a-card>
    <AddUserModal ref="addUserModal"></AddUserModal>
  </div>
</template>

<script setup>
import { UserAddOutlined } from '@ant-design/icons-vue'
import AddUserModal from './components/AddUserModal.vue'
import { reactive, ref } from 'vue'

const addUserModal = ref()

const data = reactive({})

const addUser = () => {
  addUserModal.value.open()
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
</style>
<style scoped src="@/atdv/primary-btn.css"></style>
