<template>
  <a-modal v-model:open="data.open" :width="data.create ? '800px' : '500px'" :footer="null">
    <template #title>
      <div style="font-size: 20px">添加项目</div>
    </template>
    <div style="margin-top: 20px; margin-bottom: 30px">
      <a-radio-group v-model:value="data.create" @change="(e) => changeMode(e)">
        <a-radio-button :value="true" style="width: 120px">创建项目添加</a-radio-button>
        <a-radio-button :value="false" style="width: 120px">添加已有项目</a-radio-button>
      </a-radio-group>
    </div>
    <div v-if="data.create">
      <CreateProModal ref="createProModal" @success="createSuccess()" @close="close()"></CreateProModal>
    </div>
    <div v-else>
      <div style="display: flex; margin-top: 20px">
        <a-form :model="formState" ref="formRef" name="application" :label-col="{ span: 8 }">
          <a-form-item label="项目名称" name="name" :rules="[{ required: true, message: '请输入项目名称' }]">
            <a-input v-model:value="formState.name" :placeholder="formState.artifactId" style="width: 300px" />
          </a-form-item>
          <a-form-item label="组织ID" name="groupId" :rules="[{ required: true, message: '请输入项目groupId' }]">
            <a-input v-model:value="formState.groupId" style="width: 300px" />
          </a-form-item>
          <a-form-item label="工件ID" name="artifactId" :rules="[{ required: true, message: '请输入项目artifactId' }]">
            <a-input v-model:value="formState.artifactId" style="width: 300px" />
          </a-form-item>
          <a-form-item label="版本编号" name="version" :rules="[{ required: true, message: '请选择项目版本' }]">
            <a-select
              v-model:value="formState.version"
              :options="select.options"
              style="width: 300px"
              @focus="getVersions()"></a-select>
          </a-form-item>
        </a-form>
      </div>
      <div class="button">
        <a-button class="cancel-btn" @click="close">取消</a-button>
        <a-button class="btn" @click="submit">提交</a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup>
import { reactive, ref, defineExpose, defineEmits } from 'vue'
import { AppAddProject, GetVersionList } from '@/api/frontend'
import CreateProModal from './CreateProModal.vue'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'

const emit = defineEmits(['success'])
const store = useStore()
const createProModal = ref()

const data = reactive({
  open: false,
  create: true
})
const formRef = ref()
const formState = reactive({
  groupId: '',
  artifactId: '',
  version: '',
  name: ''
})
const applicationInfo = reactive({
  appGroupId: '',
  appArtifactId: '',
  appVersion: ''
})
const select = reactive({
  options: []
})
const open = () => {
  data.open = true
  applicationInfo.appGroupId = store.getters.currentApp.groupId
  applicationInfo.appArtifactId = store.getters.currentApp.artifactId
  applicationInfo.appVersion = store.getters.currentApp.version
}
const close = () => {
  data.open = false
}
const clear = () => {
  formState.groupId = ''
  formState.artifactId = ''
  formState.version = '1.0.0'
  formState.name = ''
}
const changeMode = (create) => {}
const getVersions = () => {
  GetVersionList({ groupId: formState.groupId, artifactId: formState.artifactId }).then((res) => {
    if (res.code !== 200) {
      message.error(res.message)
      return
    }
    select.options = res.data.map((option) => {
      return { label: option, value: option, key: option }
    })
  })
}
const createSuccess = () => {
  data.open = false
  emit('success')
}
const submit = () => {
  if (formState.name === '') formState.name = formState.artifactId
  formRef.value
    .validate()
    .then(() => {
      const params = {
        ...formState,
        ...applicationInfo
      }
      delete params.name
      AppAddProject(params)
        .then((res) => {
          // console.log('AppAddProject', res)
          if (res.code !== 200) {
            message.error(res.message)
            return
          }
          message.success('添加项目成功')
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
<style scoped src="@/atdv/select.css"></style>
<style scoped src="@/atdv/radio-btn.css"></style>
