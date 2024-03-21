<template>
  <a-modal v-model:open="data.open">
    <template #title>
      <div style="font-size: 20px">删除组件</div>
    </template>
    <div class="content">
      <WarningOutlined :style="{ fontSize: '30px', color: '#ff4d4f' }" />
      <div style="margin-left: 15px">删除后不可恢复！确定删除？</div>
    </div>
    <template #footer>
      <a-button class="cancel_btn" @click="close">取消</a-button>
      <a-button class="delete_btn" @click="deleteComponent()">删除</a-button>
    </template>
  </a-modal>
</template>

<script setup>
import { reactive, defineExpose, defineEmits } from 'vue'
import { DeleteProjectComponent } from '@/api/frontend'
import { WarningOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'

const emit = defineEmits(['success'])
const store = useStore()

const data = reactive({
  open: false,
  com: {},
  parentApp: {}
})
const open = (com, parentApp) => {
  data.open = true
  data.com.groupId = com.groupId || ''
  data.com.artifactId = com.artifactId || com.name
  data.com.version = com.version
  if (parentApp) {
    data.parentApp.parentName = parentApp.name
    data.parentApp.parentVersion = parentApp.version
  } else data.parentApp = {}
}
const close = () => {
  data.open = false
}
const deleteComponent = () => {
  DeleteProjectComponent({
    ...data.com,
    ...data.parentApp
  })
    .then((res) => {
      // console.log('DeleteProjectComponent', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      message.success('删除组件成功')
      data.open = false
      emit('success')
    })
    .catch((e) => {
      message.error(e)
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
