<template>
  <div class="main">
    <div class="login">
      <div class="title">SIEMENS SCA</div>
      <div class="form">
        <a-form :model="formState" ref="formRef" name="login" :label-col="{ span: 6 }" hideRequiredMark>
          <a-form-item label="用户编号" name="uid" :rules="[{ required: true, message: '请输入用户编号' }]">
            <a-input v-model:value="formState.uid" style="width: 300px" />
          </a-form-item>
          <a-form-item label="用户密码" name="password" :rules="[{ required: true, message: '请输入用户密码' }]">
            <a-input v-model:value="formState.password" style="width: 300px" type="password" />
          </a-form-item>
        </a-form>
      </div>
      <div class="btn">
        <a-button type="primary" style="width: 100px" @click="login">登录</a-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'

const router = useRouter()
const store = useStore()

const formRef = ref()
const formState = reactive({
  uid: '',
  password: ''
})
const data = reactive({})

const login = () => {
  formRef.value
    .validate()
    .then(() => {
      store
        .dispatch('login', { ...formState })
        .then((res) => {
          router.push('/home')
        })
        .catch((err) => {
          console.error(err)
        })
    })
    .catch(() => {})
}
</script>

<style scoped>
.main {
  position: absolute;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.login {
  position: relative;
  width: 500px;
  padding: 70px 50px;
  border-radius: 16px;
  box-shadow: 0 1px 2px -2px rgba(111, 0, 95, 0.16), 0 3px 6px 0 rgba(111, 0, 95, 0.12),
    0 5px 12px 4px rgba(111, 0, 95, 0.09);
}
.title {
  text-align: center;
  font-size: 36px;
  font-weight: bold;
  color: #6f005f;
  margin-bottom: 30px;
}
.btn {
  text-align: center;
}
:deep(.ant-form-item .ant-form-item-label > label) {
  font-size: 16px;
  font-weight: bold;
}
</style>
<style scoped src="@/atdv/input.css"></style>
<style scoped src="@/atdv/primary-btn.css"></style>
