<template>
  <a-drawer v-model:open="data.open" width="900px" :closable="false" placement="right">
    <template #title><div style="font-size: 24px">许可证详情</div></template>
    <a-spin :spinning="data.spinning" tip="许可证信息加载中，请稍等...">
      <div style="display: flex; align-items: center">
        <a-tag v-if="data.detail?.riskLevel === 'high'" color="error">高危</a-tag>
        <a-tag v-if="data.detail?.riskLevel === 'medium'" color="warning">中危</a-tag>
        <a-tag v-if="data.detail?.riskLevel === 'low'" color="processing">低危</a-tag>
        <span style="font-size: 20px; font-weight: bold; margin-right: 10px">{{ data.detail?.name }}</span>
        <a-tag v-if="data.detail?.isOsiApproved" color="success">OSI认证</a-tag>
        <a-tag v-if="data.detail?.isFsfApproved" color="success">FSF许可</a-tag>
        <a-tag v-if="data.detail?.isSpdxApproved" color="success">SPDX认证</a-tag>
        <a-tag v-if="data.detail?.gplCompatibility" color="success">GPL兼容</a-tag>
        <a-tag v-else color="error">GPL不兼容</a-tag>
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
        <a-descriptions-item label="许可证内容" span="3">
          <div style="position: relative">
            <span ref="text">{{ data.text }}</span>
            <div class="text-btn" v-if="data.showBtn" @click="changeText()">{{ data.showTotal ? '收起' : '展开' }}</div>
          </div>
        </a-descriptions-item>
      </a-descriptions>

      <div class="relative">
        <div class="drawer_title" style="margin-bottom: 20px">许可证义务</div>
      </div>
      <div class="list prohibited">
        <a-list size="small" bordered :locale="data.locale" :data-source="data.detail?.obligationsRequired">
          <template #header>
            <div>必须义务</div>
          </template>
          <template #renderItem="{ item, index }">
            <a-list-item>
              <div>
                <span style="font-weight: bold">{{ `${index + 1}、${item.title}：` }}</span>
                <span>{{ `${item.content}` }}</span>
              </div>
            </a-list-item>
          </template>
        </a-list>
      </div>
      <div class="list allowed">
        <a-list size="small" bordered :locale="data.locale" :data-source="data.detail?.obligationsNotRequired">
          <template #header>
            <div>无需义务</div>
          </template>
          <template #renderItem="{ item, index }">
            <a-list-item>
              <div>
                <span style="font-weight: bold">{{ `${index + 1}、${item.title}：` }}</span>
                <span>{{ `${item.content}` }}</span>
              </div>
            </a-list-item>
          </template>
        </a-list>
      </div>

      <div class="relative">
        <div class="drawer_title" style="margin-bottom: 20px">许可证权利</div>
      </div>
      <div class="list allowed">
        <a-list size="small" bordered :locale="data.locale" :data-source="data.detail?.rightsAllowed">
          <template #header>
            <div>允许权利</div>
          </template>
          <template #renderItem="{ item, index }">
            <a-list-item>
              <div>
                <span style="font-weight: bold">{{ `${index + 1}、${item.title}：` }}</span>
                <span>{{ `${item.content}` }}</span>
              </div>
            </a-list-item>
          </template>
        </a-list>
      </div>
      <div class="list prohibited">
        <a-list size="small" bordered :locale="data.locale" :data-source="data.detail?.rightsProhibited">
          <template #header>
            <div>禁止权利</div>
          </template>
          <template #renderItem="{ item, index }">
            <a-list-item>
              <div>
                <span style="font-weight: bold">{{ `${index + 1}、${item.title}：` }}</span>
                <span>{{ `${item.content}` }}</span>
              </div>
            </a-list-item>
          </template>
        </a-list>
      </div>
    </a-spin>
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
  spinning: false,
  license: {},
  detail: {},
  text: '',
  showTotal: false,
  showBtn: true,
  locale: {
    emptyText: '暂无内容'
  }
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
  data.spinning = true
  GetLicenseInfo({ licenseName: data.license.name })
    .then((res) => {
      // console.log('GetLicenseInfo', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      data.detail = res.data
      data.text = data.detail?.text
      cutText(5)
      data.spinning = false
    })
    .catch((err) => {
      data.spinning = false
      console.error(err)
    })
}

const changeText = () => {
  data.showTotal = !data.showTotal
  if (data.showTotal) text.value.innerHTML = data.text
  else cutText(5)
}

const cutText = (line = 5) => {
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
      // 截取至目标行数前，每一次截取更多字符以缩减时间
      let step = rows > line + 1 ? 100 : 1
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
.text-btn {
  position: absolute;
  right: 0;
  bottom: 0;
  cursor: pointer;
  color: #6f005f;
}
.list {
  margin-bottom: 20px;
}
:deep(.ant-list-bordered) {
  border-radius: 0;
}
.allowed :deep(.ant-list-split .ant-list-header) {
  font-weight: bold;
  color: #fff;
  background-color: #48cd7f;
}
.prohibited :deep(.ant-list-split .ant-list-header) {
  font-weight: bold;
  color: #fff;
  background-color: #f64e60;
}
</style>
<style scoped src="@/atdv/pagination.css"></style>
<style scoped src="@/atdv/primary-btn.css"></style>
<style scoped src="@/atdv/spin.css"></style>
<style scoped src="@/atdv/description.css"></style>
