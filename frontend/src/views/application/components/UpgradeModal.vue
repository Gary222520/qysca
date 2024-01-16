<template>
  <a-modal v-model:open="data.open" :footer="null">
    <template #title>
      <div style="font-size: 20px">版本升级</div>
    </template>
    <a-radio-group v-model:value="data.tab" style="margin-top: 15px; margin-left: 25px">
      <a-radio-button value="版本信息" style="width: 100px">版本信息</a-radio-button>
      <a-radio-button value="文件上传" style="width: 100px">文件上传</a-radio-button>
    </a-radio-group>
    <div v-show="data.tab === '版本信息'" style="margin-top: 25px">
      <a-form :model="formState" ref="formRef" name="project" :label-col="{ span: 5 }">
        <a-form-item label="项目名称" name="name">
          <a-input v-model:value="formState.name" style="width: 350px" disabled />
        </a-form-item>
        <a-form-item
          label="版本编号"
          name="version"
          :rules="[{ required: true, validator: validateVersion, trigger: 'change' }]">
          <a-input v-model:value="formState.version" :placeholder="data.versionPlaceholder" style="width: 350px" />
        </a-form-item>
        <a-form-item label="备注信息" name="note">
          <a-input v-model:value="formState.note" style="width: 350px" />
        </a-form-item>
      </a-form>
    </div>
    <div v-show="data.tab === '文件上传'" style="margin-top: 25px">
      <div class="upload">
        <div v-if="data.projectInfo.builder === 'maven'">
          <Upload ref="uploadRef" @success="handleUpload"></Upload>
        </div>
      </div>
    </div>
    <div class="button">
      <a-button class="cancel-btn" @click="close">取消</a-button>
      <a-button class="btn" @click="submit">提交</a-button>
    </div>
  </a-modal>
</template>

<script setup>
import { reactive, ref, defineExpose } from 'vue'
import { UpgradeVersion } from '@/api/frontend'
import Upload from './Upload.vue'
import { message } from 'ant-design-vue'

const uploadRef = ref()
const data = reactive({
  open: false,
  projectInfo: {},
  tab: '版本信息',
  versionPlaceholder: '',
  filePath: ''
})
const projectInfo = reactive({
  filePath: '',
  scanner: ''
})
const formRef = ref()
const formState = reactive({
  name: '',
  version: '',
  note: ''
})
const open = (project) => {
  data.projectInfo = {
    language: project.content[0].language,
    builder: project.content[0].builder
  }
  formState.name = project.name
  formState.version = ''
  formState.note = ''
  data.versionPlaceholder = `当前最新版本为${project.content[0].version}`
  data.open = true
}
const close = () => {
  data.open = false
}
const clear = () => {
  formState.version = ''
  formState.note = ''
  uploadRef.value.clear()
}
const handleUpload = (uploadInfo) => {
  projectInfo.filePath = uploadInfo.filePath
  projectInfo.scanner = uploadInfo.scanner
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
  formRef.value
    .validate()
    .then(() => {
      const params = {
        ...formState,
        ...data.projectInfo
      }
      UpgradeVersion(params)
        .then((res) => {
          console.log('UpgradeVersion', res)
          close()
          clear()
        })
        .catch((e) => {
          message.error(e)
        })
    })
    .catch(() => {
      data.tab = '版本信息'
    })
}
defineExpose({ open })
</script>

<style scoped>
.upload {
  margin: 0 25px;
  margin-bottom: 20px;
}
.button {
  display: flex;
  justify-content: right;
  margin-right: 25px;
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
<style scoped src="@/atdv/radio-btn.css"></style>
