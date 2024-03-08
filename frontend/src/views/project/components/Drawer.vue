<template>
  <a-drawer v-model:open="data.open" width="800px" :closable="false" placement="right">
    <template #title><div style="font-size: 24px">组件详情</div></template>
    <div>
      <span style="font-size: 20px; font-weight: bold">{{ data.detail?.name }}</span>
      <a-tag style="margin-left: 10px">{{ data.detail?.version }}</a-tag>
      <a-tag>{{ data.component?.opensource ? '开源' : '闭源' }}</a-tag>
      <a-button v-if="data.dependency" type="primary" style="margin-left: 30px" @click="showDependency">
        查看依赖信息
      </a-button>
    </div>
    <div class="relative">
      <div class="drawer_title" style="margin-bottom: 20px">基本信息</div>
    </div>
    <a-descriptions>
      <a-descriptions-item label="组织ID">{{ data.detail?.groupId }}</a-descriptions-item>
      <a-descriptions-item label="工件ID">{{ data.detail?.artifactId }}</a-descriptions-item>
      <a-descriptions-item label="语言">{{ data.detail?.language }}</a-descriptions-item>
      <a-descriptions-item label="组件描述" span="3">{{ data.detail?.description }}</a-descriptions-item>

      <a-descriptions-item label="主页地址" span="3">{{ data.detail?.url }}</a-descriptions-item>
      <a-descriptions-item label="源码地址" span="3">{{ data.detail?.sourceUrl }}</a-descriptions-item>
      <a-descriptions-item label="下载地址" span="3">{{ data.detail?.downloadUrl }}</a-descriptions-item>
    </a-descriptions>

    <div class="relative">
      <div class="drawer_title">许可证信息</div>
    </div>
    <a-table :data-source="data.detail?.licenses" :columns="data.licenseColumns" :pagination="false">
      <template #emptyText>暂无数据</template>
    </a-table>

    <div class="relative">
      <div class="drawer_title">开发者信息</div>
    </div>
    <a-table :data-source="data.detail?.developers" :columns="data.developerColumns" :pagination="false">
      <template #emptyText>暂无数据</template>
    </a-table>
  </a-drawer>
</template>

<script setup>
import { reactive, defineExpose } from 'vue'
import { useRouter } from 'vue-router'
import { GetComponentInfo } from '@/api/frontend'
import { message } from 'ant-design-vue'

const router = useRouter()
const data = reactive({
  open: false,
  dependency: false,
  component: {},
  detail: {},
  licenseColumns: [
    { title: '许可证名称', dataIndex: 'licenseName', key: 'licenseName' },
    { title: '许可证地址', dataIndex: 'licenseUrl', key: 'licenseUrl' }
  ],
  developerColumns: [
    { title: '开发者ID', dataIndex: 'developerId', key: 'developerId' },
    { title: '开发者姓名', dataIndex: 'developerName', key: 'developerName' },
    { title: '开发者邮箱', dataIndex: 'developerEmail', key: 'developerEmail' }
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
    groupId: data.component.groupId,
    artifactId: data.component.artifactId,
    version: data.component.version,
    opensource: data.component.opensource
  }
  // console.log('params', params)
  GetComponentInfo(params)
    .then((res) => {
      // console.log('GetComponentInfo', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      data.detail = res.data
    })
    .catch((err) => {
      console.error(err)
    })
}
const showDependency = () => {
  router.push({
    path: '/home/dependency',
    query: {
      name: data.component.name,
      groupId: data.component.groupId,
      artifactId: data.component.artifactId,
      version: data.component.version,
      opensource: data.component.opensource
    }
  })
}
defineExpose({ open })
</script>

<style scoped>
.drawer_title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  margin-top: 20px;
  margin-bottom: 10px;
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
