<template>
  <a-modal v-model:open="data.open" :footer="null">
    <template #title>
      <div style="font-size: 20px">撤销Bu PO</div>
    </template>
    <div style="display: flex; margin-top: 20px">
      <a-form :model="formState" ref="formRef" name="application" :label-col="{ span: 8 }">
        <a-form-item label="应用名称" name="name" :rules="[{ required: true, message: '请输入应用名称' }]">
          <a-input v-model:value="formState.name" style="width: 300px" />
        </a-form-item>
        <a-form-item label="应用版本" name="version" :rules="[{ required: true, message: '请输入应用版本' }]">
          <a-input v-model:value="formState.version" style="width: 300px" />
        </a-form-item>
      </a-form>
    </div>
    <div class="button">
      <a-button class="cancel-btn" @click="close">取消</a-button>
      <a-button class="btn" @click="submit">撤销</a-button>
    </div>
  </a-modal>
</template>

<script setup>
import { reactive, ref, defineExpose, defineEmits, onMounted } from 'vue'
import { DeleteAppMember } from '@/api/frontend'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'

const emit = defineEmits(['success'])
const store = useStore()

const data = reactive({
  open: false,
  record: {}
})
const formRef = ref()
const formState = reactive({
  name: '',
  version: ''
})
const open = (record) => {
  data.open = true
  data.record = record
}
const close = () => {
  data.open = false
}
const clear = () => {
  formState.name = ''
  formState.version = ''
}
const submit = () => {
  formRef.value
    .validate()
    .then(() => {
      const params = {
        ...formState,
        uid: data.record.uid,
        role: 'Bu PO'
      }
      DeleteAppMember(params)
        .then((res) => {
          // console.log('DeleteAppMember', res)
          if (res.code !== 200) {
            message.error(res.message)
            return
          }
          message.success('撤销Bu PO成功')
          data.open = false
          emit('success')
          setTimeout(() => {
            clear()
          }, 500)
        })
        .catch((e) => {
          message.error(e)
        })
    })
    .catch(() => {})
}
defineExpose({ open })
</script>

<style lang="less" scoped>
.button {
  display: flex;
  justify-content: right;
  margin-right: 10px;
}
.btn {
  width: 80px;
  margin-left: 10px;
  border: #00557c;
  background-color: #00557c;
  color: #fff;
}
.btn:hover {
  opacity: 0.8;
  color: #fff;
}
.cancel-btn {
  width: 80px;
  margin-left: 10px;
}
.cancel-btn:hover {
  opacity: 0.8;
  border-color: #00557c;
  color: #00557c;
}
</style>
<style scoped src="@/atdv/input.css"></style>
<style scoped src="@/atdv/select.css"></style>
