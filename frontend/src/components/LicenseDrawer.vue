<template>
  <a-drawer v-model:open="data.open" width="800px" :closable="false" placement="right">
    <template #title><div style="font-size: 24px">许可证详情</div></template>
    <div>
      <span style="font-size: 20px; font-weight: bold">{{ data.detail?.name }}</span>
      <a-tag v-if="data.detail?.riskLevel === 'high'" color="error">高危</a-tag>
      <a-tag v-if="data.detail?.riskLevel === 'medium'" color="warning">中危</a-tag>
      <a-tag v-if="data.detail?.riskLevel === 'low'" color="processing">低危</a-tag>
      <a-tag v-if="data.detail?.isOsiApproved" color="success">OSI认证</a-tag>
      <a-tag v-if="data.detail?.isFsfApproved" color="success">FSF许可</a-tag>
      <a-tag v-if="data.detail?.isSpdxApproved" color="success">SPDX认证</a-tag>
      <a-tag v-if="data.detail?.gplCompatibility" color="success">GPL兼容</a-tag>
      <a-tag v-else color="warning">GPL不兼容</a-tag>
    </div>
    <div class="relative">
      <div class="drawer_title" style="margin-bottom: 20px">基本信息</div>
    </div>
    <a-descriptions>
      <a-descriptions-item label="许可证全名" span="3">{{ data.detail?.fullName }}</a-descriptions-item>
      <a-descriptions-item label="许可证链接" span="3">{{ data.detail?.url }}</a-descriptions-item>
      <a-descriptions-item label="风险说明" span="3">{{ data.detail?.riskDisclosure }}</a-descriptions-item>
      <a-descriptions-item label="GPL兼容性说明" span="3">
        {{ data.detail?.gplCompatibilityDescription }}
      </a-descriptions-item>
      <a-descriptions-item label="许可证内容说明" span="3">
        <span ref="text">{{ data.text }}</span>
        <div v-if="data.showBtn" @click="changeText()">{{ data.showTotal ? '收起' : '展开' }}</div>
      </a-descriptions-item>
    </a-descriptions>

    <div class="relative">
      <div class="drawer_title" style="margin-bottom: 20px">许可证义务</div>
    </div>
    <div style="display: flex">
      <div>
        <a-list size="small" bordered :data-source="data.detail?.obligationsRequired">
          <template #header>
            <div>必须义务</div>
          </template>
          <template #renderItem="{ item, index }">
            <a-list-item>
              <span>{{ index }}、</span>
              <span>{{ item.title }}：</span>
              <span>{{ item.content }}</span>
            </a-list-item>
            <a-list-item v-if="data.detail?.obligationsRequired.length == 0">暂无内容</a-list-item>
          </template>
        </a-list>
      </div>
      <div>
        <a-list size="small" bordered :data-source="data.detail?.obligationsNotRequired">
          <template #header>
            <div>无需义务</div>
          </template>
          <template #renderItem="{ item, index }">
            <a-list-item>
              <span>{{ index }}、</span>
              <span>{{ item.title }}：</span>
              <span>{{ item.content }}</span>
            </a-list-item>
            <a-list-item v-if="data.detail?.obligationsNotRequired.length == 0">暂无内容</a-list-item>
          </template>
        </a-list>
      </div>
    </div>

    <div class="relative">
      <div class="drawer_title" style="margin-bottom: 20px">许可证权利</div>
    </div>
    <div style="display: flex">
      <div>
        <a-list size="small" bordered :data-source="data.detail?.rightsAllowed">
          <template #header>
            <div>允许权利</div>
          </template>
          <template #renderItem="{ item, index }">
            <a-list-item>
              <span>{{ index }}、</span>
              <span>{{ item.title }}：</span>
              <span>{{ item.content }}</span>
            </a-list-item>
            <a-list-item v-if="data.detail?.rightsAllowed.length == 0">暂无内容</a-list-item>
          </template>
        </a-list>
      </div>
      <div>
        <a-list size="small" bordered :data-source="data.detail?.rightsProhibited">
          <template #header>
            <div>禁止权利</div>
          </template>
          <template #renderItem="{ item, index }">
            <a-list-item>
              <span>{{ index }}、</span>
              <span>{{ item.title }}：</span>
              <span>{{ item.content }}</span>
            </a-list-item>
            <a-list-item v-if="data.detail?.rightsProhibited.length == 0">暂无内容</a-list-item>
          </template>
        </a-list>
      </div>
    </div>
  </a-drawer>
</template>

<script setup>
import { reactive, ref, defineExpose, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { GetLicenseInfo } from '@/api/frontend'
import { message } from 'ant-design-vue'

const router = useRouter()
const text = ref()

const data = reactive({
  open: false,
  license: {},
  detail: {},
  text: '',
  showTotal: false,
  showBtn: false
})
const open = (license) => {
  data.open = true
  data.license = license
  getLicenseInfo()
}
const close = () => {
  data.open = false
}
const getLicenseInfo = () => {
  GetLicenseInfo({ licenseName: data.license.name })
    .then((res) => {
      // console.log('GetLicenseInfo', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      data.detail = res.data
      data.text = data.detail?.text
      cutText(3)
    })
    .catch((err) => {
      console.error(err)
    })
}
const changeText = () => {
  data.showTotal = !data.showTotal
  if (data.showTotal) text.value.innerHTML = data.text
  else cutText(3)
}
const cutText = (line = 3) => {
  if (!text.value) return
  if (data.text) text.value.innerHTML = data.text
  else return
  nextTick(() => {
    // 文本行数
    let rows = text.value.getClientRects().length
    // 文本内容
    let content = data.text
    // 文本小于指定行数则不用截取
    if (rows < line) {
      text.value.innerHTML = data.text
      data.showBtn = false
      return
    }
    // 截取文本
    while (rows > line) {
      // 截取字符数
      let step = 1
      // 遇到换行标签
      if (/<br\/>$/.test(content)) step = 5
      content = content.slice(0, -step)
      text.value.innerHTML = content
      rows = text.value.getClientRects().length
    }
    // 末尾替换为省略号（中文-3，英文-8）
    if (content.charCodeAt(content.length - 1) < 255) text.value.innerHTML = content.slice(0, -8) + '...'
    else text.value.innerHTML = content.slice(0, -3) + '...'
    data.showBtn = true
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
