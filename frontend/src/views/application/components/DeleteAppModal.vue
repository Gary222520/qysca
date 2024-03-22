<template>
  <a-modal v-model:open="data.open">
    <template #title>
      <div style="font-size: 20px">删除项目版本</div>
    </template>
    <div class="content">
      <WarningOutlined :style="{ fontSize: '30px', color: '#ff4d4f' }" />
      <div style="margin-left: 15px">删除后不可恢复！确定删除？</div>
    </div>
    <template #footer>
      <a-button class="cancel_btn" @click="close">取消</a-button>
      <a-button class="delete_btn" @click="deleteProject()">删除</a-button>
    </template>
  </a-modal>
</template>

<script setup>
import { reactive, defineExpose, defineEmits } from 'vue'
import { DeleteProject } from '@/api/frontend'
import { WarningOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'

const emit = defineEmits(['success'])
const store = useStore()

const data = reactive({
  open: false,
  app: {},
  parentApp: {}
})
const open = (app, parentApp) => {
  data.open = true
  data.app.name = app.name
  data.app.version = app.version
  if (parentApp) {
    data.parentApp.parentName = parentApp.name
    data.parentApp.parentVersion = parentApp.version
  } else data.parentApp = {}
}
const close = () => {
  data.open = false
}
const deleteProject = () => {
  DeleteProject({
    ...data.app,
    ...data.parentApp
  })
    .then((res) => {
      console.log('DeleteProject', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      if (!res.data || res.data.length === 0) message.success('删除版本成功')
      else {
        let text = '无法删除！有以下应用依赖该版本：'
        res.data.forEach((item) => {
          text += item.name + '-' + item.version + ';'
        })
        message.error(text)
      }
      data.open = false
      emit('success')
    })
    .catch((e) => {
      console.log(e)
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
