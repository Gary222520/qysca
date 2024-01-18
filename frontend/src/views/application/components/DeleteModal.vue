<template>
  <a-modal v-model:open="data.open">
    <template #title>
      <div style="font-size: 20px">删除项目</div>
    </template>
    <div class="content">
      <WarningOutlined :style="{ fontSize: '30px', color: '#ff4d4f' }" />
      <div style="margin-left: 15px">删除项目后不可恢复！确定删除？</div>
    </div>
    <template #footer>
      <a-button class="cancel_btn" @click="close">取消</a-button>
      <a-button class="delete_btn" @click="deleteProject">删除</a-button>
    </template>
  </a-modal>
</template>

<script setup>
import { reactive, defineExpose, defineEmits } from 'vue'
import { DeleteProject } from '@/api/frontend'
import { WarningOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'

const emit = defineEmits(['success'])
const data = reactive({
  open: false,
  project: {}
})
const open = (project) => {
  data.open = true
  data.project = project
}
const close = () => {
  data.open = false
}
const deleteProject = () => {
  const params = {
    name: data.project.name
  }
  DeleteProject(params)
    .then((res) => {
      // console.log('DeleteProject', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      data.open = false
      message.success('删除项目成功')
      emit('success')
    })
    .catch((err) => {
      console.error(err)
    })
}
defineExpose({ open })
</script>

<style scoped>
.cancel_btn:hover {
  border-color: #6f005f;
  color: #6f005f;
}
.delete_btn {
  margin-left: 10px;
  background-color: #ff4d4f;
  border-color: #ff4d4f;
  color: #fff;
}
.delete_btn:hover {
  opacity: 0.8;
  color: #fff;
  border-color: #ff4d4f;
}
.content {
  margin: 20px 0;
  display: flex;
  align-items: center;
  font-size: 16px;
  color: #ff4d4f;
}
</style>
