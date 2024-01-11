<template>
  <a-modal v-model:open="data.open" :footer="null">
    <template #title>
      <div style="font-size: 20px">更新版本信息</div>
    </template>
    <a-radio-group v-model:value="data.tab" style="margin-top: 15px; margin-left: 25px">
      <a-radio-button value="版本信息">版本信息</a-radio-button>
      <a-radio-button value="文件重传">文件重传</a-radio-button>
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
          <a-input v-model:value="formState.version" style="width: 350px" />
        </a-form-item>
        <a-form-item label="备注信息" name="comment">
          <a-input v-model:value="formState.comment" style="width: 350px" />
        </a-form-item>
      </a-form>
    </div>
    <div v-show="data.tab === '文件重传'" style="margin-top: 25px">
      <div class="upload">
        <a-upload
          v-if="data.record.tool === 'maven'"
          class="uploader"
          name="maven"
          action="https://www.mocky.io/v2/5cc8019d300000980a055e76"
          accept=".xml"
          v-model:file-list="upload.fileList"
          :progress="upload.progress"
          :max-count="1"
          @change="(info) => handleUpload(info, 'maven')">
          <div style="height: calc(100% - 45px); padding-top: 45px">
            <CloudUploadOutlined :style="{ fontSize: '30px' }" />
            <div class="upload_text">pom.xml</div>
          </div>
        </a-upload>
        <a-upload v-if="data.tool === 'jar'" class="uploader">
          <div style="height: calc(100% - 45px); padding-top: 45px">
            <CloudUploadOutlined :style="{ fontSize: '30px' }" />
            <div class="upload_text">jar包</div>
          </div>
        </a-upload>
        <a-upload v-if="data.tool === 'zip'" class="uploader">
          <div style="height: calc(100% - 45px); padding-top: 45px">
            <CloudUploadOutlined :style="{ fontSize: '30px' }" />
            <div class="upload_text">zip文件</div>
          </div>
        </a-upload>
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
import { CloudUploadOutlined } from '@ant-design/icons-vue'

const data = reactive({
  open: false,
  record: {},
  tab: '版本信息'
})
const formRef = ref()
const formState = reactive({
  name: '',
  version: '',
  comment: ''
})
const upload = reactive({
  fileList: [],
  progress: {
    strokeColor: {
      '0%': '#108ee9',
      '100%': '#87d068'
    },
    strokeWidth: 3,
    format: (percent) => `${parseFloat(percent.toFixed(2))}%`,
    class: 'test'
  }
})
const open = (project, record) => {
  data.record = record
  formState.name = project.name
  formState.version = record.version
  formState.comment = record.comment
  data.open = true
}
const close = () => {
  data.open = false
}
const handleUpload = (info, type) => {}
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
      close()
    })
    .catch(() => {
      data.tab = '版本信息'
    })
}
defineExpose({ open })
</script>

<style scoped>
/* 选项按钮样式 */
:deep(.ant-radio-button-wrapper) {
  text-align: center;
  width: 100px;
}
:deep(.ant-radio-button-wrapper:hover) {
  color: #6f005f;
}
:deep(.ant-radio-button-wrapper-checked:not(.ant-radio-button-wrapper-disabled)) {
  color: #fff;
  border-color: #6f005f;
  background-color: #6f005f;
}
:deep(.ant-radio-button-wrapper-checked:not(.ant-radio-button-wrapper-disabled)::before) {
  background-color: #6f005f;
}
.upload {
  display: flex;
  align-items: center;
  margin-left: 25px;
  margin-bottom: 20px;
}
/* 上传文件样式 */
:deep(.ant-upload-wrapper .ant-upload-select) {
  width: 150px;
  height: 150px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  background-color: rgba(0, 0, 0, 0.02);
  margin-right: 25px;
  cursor: pointer;
}
:deep(.ant-upload-wrapper .ant-upload-list .ant-upload-list-item-container) {
  width: 250px;
  margin-right: 10px;
}
.upload_text {
  margin-top: 8px;
  color: #666;
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
