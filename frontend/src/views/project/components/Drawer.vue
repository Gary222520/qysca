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
        <a-descriptions-item label="版本">{{ data.detail?.version }}</a-descriptions-item>
        <a-descriptions-item label="语言">{{ data.detail?.language }}</a-descriptions-item>
        <a-descriptions-item label="组件描述" span="3">{{ data.detail?.description }}</a-descriptions-item>

        <a-descriptions-item label="主页地址" span="3">{{ data.detail?.url }}</a-descriptions-item>
        <a-descriptions-item label="源码地址" span="3">{{ data.detail?.sourceUrl }}</a-descriptions-item>
        <a-descriptions-item label="下载地址" span="3">{{ data.detail?.downloadUrl }}</a-descriptions-item>
        <a-descriptions-item label="包获取地址" span="3">{{ data.detail?.purl }}</a-descriptions-item>
      </a-descriptions>

      <div class="relative">
        <div class="drawer_title">许可证信息</div>
      </div>
      <!-- <a-table :data-source="data.detail?.licenses" :columns="data.licenseColumns" :pagination="false">
      <template #emptyText>暂无数据</template>
    </a-table> -->
      <a-descriptions>
        <a-descriptions-item label="许可证">{{ arrToString(data.detail?.licenses) }}</a-descriptions-item>
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
import { reactive, defineExpose } from 'vue'
import { useRouter } from 'vue-router'
import { GetComponentInfo } from '@/api/frontend'
import { message } from 'ant-design-vue'
import { arrToString } from '@/utils/util.js'

const router = useRouter()
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
      data.spinning = false
      if (data.detail?.language instanceof Array) data.detail.language = arrToString(data.detail.language)
    })
    .catch((err) => {
      data.spinning = false
      console.error(err)
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
</style>
<style scoped src="@/atdv/pagination.css"></style>
<style scoped src="@/atdv/primary-btn.css"></style>
<style scoped src="@/atdv/description.css"></style>
