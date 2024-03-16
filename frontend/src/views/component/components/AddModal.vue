<template>
  <a-modal v-model:open="data.open" width="800px" :footer="null">
    <template #title>
      <div style="font-size: 20px">添加组件</div>
    </template>
    <a-spin :spinning="data.spinning" tip="添加组件中，请稍等...">
      <div style="display: flex; margin: 20px 0">
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
                  <li class="list_item">Java语言开发的组件</li>
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
                  <li class="list_item">Python语言开发的组件</li>
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
                  <li class="list_item">Go语言开发的组件</li>
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
                  <li class="list_item">JavaScript语言开发的组件</li>
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
        <div v-if="data.currentStep === 2">
          <div class="upload">
            <div v-if="projectInfo.builder === 'maven'">
              <Upload ref="uploadRef" :accept="'.xml'" :upload-text="'pom.xml'" @success="handleUpload"></Upload>
            </div>
            <div v-if="projectInfo.builder === 'zip'">
              <Upload ref="uploadRef" :accept="'.zip'" :upload-text="'.zip文件'" @success="handleUpload"></Upload>
            </div>
          </div>
          <div style="margin-left: 40px">
            选择组件类型：
            <a-select v-model:value="projectInfo.type">
              <a-select-option value="opensource">开源</a-select-option>
              <a-select-option value="business">商用</a-select-option>
              <a-select-option value="internal">内部</a-select-option>
            </a-select>
          </div>
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
import Upload from '@/views/project/components/Upload.vue'
import { message } from 'ant-design-vue'

const emit = defineEmits(['success'])
const uploadRef = ref()
const data = reactive({
  open: false,
  currentStep: 0,
  steps: [{ title: '选择语言' }, { title: '选择工具' }, { title: '上传文件' }],
  spinning: false
})
const projectInfo = reactive({
  language: 'java',
  builder: 'maven',
  filePath: '',
  type: 'opensource'
})
const open = () => {
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
  if (language === 'python' || language === 'go' || language === 'javascript') {
    message.info('暂未支持该语言')
    return
  }
  projectInfo.language = language
  next()
}
const selectTool = (builder) => {
  projectInfo.builder = builder
  next()
}
const handleUpload = (uploadInfo) => {
  projectInfo.filePath = uploadInfo.filePath
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
      data.open = false
      emit('success')
      setTimeout(() => {
        clear()
      }, 500)
    })
    .catch((err) => {
      data.spinning = false
      message(err)
    })
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
