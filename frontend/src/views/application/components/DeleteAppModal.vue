<template>
  <a-modal v-model:open="data.open">
    <template #title>
      <div style="font-size: 20px">删除项目版本</div>
    </template>
    <div class="content">
      <WarningOutlined :style="{ fontSize: '30px', color: '#ef0137' }" />
      <div style="margin-left: 15px">删除后不可恢复！确定删除？</div>
    </div>
    <template #footer>
      <a-button class="cancel_btn" @click="close">取消</a-button>
      <a-button class="delete_btn" @click="deleteProject()">删除</a-button>
    </template>
    <WarnModal ref="warnModal" @ok="close()"></WarnModal>
  </a-modal>
</template>

<script setup>
import { reactive, ref, defineExpose, defineEmits } from 'vue'
import { DeleteProject } from '@/api/frontend'
import { WarningOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'
import WarnModal from '@/components/WarnModal.vue'

const emit = defineEmits(['success'])
const store = useStore()
const warnModal = ref()

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
  emit('success')
}
const deleteProject = () => {
  DeleteProject({
    ...data.app,
    ...data.parentApp
  })
    .then((res) => {
      // console.log('DeleteProject', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      if (!res.data || res.data.length === 0) {
        data.open = false
        message.success('删除版本成功')
        emit('success')
      } else {
        data.open = false
        warnModal.value.open(res.data, '有以下应用依赖该版本：')
      }
    })
    .catch((e) => {
      console.error(e)
    })
}
defineExpose({ open })
</script>

<style scoped>
.cancel_btn:hover {
  border-color: #00557c;
  color: #00557c;
}
.delete_btn {
  margin-left: 10px;
  background-color: #ef0137;
  border-color: #ef0137;
  color: #fff;
}
.delete_btn:hover {
  opacity: 0.8;
  color: #fff;
  border-color: #ef0137;
}
.content {
  margin: 20px 0;
  display: flex;
  align-items: center;
  font-size: 16px;
  color: #ef0137;
}
</style>
