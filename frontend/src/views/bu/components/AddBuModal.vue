<template>
  <a-modal v-model:open="data.open" :footer="null">
    <template #title>
      <div style="font-size: 20px">创建部门</div>
    </template>
    <div style="display: flex; margin-top: 20px">
      <a-form :model="formState" ref="formRef" name="application" :label-col="{ span: 8 }">
        <a-form-item label="部门编号" name="bid" :rules="[{ required: true, message: '请输入部门编号' }]">
          <a-input v-model:value="formState.bid" style="width: 300px" />
        </a-form-item>
        <a-form-item label="部门名称" name="name" :rules="[{ required: true, message: '请输入部门名称' }]">
          <a-input v-model:value="formState.name" style="width: 300px" />
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
import { reactive, ref, defineExpose, defineEmits } from 'vue'
import { CreateBu } from '@/api/frontend'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'

const emit = defineEmits(['success'])
const store = useStore()

const data = reactive({
  open: false
})
const formRef = ref()
const formState = reactive({
  bid: '',
  name: ''
})
const open = () => {
  data.open = true
}
const close = () => {
  data.open = false
}
const clear = () => {
  formState.name = ''
  formState.bid = ''
}
const submit = () => {
  formRef.value
    .validate()
    .then(() => {
      CreateBu({ name: formState.name, bid: formState.bid })
        .then((res) => {
          // console.log('CreateBu', res)
          if (res.code !== 200) {
            message.error(res.message)
            return
          }
          message.success('创建部门成功')
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
