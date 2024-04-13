<template>
  <a-modal v-model:open="data.open" width="800px" @cancel="close()" :footer="null">
    <template #title>
      <div style="font-size: 20px">添加组件</div>
    </template>
    <a-spin :spinning="data.spinning" tip="添加组件中，请稍等...">
      <div style="display: flex; margin: 20px 0">
        <a-steps class="steps" direction="vertical" :current="data.currentStep" :items="data.steps"></a-steps>
        <div v-if="data.currentStep === 0">
          <Language @select="(lang) => selectLanguage(lang)"></Language>
        </div>
        <div v-if="data.currentStep === 1">
          <JavaBuilder
            v-if="componentInfo.language === 'java'"
            @select="(builder) => selectBuilder(builder)"></JavaBuilder>
          <PythonBuilder
            v-if="componentInfo.language === 'python'"
            @select="(builder) => selectBuilder(builder)"></PythonBuilder>
          <GoBuilder
            v-if="componentInfo.language === 'golang'"
            @select="(builder) => selectBuilder(builder)"></GoBuilder>
          <JSBuilder
            v-if="componentInfo.language === 'javaScript'"
            @select="(builder) => selectBuilder(builder)"></JSBuilder>
        </div>
        <div v-if="data.currentStep === 2">
          <div class="upload" v-if="componentInfo.language === 'java'">
            <div v-if="componentInfo.builder === 'maven'">
              <Upload ref="uploadRef" :accept="'.xml'" :upload-text="'pom.xml'" @success="handleUpload"></Upload>
            </div>
            <div v-if="componentInfo.builder === 'gradle'">
              <Upload ref="uploadRef" :accept="'.zip'" :upload-text="'.zip文件'" @success="handleUpload"></Upload>
            </div>
            <div v-if="componentInfo.builder === 'zip'">
              <Upload ref="uploadRef" :accept="'.zip'" :upload-text="'.zip文件'" @success="handleUpload"></Upload>
            </div>
            <div v-if="componentInfo.builder === 'jar'">
              <Upload ref="uploadRef" :accept="'.jar'" :upload-text="'jar包'" @success="handleUpload"></Upload>
            </div>
          </div>
          <div class="upload" v-if="componentInfo.language === 'python'">
            <div v-if="componentInfo.builder === 'zip'">
              <Upload ref="uploadRef" :accept="'.zip'" :upload-text="'.zip文件'" @success="handleUpload"></Upload>
            </div>
            <div v-if="componentInfo.builder === 'txt'">
              <Upload ref="uploadRef" :accept="'.txt'" :upload-text="'.txt文件'" @success="handleUpload"></Upload>
            </div>
            <div v-if="componentInfo.builder === 'tar.gz'">
              <Upload ref="uploadRef" :accept="'.gz'" :upload-text="'tar.gz文件'" @success="handleUpload"></Upload>
            </div>
          </div>
          <div class="upload" v-if="componentInfo.language === 'golang'">
            <div v-if="componentInfo.builder === 'go.mod'">
              <Upload ref="uploadRef" :accept="'.mod'" :upload-text="'go.mod'" @success="handleUpload"></Upload>
            </div>
            <div v-if="componentInfo.builder === 'zip'">
              <Upload ref="uploadRef" :accept="'.zip'" :upload-text="'.zip文件'" @success="handleUpload"></Upload>
            </div>
          </div>
          <div class="upload" v-if="componentInfo.language === 'javaScript'">
            <div v-if="componentInfo.builder === 'package.json'">
              <Upload ref="uploadRef" :accept="'.json'" :upload-text="'package.json'" @success="handleUpload"></Upload>
            </div>
            <div v-if="componentInfo.builder === 'zip'">
              <Upload ref="uploadRef" :accept="'.zip'" :upload-text="'.zip文件'" @success="handleUpload"></Upload>
            </div>
          </div>
          <a-form :model="formState" ref="formRef" name="component" :label-col="{ span: 5 }" style="margin-top: 30px">
            <a-form-item label="组件名称" name="name" :rules="[{ required: true, message: '请输入组件名称' }]">
              <a-input v-model:value="formState.name" style="width: 300px" />
            </a-form-item>
            <a-form-item label="版本编号" name="version" :rules="[{ required: true, message: '请输入版本编号' }]">
              <a-input v-model:value="formState.version" style="width: 300px" />
            </a-form-item>
            <a-form-item label="组件类型" name="type" :rules="[{ required: true, message: '请选择组件类型' }]">
              <a-select v-model:value="formState.type" style="width: 300px">
                <a-select-option value="opensource">开源</a-select-option>
                <a-select-option value="business">商用</a-select-option>
                <a-select-option value="internal">内部</a-select-option>
              </a-select>
            </a-form-item>
          </a-form>
        </div>
      </div>
      <div class="button">
        <a-button class="cancel-btn" @click="close">取消</a-button>
        <a-button class="btn" v-if="data.currentStep === 2" @click="submit">提交</a-button>
        <a-button class="btn" v-if="data.currentStep > 0" @click="back">上一步</a-button>
      </div>
    </a-spin>
  </a-modal>
</template>

<script setup>
import { reactive, ref, defineExpose, defineEmits } from 'vue'
import { AddComponent } from '@/api/frontend'
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
  currentStep: 0,
  steps: [{ title: '选择语言' }, { title: '选择工具' }, { title: '上传文件' }],
  spinning: false
})
const componentInfo = reactive({
  language: '',
  builder: '',
  filePath: ''
})
const formRef = ref()
const formState = reactive({
  name: '',
  version: '',
  type: 'opensource'
})
const open = () => {
  data.open = true
}
const close = () => {
  data.open = false
  clear()
}
const clear = () => {
  data.currentStep = 0
  if (data.currentStep === 2) {
    formRef.value.resetFields()
    uploadRef.value.clear()
  }
}
const selectLanguage = (language) => {
  componentInfo.language = language
  next()
}
const selectBuilder = (builder) => {
  componentInfo.builder = builder
  next()
}
const handleUpload = (uploadInfo) => {
  componentInfo.filePath = uploadInfo.filePath
}
const back = () => {
  data.currentStep -= 1
}
const next = () => {
  data.currentStep += 1
}
const submit = () => {
  if (componentInfo.filePath === '') {
    message.info('文件未上传完成')
    return
  }
  formRef.value
    .validate()
    .then(() => {
      const params = {
        ...componentInfo,
        ...formState
      }
      data.spinning = true
      AddComponent(params)
        .then((res) => {
          // console.log('AddComponent', res)
          data.spinning = false
          if (res.code !== 200) {
            message.error(res.message)
            return
          }
          message.success('添加组件成功')
          emit('success')
          close()
        })
        .catch((err) => {
          data.spinning = false
          message(err)
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
<style scoped src="@/atdv/spin.css"></style>
<style scoped src="@/atdv/select.css"></style>
