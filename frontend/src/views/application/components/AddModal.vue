<template>
  <a-modal v-model:open="data.open" width="800px" :footer="null">
    <template #title>
      <div style="font-size: 20px">新建项目</div>
    </template>
    <div style="display: flex; margin-top: 20px">
      <a-steps class="steps" direction="vertical" :current="data.currentStep" :items="data.steps"></a-steps>
      <div v-if="data.currentStep === 0">
        <div class="content">
          <a-card class="card" hoverable @click="selectLanguage('java')">
            <div class="card_title">
              <img class="img" src="@/assets/java.png" />
              <div class="name">Java</div>
            </div>
            <div class="card_text">
              <ul style="margin-bottom: 0">
                <li class="list_item">Java语言开发的软件</li>
                <li class="list_item">maven或gradle管理的项目</li>
              </ul>
            </div>
          </a-card>
          <a-card class="card" hoverable @click="selectLanguage('python')">
            <div class="card_title">
              <img class="img" src="@/assets/python.png" />
              <div class="name">Python</div>
            </div>
            <div class="card_text">
              <ul>
                <li class="list_item">Python语言开发的软件</li>
                <li class="list_item">pip工具管理的项目</li>
              </ul>
            </div>
          </a-card>
        </div>
        <div class="content" style="margin-top: 10px">
          <a-card class="card" hoverable @click="selectLanguage('go')">
            <div class="card_title">
              <img class="img" src="@/assets/go.png" />
              <div class="name">Go</div>
            </div>
            <div class="card_text">
              <ul style="margin-bottom: 0">
                <li class="list_item">Go语言开发的软件</li>
                <li class="list_item">Go.mod工具管理的项目</li>
              </ul>
            </div>
          </a-card>
          <a-card class="card" hoverable @click="selectLanguage('javascript')">
            <div class="card_title">
              <img class="img" src="@/assets/javascript.png" />
              <div class="name">JavaScript</div>
            </div>
            <div class="card_text">
              <ul style="margin-bottom: 0">
                <li class="list_item">JavaScript语言开发的软件</li>
                <li class="list_item">npm工具管理的NodeJS项目</li>
              </ul>
            </div>
          </a-card>
        </div>
      </div>
      <div v-if="data.currentStep === 1">
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
        </div>
        <div class="content" style="margin-top: 10px">
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
      </div>
      <div v-if="data.currentStep === 2">
        <a-form :model="formState" ref="formRef" name="project" :label-col="{ span: 8 }">
          <a-form-item label="项目名称" name="name" :rules="[{ required: true, message: '请输入项目名称' }]">
            <a-input v-model:value="formState.name" style="width: 300px" />
          </a-form-item>
          <a-form-item
            label="版本编号"
            name="version"
            :rules="[{ required: true, validator: validateVersion, trigger: 'change' }]">
            <a-input v-model:value="formState.version" style="width: 300px" />
          </a-form-item>
          <a-form-item label="备注信息" name="comment">
            <a-input v-model:value="formState.comment" style="width: 300px" />
          </a-form-item>
        </a-form>
      </div>
      <div v-if="data.currentStep === 3">
        <div class="upload">
          <div v-if="data.tool === 'maven'">
            <Upload ref="uploadRef"></Upload>
          </div>
        </div>
      </div>
    </div>
    <div class="button">
      <a-button class="cancel-btn" @click="close">取消</a-button>
      <a-button class="btn" v-if="data.currentStep === 2" @click="validateForm">下一步</a-button>
      <a-button class="btn" v-if="data.currentStep === 3" @click="submit">提交</a-button>
      <a-button class="btn" v-if="data.currentStep > 0" @click="back">上一步</a-button>
    </div>
  </a-modal>
</template>

<script setup>
import { reactive, ref, defineExpose } from 'vue'
import { AddProject } from '@/api/frontend'
import { CloudUploadOutlined } from '@ant-design/icons-vue'
import Upload from './Upload.vue'

const uploadRef = ref()
const data = reactive({
  open: false,
  currentStep: 0,
  steps: [{ title: '选择语言' }, { title: '选择工具' }, { title: '项目信息' }, { title: '上传文件' }],
  language: 'java',
  tool: 'maven'
})
const formRef = ref()
const formState = reactive({
  name: '',
  version: '1.0.0',
  comment: ''
})
const open = () => {
  data.open = true
}
const close = () => {
  data.open = false
}
const clear = () => {
  data.currentStep = 0
  formState.name = ''
  formState.version = '1.0.0'
  formState.comment = ''
  uploadRef.value.clear()
}
const selectLanguage = (language) => {
  data.language = language
  next()
}
const selectTool = (tool) => {
  data.tool = tool
  next()
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
const validateForm = () => {
  formRef.value
    .validate()
    .then(() => {
      next()
    })
    .catch(() => {})
}
const back = () => {
  data.currentStep -= 1
}
const next = () => {
  data.currentStep += 1
}
const submit = () => {
  data.open = false
  setTimeout(() => {
    clear()
  }, 500)
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
