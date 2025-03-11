<template>
  <a-modal v-model:open="data.open" @close="close()" :footer="null">
    <template #title>
      <div style="font-size: 20px">更新组件信息</div>
    </template>
    <div style="display: flex; margin-top: 20px">
      <a-form :model="formState" ref="formRef" name="component" :label-col="{ span: 8 }">
        <a-form-item label="主页地址" name="url">
          <a-input v-model:value="formState.url" style="width: 300px" />
        </a-form-item>
        <a-form-item label="源码地址" name="sourceUrl">
          <a-input v-model:value="formState.sourceUrl" style="width: 300px" />
        </a-form-item>
        <a-form-item label="下载地址" name="downloadUrl">
          <a-input v-model:value="formState.downloadUrl" style="width: 300px" />
        </a-form-item>
        <a-form-item label="包获取地址" name="packageUrl">
          <a-input v-model:value="formState.packageUrl" style="width: 300px" />
        </a-form-item>
      </a-form>
    </div>
    <div class="button">
      <a-button class="cancel-btn" @click="close">取消</a-button>
      <a-button class="btn" @click="update()">修改</a-button>
    </div>
  </a-modal>
</template>

<script setup>
import { reactive, ref, defineExpose, defineEmits } from 'vue'
import { UpdateRelease } from '@/api/frontend'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'

const emit = defineEmits(['success'])
const store = useStore()

const data = reactive({
  open: false,
  com: {},
  language: ''
})
const formRef = ref()
const formState = reactive({
  url: '',
  sourceUrl: '',
  downloadUrl: '',
  packageUrl: ''
})
const open = (com) => {
  data.open = true
  data.com = com
  data.language = data.com.language instanceof Array ? 'app' : data.com.language
  formState.url = com.url
  formState.sourceUrl = com.sourceUrl
  formState.downloadUrl = com.downloadUrl
  formState.packageUrl = com.purl
}
const close = () => {
  data.open = false
  clear()
}
const clear = () => {
  formRef.value.resetFields()
}

const update = () => {
  formRef.value
    .validate()
    .then(() => {
      const params = {
        name: data.com.name,
        version: data.com.version,
        language: data.language,
        ...formState
      }
      UpdateRelease(params)
        .then((res) => {
          // console.log('UpdateRelease', res)
          if (res.code !== 200) {
            message.error(res.message)
            return
          }
          message.success('修改成功')
          close()
          emit('success')
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
.button {
  display: flex;
  justify-content: right;
}
.btn {
  min-width: 80px;
  margin-left: 10px;
  border: #00557c;
  background-color: #00557c;
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
  border-color: #00557c;
  color: #00557c;
}
</style>
<style scoped src="@/atdv/input.css"></style>
<style scoped src="@/atdv/select.css"></style>
