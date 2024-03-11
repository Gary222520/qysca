<template>
  <a-modal v-model:open="data.open" :footer="null">
    <template #title>
      <div style="font-size: 20px">{{ data.upgrade ? '应用升级' : '新建应用' }}</div>
    </template>
    <div style="display: flex; margin-top: 20px">
      <a-form :model="formState" ref="formRef" name="application" :label-col="{ span: 8 }">
        <a-form-item label="组织ID" name="groupId" :rules="[{ required: true, message: '请输入应用groupId' }]">
          <a-input v-model:value="formState.groupId" style="width: 300px" :disabled="data.upgrade" />
        </a-form-item>
        <a-form-item label="工件ID" name="artifactId" :rules="[{ required: true, message: '请输入应用artifactId' }]">
          <a-input v-model:value="formState.artifactId" style="width: 300px" :disabled="data.upgrade" />
        </a-form-item>
        <a-form-item
          label="版本编号"
          name="version"
          :rules="[{ required: true, validator: validateVersion, trigger: 'change' }]">
          <a-input v-model:value="formState.version" style="width: 300px" :placeholder="data.versionPlaceholder" />
        </a-form-item>
        <a-form-item label="应用名称" name="name" :rules="[{ required: true, message: '请输入应用名称' }]">
          <a-input
            v-model:value="formState.name"
            :placeholder="formState.artifactId"
            :disabled="data.upgrade"
            style="width: 300px" />
        </a-form-item>
        <a-form-item label="应用描述" name="description">
          <a-input v-model:value="formState.description" style="width: 300px" />
        </a-form-item>
      </a-form>
    </div>
    <div class="button">
      <a-button class="cancel-btn" @click="close">取消</a-button>
      <a-button class="btn" @click="submit">提交</a-button>
    </div>
  </a-modal>
</template>

<script setup>
import { reactive, ref, defineExpose, defineEmits } from 'vue'
import { CreateApplication } from '@/api/frontend'
import { message } from 'ant-design-vue'

const emit = defineEmits(['success'])

const data = reactive({
  open: false,
  upgrade: false,
  versionPlaceholder: ''
})
const formRef = ref()
const formState = reactive({
  groupId: '',
  artifactId: '',
  version: '',
  name: '',
  description: ''
})
const open = (groupId, artifactId, version, name) => {
  data.open = true
  if (groupId || artifactId || version || name) {
    data.upgrade = true
    formState.groupId = groupId
    formState.artifactId = artifactId
    formState.version = ''
    formState.name = name
    data.versionPlaceholder = `当前版本为${version}`
  } else {
    data.upgrade = false
    formState.version = '1.0.0'
  }
}
const close = () => {
  data.open = false
}
const clear = () => {
  formState.groupId = ''
  formState.artifactId = ''
  formState.version = '1.0.0'
  formState.name = ''
  formState.description = ''
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
const submit = () => {
  if (formState.name === '') formState.name = formState.artifactId
  formRef.value
    .validate()
    .then(() => {
      const params = {
        ...formState
      }
      CreateApplication(params)
        .then((res) => {
          // console.log('CreateApplication', res)
          if (res.code !== 200) {
            message.error(res.message)
            return
          }
          message.success('添加应用成功')
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
<style scoped src="@/atdv/steps.css"></style>
<style scoped src="@/atdv/input.css"></style>
