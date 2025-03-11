<template>
  <a-modal v-model:open="data.open" :footer="null">
    <template #title>
      <div style="font-size: 20px">应用升级</div>
    </template>
    <div style="display: flex; margin-top: 20px">
      <a-form :model="formState" ref="formRef" name="application" :label-col="{ span: 8 }">
        <a-form-item
          label="版本编号"
          name="newVersion"
          :rules="[{ required: true, validator: validateVersion, trigger: 'change' }]">
          <a-input v-model:value="formState.newVersion" style="width: 300px" :placeholder="data.versionPlaceholder" />
        </a-form-item>
        <a-form-item label="应用描述" name="description">
          <a-input v-model:value="formState.description" style="width: 300px" />
        </a-form-item>
      </a-form>
    </div>
    <div class="button">
      <a-button class="cancel-btn" @click="close">取消</a-button>
      <a-button class="btn" @click="submit">升级</a-button>
    </div>
  </a-modal>
</template>

<script setup>
import { reactive, ref, defineExpose, defineEmits } from 'vue'
import { UpgradeProject } from '@/api/frontend'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'

const emit = defineEmits(['success'])
const store = useStore()

const data = reactive({
  open: false,
  versionPlaceholder: ''
})
const formRef = ref()
const formState = reactive({
  name: '',
  oldVersion: '',
  newVersion: '',
  description: ''
})
const open = (app, parentApp) => {
  data.open = true
  formState.name = app.name
  formState.oldVersion = app.version
  data.versionPlaceholder = `当前版本为${app.version}`
  if (parentApp) {
    formState.parentName = parentApp.name
    formState.parentVersion = parentApp.version
  } else {
    delete formState.parentName
    delete formState.parentVersion
  }
}
const close = () => {
  data.open = false
}
const clear = () => {
  formRef.value.resetFields()
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
        creator: JSON.parse(sessionStorage.getItem('user')).user.uid
      }
      UpgradeProject(params)
        .then((res) => {
          // console.log('UpgradeProject', res)
          if (res.code !== 200) {
            message.error(res.message)
            return
          }
          message.success('应用升级成功')
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
.button {
  display: flex;
  justify-content: right;
  margin-right: 10px;
}
.btn {
  width: 80px;
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
