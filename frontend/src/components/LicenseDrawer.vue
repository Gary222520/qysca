<template>
  <a-drawer v-model:open="data.open" width="900px" @close="close()" :closable="false" placement="right">
    <template #title><div style="font-size: 20px">许可证详情</div></template>
    <a-spin :spinning="data.spinning" tip="许可证信息加载中，请稍等...">
      <div style="display: flex; align-items: center; margin-bottom: 20px">
        <a-tag v-if="data.detail?.riskLevel === 'high'" class="error-tag">高危</a-tag>
        <a-tag v-if="data.detail?.riskLevel === 'medium'" class="warning-tag">中危</a-tag>
        <a-tag v-if="data.detail?.riskLevel === 'low'" class="processing-tag">低危</a-tag>
        <span style="font-size: 18px; font-weight: bold; margin-right: 10px">{{ data.detail?.name }}</span>
        <a-tag v-if="data.detail?.isOsiApproved" class="flex success-tag">
          <template #icon><img class="tag-img" src="@/assets/osi.png" /></template>
          <div style="color: #48cd7f">OSI认证</div>
        </a-tag>
        <a-tag v-if="data.detail?.isFsfApproved" class="flex success-tag">
          <template #icon><img class="tag-img" src="@/assets/fsf.png" /></template>
          <div style="color: #48cd7f">FSF许可</div>
        </a-tag>
        <a-tag v-if="data.detail?.isSpdxApproved" class="flex success-tag">
          <template #icon><img class="tag-img" src="@/assets/spdx.png" /></template>
          <div style="color: #48cd7f">SPDX认证</div>
        </a-tag>
        <a-tag v-if="data.detail?.gplCompatibility" class="success-tag">
          <div style="color: #48cd7f">GPL兼容</div>
        </a-tag>
        <a-tag v-else class="error-tag">
          <div style="color: #f64e60">GPL不兼容</div>
        </a-tag>
      </div>
      <div class="relative">
        <div class="drawer_title">基本信息</div>
      </div>
      <a-descriptions>
        <a-descriptions-item label="许可证全名" span="3">{{ data.detail?.fullName }}</a-descriptions-item>
        <a-descriptions-item label="许可证链接" span="3">{{ data.detail?.url }}</a-descriptions-item>
        <a-descriptions-item label="风险说明" span="3">{{ data.detail?.riskDisclosure }}</a-descriptions-item>
        <a-descriptions-item label="GPL兼容性说明" span="3">
          <div style="position: relative">
            <span ref="GPLtext" v-html="data.GPLtext"></span>
            <div class="text-btn" v-if="data.showBtn" @click="changeText()">{{ data.showTotal ? '收起' : '展开' }}</div>
          </div>
        </a-descriptions-item>
        <a-descriptions-item label="许可证内容" span="3">
          <a-button type="primary" @click="openModal()">
            <SearchOutlined :style="{ fontSize: '16px' }" />点击查看
          </a-button>
        </a-descriptions-item>
      </a-descriptions>
      <a-modal v-model:open="data.showLicense" width="1000px" @cancel="closeModal()">
        <template #title>
          <div style="font-size: 20px">许可证内容</div>
        </template>
        <span v-html="data.text"></span>
        <template #footer>
          <a-button class="btn" @click="closeModal()">确认</a-button>
        </template>
      </a-modal>

      <div class="relative">
        <div class="drawer_title">许可证义务</div>
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
        <div class="drawer_title">许可证权利</div>
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
import { SearchOutlined } from '@ant-design/icons-vue'
import { reactive, ref, defineExpose, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { GetLicenseInfo } from '@/api/frontend'
import { message } from 'ant-design-vue'

const router = useRouter()
const text = ref()
const GPLtext = ref()

const data = reactive({
  open: false,
  spinning: false,
  license: {},
  detail: {},
  text: '',
  showLicense: false,
  GPLtext: '',
  showTotal: false,
  showBtn: true,
  locale: {
    emptyText: '暂无内容'
  }
})
const open = (license) => {
  data.open = true
  data.license = license
  data.showTotal = false
  data.showBtn = true
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
      data.text = handleText(data.detail?.text)
      data.GPLtext = data.detail?.gplCompatibilityDescription
      cutText(3)
      data.spinning = false
    })
    .catch((err) => {
      data.spinning = false
      console.error(err)
    })
}
const handleText = (text) => {
  text = text.replace(/\\n/g, '<br/>')
  text = text.replace(/\\u003c/g, '<')
  text = text.replace(/\\u003d/g, '=')
  text = text.replace(/\\u003e/g, '>')
  text = text.replace(/\\u0026/g, '&')
  return text
}

const openModal = () => {
  data.showLicense = true
}
const closeModal = () => {
  data.showLicense = false
}

const changeText = () => {
  data.showTotal = !data.showTotal
  if (data.showTotal) GPLtext.value.innerHTML = data.GPLtext
  else cutText(3)
}
const cutText = (line = 3) => {
  if (!GPLtext.value) return

  if (data.GPLtext) GPLtext.value.innerHTML = data.GPLtext
  else return

  nextTick(() => {
    // 文本行数
    let rows = GPLtext.value.getClientRects().length
    // 文本内容
    let content = data.GPLtext
    // 文本小于指定行数则不用截取
    if (rows <= line) {
      GPLtext.value.innerHTML = data.GPLtext
      data.showBtn = false
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
      GPLtext.value.innerHTML = content
      rows = GPLtext.value.getClientRects().length
    }
    // 末尾替换为省略号（中文-3，英文-8）
    if (content.charCodeAt(content.length - 1) < 255) GPLtext.value.innerHTML = content.slice(0, -8) + '...'
    else GPLtext.value.innerHTML = content.slice(0, -3) + '...'
    data.showBtn = true
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
  color: #00557c;
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
  background-color: #00557c;
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
  color: #00557c;
}
.btn:hover {
  border-color: #00557c;
  color: #00557c;
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
  background-color: #00d7a0;
}
.prohibited :deep(.ant-list-split .ant-list-header) {
  font-weight: bold;
  color: #fff;
  background-color: #ef0137;
}
.flex {
  display: flex;
  align-items: center;
}
.tag-img {
  width: 12px;
  height: 12px;
  margin-right: 5px;
}
.lineSeparator {
  white-space: pre-wrap;
}
</style>
<style scoped src="@/atdv/pagination.css"></style>
<style scoped src="@/atdv/primary-btn.css"></style>
<style scoped src="@/atdv/spin.css"></style>
<style scoped src="@/atdv/description.css"></style>
<style scoped src="@/atdv/tag.css"></style>
