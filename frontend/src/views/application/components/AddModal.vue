<template>
  <a-modal class="modal" v-model:open="data.open" width="800px" :footer="null">
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
          <a-upload
            v-if="data.tool === 'maven'"
            class="uploader"
            name="pom"
            action="https://www.mocky.io/v2/5cc8019d300000980a055e76"
            accept=".xml"
            v-model:file-list="upload.fileList"
            :progress="upload.progress"
            :max-count="1"
            @change="(info) => handleUpload(info, 'pom')"
            @remove="reUpload">
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
    </div>
    <div class="button">
      <a-button class="cancel-btn" @click="close">取消</a-button>
      <a-button class="btn" v-if="data.currentStep === 2" @click="validateForm">下一步</a-button>
      <a-button class="btn" v-if="data.currentStep === 3" @click="submit">提交</a-button>
      <a-button class="btn" v-if="data.currentStep > 0" @click="back">上一步</a-button>
    </div>
  </a-modal>
</template>

<script>
import { reactive, ref } from 'vue'
import { CloudUploadOutlined } from '@ant-design/icons-vue'
export default {
  components: {
    CloudUploadOutlined
  },
  setup() {
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
      upload.fileList = []
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
    const handleUpload = (info, type) => {}
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
    return {
      data,
      formState,
      formRef,
      upload,
      open,
      close,
      selectLanguage,
      selectTool,
      validateVersion,
      validateForm,
      handleUpload,
      back,
      next,
      submit
    }
  }
}
</script>

<style lang="less" scoped>
.steps {
  width: 150px;
}
:deep(.ant-steps.ant-steps-vertical > .ant-steps-item) {
  min-height: 100px;
}
:deep(.ant-steps-item-container) {
  pointer-events: none;
}
:deep(.ant-steps .ant-steps-item-process .ant-steps-item-icon) {
  background-color: #6f005f;
  border: transparent;
}
:deep(.ant-steps .ant-steps-item-finish > .ant-steps-item-container > .ant-steps-item-tail::after) {
  background-color: #6f005f;
}
:deep(.ant-steps .ant-steps-item-finish .ant-steps-item-icon) {
  background-color: rgba(111, 0, 95, 0.1);
  border-color: rgba(111, 0, 95, 0.1);
}
:deep(.ant-steps .ant-steps-item-finish .ant-steps-item-icon > .ant-steps-icon) {
  color: #6f005f;
}
:deep(.ant-input:hover) {
  border-color: #6f005f;
}
:deep(.ant-input:focus) {
  border-color: #6f005f;
  box-shadow: 0 0 0 2px rgba(111, 0, 95, 0.1);
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
  display: flex;
  align-items: center;
  margin-left: 40px;
  margin-bottom: 20px;
}
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
