<template>
  <a-modal v-model:open="data.open" :footer="null">
    <template #title>
      <div style="font-size: 20px">添加应用成员</div>
    </template>
    <div style="display: flex; margin-top: 20px">
      <a-form :model="formState" ref="formRef" name="application" :label-col="{ span: 8 }">
        <a-form-item label="用户编号" name="uid" :rules="[{ required: true, message: '请输入用户编号' }]">
          <a-input v-model:value="formState.uid" style="width: 300px" />
        </a-form-item>
        <a-form-item label="角色" name="role" :rules="[{ required: true, message: '请选择用户编号' }]">
          <a-select v-model:value="formState.role" style="width: 300px">
            <a-select-option v-if="permit('App Leader')" value="App Leader">App Leader</a-select-option>
            <a-select-option v-if="permit('App Member')" value="App Member">App Member</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </div>
    <div class="button">
      <a-button class="cancel-btn" @click="close">取消</a-button>
      <a-button class="btn" @click="submit">添加</a-button>
    </div>
  </a-modal>
</template>

<script setup>
import { reactive, ref, defineExpose, defineEmits, onMounted } from 'vue'
import { AddAppMember } from '@/api/frontend'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'

const emit = defineEmits(['success'])
const store = useStore()

const data = reactive({
  open: false,
  app: {}
})
const formRef = ref()
const formState = reactive({
  uid: '',
  role: ''
})
const open = (app) => {
  data.open = true
  data.app = app
}
const close = () => {
  data.open = false
}
const clear = () => {
  formState.uid = ''
}
const permit = (role) => {
  const permission = JSON.parse(sessionStorage.getItem('user')).userBuAppRoles
  if (role === 'App Member') return permission.some((item) => item.role === 'App Leader')
  return true
}
const submit = () => {
  formRef.value
    .validate()
    .then(() => {
      const params = {
        name: data.app.name,
        version: data.app.version,
        ...formState
      }
      AddAppMember(params)
        .then((res) => {
          // console.log('AddAppMember', res)
          if (res.code !== 200) {
            message.error(res.message)
            return
          }
          message.success('添加应用成员成功')
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
