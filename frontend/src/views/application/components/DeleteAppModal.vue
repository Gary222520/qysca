<template>
  <a-modal v-model:open="data.open">
    <template #title>
      <div style="font-size: 20px">{{ data.all ? '删除应用' : '删除该版本' }}</div>
    </template>
    <div class="content">
      <WarningOutlined :style="{ fontSize: '30px', color: '#ff4d4f' }" />
      <div style="margin-left: 15px">删除后不可恢复！确定删除？</div>
    </div>
    <template #footer>
      <a-button class="cancel_btn" @click="close">取消</a-button>
      <a-button class="delete_btn" @click="deleteProject">删除</a-button>
    </template>
  </a-modal>
</template>

<script setup>
import { reactive, defineExpose, defineEmits } from 'vue'
import { DeleteApplication, DeleteApplicationVersion } from '@/api/frontend'
import { WarningOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'

const emit = defineEmits(['success'])
const store = useStore()

const data = reactive({
  open: false,
  all: false,
  currentApp: {}
})
const open = (all) => {
  data.open = true
  data.all = all
  data.currentApp = store.getters.currentApp
}
const close = () => {
  data.open = false
}
const deleteProject = () => {
  if (data.all) {
    DeleteApplication({ groupId: data.currentApp.groupId, artifactId: data.currentApp.artifactId })
      .then((res) => {
        // console.log('DeleteApplication', res)
        if (res.code !== 200) {
          message.error(res.message)
          return
        }
        message.success('删除应用成功')
        data.open = false
        emit('success')
      })
      .catch((e) => {
        message.error(e)
      })
  } else {
    DeleteApplicationVersion({
      groupId: data.currentApp.groupId,
      artifactId: data.currentApp.artifactId,
      version: data.currentApp.version
    })
      .then((res) => {
        // console.log('DeleteApplicationVersion', res)
        if (res.code !== 200) {
          message.error(res.message)
          return
        }
        message.success('删除版本成功')
        data.open = false
        emit('success')
      })
      .catch((e) => {
        message.error(e)
      })
  }
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
