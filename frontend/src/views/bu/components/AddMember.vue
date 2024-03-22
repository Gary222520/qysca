<template>
  <a-modal v-model:open="data.open" :footer="null">
    <template #title>
      <div style="font-size: 20px">添加部门成员</div>
    </template>
    <div style="display: flex; margin-top: 20px">
      <a-form :model="formState" ref="formRef" name="application" :label-col="{ span: 8 }">
        <a-form-item label="用户编号" name="uid" :rules="[{ required: true, message: '请输入用户编号' }]">
          <a-input v-model:value="formState.uid" style="width: 300px" />
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
import { AddBuMember } from '@/api/frontend'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'

const emit = defineEmits(['success'])
const store = useStore()

const data = reactive({
  open: false,
  bu: {}
})
const formRef = ref()
const formState = reactive({
  uid: ''
})
const open = (record) => {
  data.open = true
  data.bu = record
}
const close = () => {
  data.open = false
}
const clear = () => {
  formState.uid = ''
}
const submit = () => {
  formRef.value
    .validate()
    .then(() => {
      const params = {
        uid: formState.uid,
        buName: data.bu.name
      }
      AddBuMember(params)
        .then((res) => {
          // console.log('AddBuMember', res)
          if (res.code !== 200) {
            message.error(res.message)
            return
          }
          message.success('添加部门成员成功')
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
