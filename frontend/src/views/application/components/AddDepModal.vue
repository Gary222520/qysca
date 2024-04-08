<template>
  <a-modal v-model:open="data.open" width="800px" :footer="null">
    <template #title>
      <div style="font-size: 20px">{{ data.add ? '添加项目依赖信息' : '更新项目依赖信息' }}</div>
    </template>
    <div style="display: flex; margin-top: 20px">
      <a-steps class="steps" direction="vertical" :current="data.currentStep" :items="data.steps"></a-steps>
      <div v-if="data.currentStep === 0">
        <Language @select="(lang) => selectLanguage(lang)"></Language>
      </div>
      <div v-if="data.currentStep === 1">
        <JavaBuilder v-if="projectInfo.language === 'java'" @select="(builder) => selectBuilder(builder)"></JavaBuilder>
        <PythonBuilder
          v-if="projectInfo.language === 'python'"
          @select="(builder) => selectBuilder(builder)"></PythonBuilder>
        <GoBuilder v-if="projectInfo.language === 'golang'" @select="(builder) => selectBuilder(builder)"></GoBuilder>
        <JSBuilder
          v-if="projectInfo.language === 'javaScript'"
          @select="(builder) => selectBuilder(builder)"></JSBuilder>
      </div>
      <div v-if="data.currentStep === 2">
        <div class="upload" v-if="projectInfo.language === 'java'">
          <div v-if="projectInfo.builder === 'maven'">
            <Upload ref="uploadRef" :accept="'.xml'" :upload-text="'pom.xml'" @success="handleUpload"></Upload>
          </div>
          <div v-if="projectInfo.builder === 'gradle'">
            <Upload ref="uploadRef" :accept="'.zip'" :upload-text="'.zip文件'" @success="handleUpload"></Upload>
          </div>
          <div v-if="projectInfo.builder === 'zip'">
            <Upload ref="uploadRef" :accept="'.zip'" :upload-text="'.zip文件'" @success="handleUpload"></Upload>
          </div>
          <div v-if="projectInfo.builder === 'jar'">
            <Upload ref="uploadRef" :accept="'.jar'" :upload-text="'jar包'" @success="handleUpload"></Upload>
          </div>
        </div>
        <div class="upload" v-if="projectInfo.language === 'python'">
          <div v-if="projectInfo.builder === 'zip'">
            <Upload ref="uploadRef" :accept="'.zip'" :upload-text="'.zip文件'" @success="handleUpload"></Upload>
          </div>
          <div v-if="projectInfo.builder === 'txt'">
            <Upload ref="uploadRef" :accept="'.txt'" :upload-text="'.txt文件'" @success="handleUpload"></Upload>
          </div>
          <div v-if="projectInfo.builder === 'tar.gz'">
            <Upload ref="uploadRef" :accept="'.gz'" :upload-text="'tar.gz文件'" @success="handleUpload"></Upload>
          </div>
        </div>
        <div class="upload" v-if="projectInfo.language === 'golang'">
          <div v-if="projectInfo.builder === 'go.mod'">
            <Upload ref="uploadRef" :accept="'.mod'" :upload-text="'go.mod'" @success="handleUpload"></Upload>
          </div>
          <div v-if="projectInfo.builder === 'zip'">
            <Upload ref="uploadRef" :accept="'.zip'" :upload-text="'.zip文件'" @success="handleUpload"></Upload>
          </div>
        </div>
        <div class="upload" v-if="projectInfo.language === 'javaScript'">
          <div v-if="projectInfo.builder === 'package.json'">
            <Upload ref="uploadRef" :accept="'.json'" :upload-text="'package.json'" @success="handleUpload"></Upload>
          </div>
          <div v-if="projectInfo.builder === 'zip'">
            <Upload ref="uploadRef" :accept="'.zip'" :upload-text="'.zip文件'" @success="handleUpload"></Upload>
          </div>
        </div>
      </div>
    </div>
    <div class="button">
      <a-button class="cancel-btn" @click="close">取消</a-button>
      <a-button class="btn" v-if="data.currentStep === 2" @click="submit">提交</a-button>
      <a-button class="btn" v-if="data.currentStep > 0" @click="back">上一步</a-button>
    </div>
  </a-modal>
</template>

<script setup>
import { reactive, ref, defineExpose, defineEmits } from 'vue'
import { AddDependency } from '@/api/frontend'
import Upload from '@/components/Upload.vue'
import Language from '@/components/Language.vue'
import JavaBuilder from '@/components/builder/JavaBuilder.vue'
import PythonBuilder from '@/components/builder/PythonBuilder.vue'
import GoBuilder from '@/components/builder/GoBuilder.vue'
import JSBuilder from '@/components/builder/JSBuilder.vue'
import { message } from 'ant-design-vue'

const emit = defineEmits(['success'])
const uploadRef = ref()
const data = reactive({
  open: false,
  add: true,
  currentStep: 0,
  steps: [{ title: '选择语言' }, { title: '选择工具' }, { title: '上传文件' }]
})
const projectInfo = reactive({
  name: '',
  version: '',
  language: '',
  builder: '',
  scanner: '',
  filePath: ''
})
const open = (app, add) => {
  projectInfo.name = app.name
  projectInfo.version = app.version
  data.add = add
  data.open = true
}
const close = () => {
  data.open = false
}
const clear = () => {
  data.currentStep = 0
  uploadRef.value.clear()
}
const selectLanguage = (language) => {
  projectInfo.language = language
  next()
}
const selectBuilder = (builder) => {
  projectInfo.builder = builder
  next()
}
const handleUpload = (uploadInfo) => {
  projectInfo.filePath = uploadInfo.filePath
  projectInfo.scanner = uploadInfo.scanner
}
const back = () => {
  data.currentStep -= 1
}
const next = () => {
  data.currentStep += 1
}
const submit = () => {
  const params = {
    ...projectInfo
  }
  if (projectInfo.filePath === '') {
    message.info('文件未上传完成')
    return
  }
  AddDependency(params)
    .then((res) => {
      // console.log('AddDependency', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      message.success(data.add ? '添加成功' : '更新成功')
      data.open = false
      emit('success')
      setTimeout(() => {
        clear()
      }, 500)
    })
    .catch((e) => {
      message.error(e)
    })
}
defineExpose({ open })
</script>

<style lang="less" scoped>
.steps {
  width: 150px;
}
.upload {
  width: 500px;
  margin-left: 40px;
  margin-bottom: 20px;
}
.button {
  display: flex;
  justify-content: right;
  margin-top: 10px;
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
<style scoped src="@/atdv/select.css"></style>
