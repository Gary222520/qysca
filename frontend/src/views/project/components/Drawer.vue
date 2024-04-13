<template>
  <a-drawer v-model:open="data.open" width="800px" :closable="false" placement="right">
    <template #title><div style="font-size: 20px">组件详情</div></template>
    <a-spin :spinning="data.spinning" tip="组件信息加载中，请稍等...">
      <div>
        <span style="font-size: 18px; font-weight: bold">{{ data.detail?.name }}</span>
        <a-tag style="margin-left: 10px">{{ data.detail?.version }}</a-tag>
        <a-tag v-if="data.component?.type === 'opensource'">开源</a-tag>
        <a-tag v-if="data.component?.type === 'business'">商用</a-tag>
        <a-tag v-if="data.component?.type === 'internal'">内部</a-tag>
        <a-button v-if="data.dependency" type="primary" style="margin-left: 30px" @click="showDependency">
          查看依赖信息
        </a-button>
      </div>
      <div class="relative">
        <div class="drawer_title">基本信息</div>
      </div>
      <a-descriptions>
        <a-descriptions-item label="名称">{{ data.detail?.name }}</a-descriptions-item>
        <a-descriptions-item label="版本" span="2">{{ data.detail?.version }}</a-descriptions-item>
        <a-descriptions-item label="语言" span="3">
          <div v-if="data.detail?.language instanceof Array">
            <a-tag v-for="(item, index) in data.detail?.language" :key="index">{{ item }}</a-tag>
          </div>
          <div v-else>
            <a-tag>{{ data.detail?.language }}</a-tag>
          </div>
        </a-descriptions-item>
        <a-descriptions-item label="组件描述" span="3">
          <div style="position: relative">
            <span ref="descriptionRef" v-html="description.text"></span>
            <div class="text-btn" v-if="description.showBtn" @click="changeDescription()">
              {{ description.showTotal ? '收起' : '展开' }}
            </div>
          </div>
        </a-descriptions-item>
        <a-descriptions-item label="主页地址" span="3">{{ data.detail?.url || '-' }}</a-descriptions-item>
        <a-descriptions-item label="源码地址" span="3">{{ data.detail?.sourceUrl || '-' }}</a-descriptions-item>
        <a-descriptions-item label="下载地址" span="3">{{ data.detail?.downloadUrl || '-' }}</a-descriptions-item>
        <a-descriptions-item label="包获取地址" span="3">{{ data.detail?.purl || '-' }}</a-descriptions-item>
      </a-descriptions>

      <div class="relative">
        <div class="drawer_title">许可证信息</div>
      </div>

      <a-descriptions>
        <a-descriptions-item label="许可证" span="3">
          <div>
            <a-tag v-for="(item, index) in data.detail?.licenses" :key="index">{{ item }}</a-tag>
          </div>
        </a-descriptions-item>
      </a-descriptions>

      <div class="relative">
        <div class="drawer_title">开发者信息</div>
      </div>
      <a-table :data-source="data.detail?.developers" :columns="data.developerColumns" :pagination="false">
        <template #emptyText>暂无数据</template>
      </a-table>
    </a-spin>
  </a-drawer>
</template>

<script setup>
import { reactive, ref, defineExpose, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { GetComponentInfo } from '@/api/frontend'
import { message } from 'ant-design-vue'
import { arrToString } from '@/utils/util.js'

const router = useRouter()
const descriptionRef = ref()

const data = reactive({
  spinning: false,
  open: false,
  dependency: false,
  component: {},
  detail: {},
  licenseColumns: [
    { title: '许可证名称', dataIndex: 'name', key: 'name' },
    { title: '许可证地址', dataIndex: 'url', key: 'url' }
  ],
  developerColumns: [
    { title: '开发者ID', dataIndex: 'id', key: 'id' },
    { title: '开发者姓名', dataIndex: 'name', key: 'name' },
    { title: '开发者邮箱', dataIndex: 'email', key: 'email' }
  ]
})
const description = reactive({
  text: '',
  showBtn: true,
  showTotal: false
})

const open = (component, dependency) => {
  data.open = true
  data.dependency = dependency
  data.component = component
  getComponentInfo()
}
const close = () => {
  data.open = false
}
const getComponentInfo = () => {
  const params = {
    name: data.component.name,
    version: data.component.version,
    language: data.component.language
  }
  if (params.language instanceof Array) params.language = 'app'
  // console.log('params', params)
  data.spinning = true
  GetComponentInfo(params)
    .then((res) => {
      // console.log('GetComponentInfo', res)
      if (res.code !== 200) {
        data.spinning = false
        message.error(res.message)
        return
      }
      data.detail = res.data
      data.detail.licenses = data.detail.licenses.filter((item) => item !== '')
      description.text = data.detail.description
      cutDescription(3)
      data.spinning = false
    })
    .catch((err) => {
      data.spinning = false
      console.error(err)
    })
}

const changeDescription = () => {
  description.showTotal = !description.showTotal
  if (description.showTotal) descriptionRef.value.innerHTML = description.text
  else cutDescription(3)
}
const cutDescription = (line = 3) => {
  if (!descriptionRef.value) return

  if (description.text) descriptionRef.value.innerHTML = description.text
  else return

  nextTick(() => {
    // 文本行数
    let rows = descriptionRef.value.getClientRects().length
    // 文本内容
    let content = description.text
    // 文本小于指定行数则不用截取
    if (rows <= line) {
      descriptionRef.value.innerHTML = description.text
      description.showBtn = false
      return
    }
    // 截取文本
    while (rows > line) {
      // 截取字符数
      // 截取至目标行数前，每一次截取更多字符以缩减时间
      let step = rows > line + 1 ? 100 : 1
      // 遇到标签
      if (/<br\/>$/.test(content)) step = 5
      content = content.slice(0, -step)
      descriptionRef.value.innerHTML = content
      rows = descriptionRef.value.getClientRects().length
    }
    // 末尾替换为省略号（中文-3，英文-8）
    if (content.charCodeAt(content.length - 1) < 255) descriptionRef.value.innerHTML = content.slice(0, -8) + '...'
    else descriptionRef.value.innerHTML = content.slice(0, -3) + '...'
    description.showBtn = true
  })
}

const showDependency = () => {
  router.push({
    path: '/home/dependency',
    query: {
      name: data.component.name,
      version: data.component.version,
      language: data.component.language
    }
  })
}
defineExpose({ open })
</script>

<style scoped>
.drawer_title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
  margin: 15px 0;
  padding-left: 10px;
}
.drawer_title::before {
  position: absolute;
  display: block;
  content: '';
  width: 3px;
  height: 18px;
  left: 0;
  background-color: #6f005f;
}
.relative {
  position: relative;
  display: flex;
  align-items: center;
}
.text-btn {
  position: absolute;
  right: 0;
  bottom: 0;
  cursor: pointer;
  color: #6f005f;
}
</style>
<style scoped src="@/atdv/pagination.css"></style>
<style scoped src="@/atdv/primary-btn.css"></style>
<style scoped src="@/atdv/description.css"></style>
