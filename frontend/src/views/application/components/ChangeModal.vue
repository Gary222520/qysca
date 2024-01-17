<template>
  <a-modal v-model:open="data.open" width="800px" :footer="null">
    <template #title>
      <div style="font-size: 20px">更新版本信息</div>
    </template>
    <div style="display: flex; margin: 20px 0">
      <a-steps class="steps" direction="vertical" :current="data.currentStep" :items="data.steps"></a-steps>
      <div v-if="data.currentStep === 0">
        <div class="content">
          <a-card class="card" hoverable @click="selectTool('maven')">
            <div class="card_title">
              <img class="img" src="@/assets/maven.png" />
              <div class="name">Maven</div>
            </div>
            <div class="card_text">
              <ul style="margin-bottom: 0">
                <li class="list_item">maven构建并管理的项目</li>
                <li class="list_item">pom.xml记录依赖信息</li>
              </ul>
            </div>
          </a-card>
          <a-card class="card" hoverable @click="selectTool('zip')">
            <div class="card_title">
              <img class="img" src="@/assets/zip.png" />
              <div class="name">Zip</div>
            </div>
            <div class="card_text">
              <ul style="margin-bottom: 0">
                <li class="list_item">项目根目录的Zip压缩文件</li>
                <li class="list_item">扫描zip中的依赖文件</li>
              </ul>
            </div>
          </a-card>
        </div>
        <div class="content" style="margin-top: 10px">
          <a-card class="card" hoverable @click="selectTool('gradle')">
            <div class="card_title">
              <img class="img" src="@/assets/gradle.png" />
              <div class="name">Gradle</div>
            </div>
            <div class="card_text">
              <ul>
                <li class="list_item">gradle构建并管理的项目</li>
                <li class="list_item">settings.gradle记录依赖信息</li>
              </ul>
            </div>
          </a-card>
          <a-card class="card" hoverable @click="selectTool('jar')">
            <div class="card_title">
              <img class="img" src="@/assets/jar.png" />
              <div class="name">Jar</div>
            </div>
            <div class="card_text">
              <ul style="margin-bottom: 0">
                <li class="list_item">项目构建完成的jar包</li>
                <li class="list_item">扫描jar包中的依赖文件</li>
              </ul>
            </div>
          </a-card>
        </div>
      </div>
      <div v-if="data.currentStep === 1">
        <a-form :model="formState" ref="formRef" name="project" :label-col="{ span: 8 }">
          <a-form-item label="项目名称" name="name">
            <a-input v-model:value="formState.name" style="width: 300px" disabled />
          </a-form-item>
          <a-form-item label="版本编号" name="version">
            <a-input v-model:value="formState.version" style="width: 300px" disabled />
          </a-form-item>
          <a-form-item label="备注信息" name="note">
            <a-input v-model:value="formState.note" style="width: 300px" />
          </a-form-item>
        </a-form>
      </div>
      <div v-if="data.currentStep === 2">
        <div class="upload">
          <div v-if="projectInfo.builder === 'maven'">
            <Upload ref="uploadRef" :accept="'.xml'" :upload-text="'pom.xml'" @success="handleUpload"></Upload>
          </div>
          <div v-if="projectInfo.builder === 'zip'">
            <Upload ref="uploadRef" :accept="'.zip'" :upload-text="'.zip文件'" @success="handleUpload"></Upload>
          </div>
        </div>
      </div>
    </div>
    <div class="button">
      <a-button class="cancel-btn" @click="close">取消</a-button>
      <a-button class="btn" v-if="data.currentStep === 1" @click="next">下一步</a-button>
      <a-button class="btn" v-if="data.currentStep === 2" @click="submit">提交</a-button>
      <a-button class="btn" v-if="data.currentStep > 0" @click="back">上一步</a-button>
    </div>
  </a-modal>
</template>

<script setup>
import { reactive, ref, defineExpose } from 'vue'
import Upload from './Upload.vue'
import { UpdateVersion } from '@/api/frontend'
import { message } from 'ant-design-vue'

const uploadRef = ref()
const data = reactive({
  open: false,
  record: {},
  currentStep: 0,
  steps: [{ title: '选择工具' }, { title: '项目信息' }, { title: '上传文件' }]
})
const formRef = ref()
const formState = reactive({
  name: '',
  version: '',
  note: ''
})
const projectInfo = reactive({
  language: data.record.language,
  builder: '',
  filePath: '',
  scanner: ''
})
const open = (record) => {
  data.record = record
  formState.name = record.name
  formState.version = record.version
  formState.note = record.note
  data.open = true
}
const close = () => {
  data.open = false
}
const clear = () => {
  uploadRef.value.clear()
}
const handleUpload = (uploadInfo) => {
  projectInfo.filePath = uploadInfo.filePath
  projectInfo.scanner = uploadInfo.scanner
}
const selectTool = (builder) => {
  if (builder === 'gradle' || builder === 'jar') {
    message.info('暂未支持该方式')
    return
  }
  projectInfo.builder = builder
  next()
}
const back = () => {
  data.currentStep -= 1
}
const next = () => {
  data.currentStep += 1
}
const submit = () => {
  formRef.value
    .validate()
    .then(() => {
      const params = {
        ...formState,
        ...projectInfo,
        language: data.record.language,
        builder: data.record.builder
      }
      UpdateVersion(params)
        .then((res) => {
          console.log('UpdateVersion', res)
          message.success('版本更新成功')
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
<style scoped src="@/atdv/radio-btn.css"></style>
<style scoped src="@/atdv/steps.css"></style>
