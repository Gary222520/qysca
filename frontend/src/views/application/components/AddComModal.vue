<template>
  <a-modal v-model:open="data.open" @cancel="close()" :footer="null">
    <template #title>
      <div style="font-size: 20px">添加组件</div>
    </template>
    <div style="display: flex; margin-top: 20px">
      <a-form :model="formState" ref="formRef" name="application" :label-col="{ span: 8 }">
        <a-form-item label="语言" name="language" :rules="[{ required: true, message: '请选择语言' }]">
          <a-select v-model:value="formState.language" style="width: 300px">
            <a-select-option value="java">java</a-select-option>
            <a-select-option value="python">python</a-select-option>
            <a-select-option value="golang">golang</a-select-option>
            <a-select-option value="javaScript">javaScript</a-select-option>
            <a-select-option value="app">mixed</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="组件名称" name="name" :rules="[{ required: true, message: '请输入组件名称' }]">
          <a-dropdown placement="bottom">
            <a-input v-model:value="formState.name" @change="() => getNameList()" style="width: 300px" />
            <template #overlay>
              <a-menu v-if="selection.componentList.length" style="max-height: 300px; width: 300px; overflow-y: scroll">
                <a-menu-item
                  v-for="(item, index) in selection.componentList"
                  :key="index"
                  @click="() => chooseName(item)">
                  {{ item.name }}
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </a-form-item>
        <a-form-item label="版本编号" name="version" :rules="[{ required: true, message: '请选择组件版本' }]">
          <a-select
            v-model:value="formState.version"
            :options="selection.options"
            @change="(value) => chooseVersion(value)"
            style="width: 300px"></a-select>
        </a-form-item>
      </a-form>
    </div>
    <div class="button">
      <a-button class="cancel-btn" @click="close">取消</a-button>
      <a-button class="btn" @click="submit()">添加</a-button>
    </div>
  </a-modal>
</template>

<script setup>
import { reactive, ref, defineExpose, defineEmits } from 'vue'
import { GetComponentNameList, AddProjectComponent } from '@/api/frontend'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'

const emit = defineEmits(['success'])
const store = useStore()

const data = reactive({
  open: false
})
const selection = reactive({
  componentList: [],
  options: []
})
const formRef = ref()
const formState = reactive({
  name: '',
  version: '',
  language: 'java'
})
const parentInfo = reactive({
  parentName: '',
  parentVersion: ''
})
const componentInfo = reactive({
  name: '',
  version: '',
  language: ''
})
const open = (parent) => {
  data.open = true
  parentInfo.parentName = parent.name
  parentInfo.parentVersion = parent.version
}
const close = () => {
  data.open = false
  clear()
}
const clear = () => {
  formRef.value.resetFields()
  selection.componentList = []
  selection.options = []
  componentInfo.name = ''
  componentInfo.version = ''
  componentInfo.language = ''
}
const getNameList = async () => {
  formState.version = ''
  selection.options = []
  if (formState.name === '') return
  await GetComponentNameList({ name: formState.name, language: formState.language })
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      // console.log('GetComponentNameList', res)
      selection.componentList = res.data.reduce((pre, curr) => {
        let exsist = false
        pre.forEach((item) => {
          if (curr.name === item.name) {
            item.versions.push({ label: curr.version, value: curr.version })
            exsist = true
          }
        })
        if (!exsist) {
          pre.push({
            name: curr.name,
            versions: [{ label: curr.version, value: curr.version }]
          })
        }
        return pre
      }, [])
      componentInfo.name = formState.name
      componentInfo.language = formState.language
      if (selection.componentList.length === 1) selection.options = selection.componentList[0].versions
    })
    .catch((err) => {
      console.error(err)
    })
}
const chooseName = async (item) => {
  // console.log('chooseName', item)
  formState.name = item.name
  await getNameList()
  componentInfo.name = item.name
  componentInfo.language = formState.language
  selection.options = item.versions
}
const chooseVersion = (version) => {
  componentInfo.version = version
}
const submit = () => {
  formRef.value
    .validate()
    .then(() => {
      const params = {
        ...parentInfo,
        ...componentInfo
      }
      AddProjectComponent(params)
        .then((res) => {
          // console.log('AddProjectComponent', res)
          if (res.code !== 200) {
            message.error(res.message)
            return
          }
          message.success('添加组件成功')
          emit('success')
          close()
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
<style scoped src="@/atdv/select.css"></style>
