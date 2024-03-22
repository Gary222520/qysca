<template>
  <a-modal v-model:open="data.open" :footer="null">
    <template #title>
      <div style="font-size: 20px">更新用户信息</div>
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
        <!-- <a-form-item label="角色" name="role" :rules="[{ required: true, message: '请选择角色' }]">
          <a-select v-model:value="formState.role" :options="selection.options" style="width: 300px"></a-select>
        </a-form-item> -->
      </a-form>
    </div>
    <div class="button">
      <a-button class="cancel-btn" @click="close">取消</a-button>
      <a-button class="btn" @click="submit">更新</a-button>
    </div>
  </a-modal>
</template>

<script setup>
import { reactive, ref, defineExpose, defineEmits, onMounted } from 'vue'
import { UpdateUser } from '@/api/frontend'
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
  uid: '',
  name: '',
  password: '',
  confirmPass: '',
  email: '',
  phone: ''
})
const open = (record) => {
  data.open = true
  data.record = record
  formState.uid = record.uid
  formState.name = record.name
  formState.password = ''
  formState.confirmPass = ''
  formState.email = record.email
  formState.phone = record.phone
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
        ...formState,
        id: data.record.id
      }
      delete params.confirmPass
      UpdateUser(params)
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
