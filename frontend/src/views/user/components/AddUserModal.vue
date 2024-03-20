<template>
  <a-modal v-model:open="data.open" :footer="null">
    <template #title>
      <div style="font-size: 20px">创建用户</div>
    </template>
    <div style="display: flex; margin-top: 20px">
      <a-form :model="formState" ref="formRef" name="application" :label-col="{ span: 8 }">
        <a-form-item label="用户编号" name="uid" :rules="[{ required: true, message: '请输入用户编号' }]">
          <a-input v-model:value="formState.uid" style="width: 300px" />
        </a-form-item>
        <a-form-item label="用户名称" name="name" :rules="[{ required: true, message: '请输入用户名称' }]">
          <a-input v-model:value="formState.name" style="width: 300px" />
        </a-form-item>
        <a-form-item
          label="密码"
          name="password"
          :rules="[{ required: true, validator: validatePassword, trigger: 'change' }]">
          <a-input v-model:value="formState.password" style="width: 300px" type="password" />
        </a-form-item>
        <a-form-item
          label="确认密码"
          name="confirmPass"
          :rules="[{ required: true, validator: validateConfirmPass, trigger: 'change' }]">
          <a-input v-model:value="formState.confirmPass" style="width: 300px" type="password" />
        </a-form-item>
        <a-form-item label="邮箱" name="email" :rules="[{ required: true, message: '请输入邮箱' }]">
          <a-input v-model:value="formState.email" style="width: 300px" />
        </a-form-item>
        <a-form-item label="手机号" name="phone" :rules="[{ required: true, message: '请输入手机号' }]">
          <a-input v-model:value="formState.phone" style="width: 300px" />
        </a-form-item>
        <a-form-item label="角色" name="role" :rules="[{ required: true, message: '请选择角色' }]">
          <a-select v-model:value="formState.role" :options="selection.options" style="width: 300px">
            <!-- <a-select-option value="Bu Rep">Bu Rep</a-select-option>
            <a-select-option value="Bu PO">Bu PO</a-select-option>
            <a-select-option value="App Leader">App Leader</a-select-option>
            <a-select-option value="App Member">App Member</a-select-option>
            <a-select-option value="Admin">Admin</a-select-option> -->
          </a-select>
        </a-form-item>
      </a-form>
    </div>
    <div class="button">
      <a-button class="cancel-btn" @click="close">取消</a-button>
      <a-button class="btn" @click="submit">创建</a-button>
    </div>
  </a-modal>
</template>

<script setup>
import { reactive, ref, defineExpose, defineEmits, onMounted } from 'vue'
import { Register } from '@/api/frontend'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'

const emit = defineEmits(['success'])
const store = useStore()

onMounted(() => {
  selection.options = selection[store.getters.role]
  formState.role = selection.options[0]
})

const data = reactive({
  open: false
})
const selection = reactive({
  Admin: [
    { label: 'Bu Rep', value: 'Bu Rep' },
    { label: 'Bu PO', value: 'Bu PO' }
  ],
  'Bu PO': [{ label: 'App Leader', value: 'App Leader' }],
  'App Leader': [
    { label: 'App Leader', value: 'App Leader' },
    { label: 'App Member', value: 'App Member' }
  ],
  options: []
})
const formRef = ref()
const formState = reactive({
  uid: '',
  name: '',
  password: '',
  confirmPass: '',
  email: '',
  phone: '',
  role: ''
})
const open = () => {
  data.open = true
}
const close = () => {
  data.open = false
}
const clear = () => {
  formState.uid = ''
  formState.name = ''
  formState.password = ''
  formState.confirmPass = ''
  formState.email = ''
  formState.phone = ''
  formState.role = ''
}
const validatePassword = async (_rule, value) => {
  if (value === '') return Promise.reject(new Error('请输入密码'))
  if (formState.confirmPass !== '') formRef.value.validateFields('confirmPass')
  else return Promise.resolve()
}
const validateConfirmPass = async (_rule, value) => {
  if (value === '') return Promise.reject(new Error('请再次输入密码'))
  if (formState.password === value) return Promise.resolve()
  else return Promise.reject(new Error('两次密码不一致'))
}
const submit = () => {
  formRef.value
    .validate()
    .then(() => {
      const params = {
        ...formState
      }
      Register(params)
        .then((res) => {
          // console.log('Register', res)
          if (res.code !== 200) {
            message.error(res.message)
            return
          }
          message.success('创建用户成功')
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
.steps {
  width: 150px;
}
.content {
  width: 600px;
  display: flex;
}
.card {
  width: 50%;
  margin-right: 10px;
}
.card_title {
  display: flex;
  align-items: center;
}
.img {
  height: 60px;
  margin-left: 10px;
}
.name {
  font-size: 28px;
  font-weight: bold;
  margin-left: 20px;
}
.card_text {
  margin-top: 10px;
}
.list_item {
  margin-top: 5px;
}
.upload {
  width: 500px;
  margin-left: 40px;
  margin-bottom: 20px;
}
.button {
  display: flex;
  justify-content: right;
  margin-right: 10px;
}
.btn {
  width: 80px;
  margin-left: 10px;
  border: #6f005f;
  background-color: #6f005f;
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
  border-color: #6f005f;
  color: #6f005f;
}
</style>
<style scoped src="@/atdv/input.css"></style>
<style scoped src="@/atdv/select.css"></style>
