<template>
  <a-modal v-model:open="data.open" :footer="null">
    <template #title>
      <div style="font-size: 20px">新增应用</div>
    </template>
    <div style="display: flex; margin-top: 20px">
      <a-form :model="formState" ref="formRef" name="application" :label-col="{ span: 8 }">
        <a-form-item label="应用名称" name="name" :rules="[{ required: true, message: '请输入应用名称' }]">
          <a-input v-model:value="formState.name" :placeholder="formState.artifactId" style="width: 300px" />
        </a-form-item>
        <a-form-item
          label="版本编号"
          name="version"
          :rules="[{ required: true, validator: validateVersion, trigger: 'change' }]">
          <a-input v-model:value="formState.version" style="width: 300px" />
        </a-form-item>
        <a-form-item label="应用类型" name="type" :rules="[{ required: true, message: '请选择应用类型' }]">
          <a-select v-model:value="formState.type" style="width: 300px">
            <a-select-option value="agent">agent</a-select-option>
            <a-select-option value="backend">backend</a-select-option>
            <a-select-option value="collector">collector</a-select-option>
            <a-select-option value="filter">filter</a-select-option>
            <a-select-option value="raw storage">raw storage</a-select-option>
            <a-select-option value="web UI">web UI</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="应用描述" name="description">
          <a-input v-model:value="formState.description" style="width: 300px" />
        </a-form-item>
      </a-form>
    </div>
    <div class="button">
      <a-button class="cancel-btn" @click="close">取消</a-button>
      <a-button class="btn" @click="submit(false)">新建</a-button>
      <a-button class="btn" @click="submit(true)">新建并添加依赖信息</a-button>
    </div>
  </a-modal>
</template>

<script setup>
import { reactive, ref, defineExpose, defineEmits } from 'vue'
import { AddProject } from '@/api/frontend'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'

const emit = defineEmits(['success'])
const store = useStore()

const data = reactive({
  open: false
})
const formRef = ref()
const formState = reactive({
  version: '1.0.0',
  name: '',
  type: 'agent',
  description: ''
})
const open = (parentId, id) => {
  data.open = true
  if (parentId) formState.parentId = parentId
  else delete formState.parentId
  if (id) formState.id = id
  else delete formState.id
}
const close = () => {
  data.open = false
}
const clear = () => {
  formRef.value.resetFields()
}
const validateVersion = async (_rule, value) => {
  const reg = /^[1-9]\d?(\.([1-9]?\d)){2}$/
  if (value === '') {
    return Promise.reject(new Error('请输入版本编号'))
  } else if (!reg.test(value)) {
    return Promise.reject(new Error('版本编号格式为x.x.x（1-99.0-99.0-99）'))
  } else {
    return Promise.resolve()
  }
}
const submit = (dep) => {
  if (formState.name === '') formState.name = formState.artifactId
  formRef.value
    .validate()
    .then(() => {
      const params = {
        ...formState,
        buName: JSON.parse(sessionStorage.getItem('user')).userBuAppRoles[0].buName
      }
      AddProject(params)
        .then((res) => {
          // console.log('AddProject', res)
          if (res.code !== 200) {
            message.error(res.message)
            return
          }
          message.success('新增应用成功')
          data.open = false
          if (dep) emit('success', params)
          else emit('success')
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
}
.btn {
  min-width: 80px;
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
