<template>
  <div class="main">
    <div class="title">组件管理</div>
    <a-card class="content_card">
      <div class="operations">
        <div>
          <a-radio-group v-model:value="data.search.type" @change="(e) => getComponents()">
            <a-radio-button value="opensource" style="width: 70px">开源</a-radio-button>
            <a-radio-button value="business" style="width: 70px">商用</a-radio-button>
            <a-radio-button value="internal" style="width: 100px">内部使用</a-radio-button>
          </a-radio-group>
          <a-button
            v-show="permit([ROLE.BU_REP, ROLE.BU_PO])"
            type="primary"
            @click="addComponent()"
            style="margin-left: 20px">
            <PlusOutlined />添加组件
          </a-button>
        </div>
        <div style="display: flex; align-items: center">
          <span v-if="!data.accurate">
            语言：<a-select
              v-model:value="data.search.language"
              placeholder="请选择语言"
              style="width: 200px; margin-right: 10px"
              @change="(value, option) => getComponents()">
              <a-select-option value="java">java</a-select-option>
              <a-select-option value="python">python</a-select-option>
              <a-select-option value="golang">golang</a-select-option>
              <a-select-option value="javaScript">javaScript</a-select-option>
              <a-select-option value="app">mixed</a-select-option>
            </a-select>
          </span>
          <a-dropdown placement="bottomRight">
            <a-input-search
              v-if="!data.accurate"
              v-model:value="data.search.name"
              placeholder="请输入组件名称"
              style="width: 400px"
              @change="
                (e) => {
                  getNameList()
                  getComponents()
                }
              "
              @search="(value, e) => getComponents()"></a-input-search>
            <template #overlay>
              <a-menu v-if="selection.nameList.length" style="max-height: 300px; width: 400px; overflow-y: scroll">
                <a-menu-item v-for="(item, index) in selection.nameList" :key="index" @click="() => chooseName(item)">
                  {{ item }}
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </div>
      <a-table :data-source="data.datasource" :columns="data.columns" bordered :pagination="pagination">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <div class="column_name" @click="showInfo(record)">{{ record.name }}</div>
          </template>
          <template v-if="column.key === 'type'">
            <div v-if="record.type === 'opensource'">开源</div>
            <div v-if="record.type === 'business'">商用</div>
            <div v-if="record.type === 'internal'">内部使用</div>
          </template>
          <template v-if="column.key === 'language'">
            <div v-if="record.language instanceof Array">{{ arrToString(record.language) }}</div>
            <div v-else>{{ record.language }}</div>
          </template>
          <template v-if="column.key === 'action'">
            <div
              v-if="record.state === 'SUCCESS' || record.state === 'CREATED'"
              style="display: flex; align-items: center">
              <div class="action_icon">
                <a-tooltip>
                  <template #title>详情</template>
                  <FileTextOutlined :style="{ fontSize: '18px', color: '#00557c' }" @click="showInfo(record)" />
                </a-tooltip>
              </div>
              <div class="action_icon" v-if="record.creator && record.creator !== '-'">
                <a-tooltip>
                  <template #title>更新</template>
                  <SyncOutlined :style="{ fontSize: '18px', color: '#00557c' }" @click="updateComponent(record)" />
                </a-tooltip>
              </div>
              <div class="action_icon" v-if="record.creator && record.creator !== '-'">
                <a-tooltip>
                  <template #title>删除</template>
                  <a-popconfirm v-model:open="record.popconfirm" title="确定删除这个组件吗？">
                    <template #cancelButton>
                      <a-button class="cancel_btn" size="small" @click="record.popconfirm = false">取消</a-button>
                    </template>
                    <template #okButton>
                      <a-button danger type="primary" size="small" @click="deleteComponent(record)">删除</a-button>
                    </template>
                    <DeleteOutlined
                      v-show="permit([ROLE.BU_REP, ROLE.BU_PO])"
                      :style="{ fontSize: '18px', color: '#ef0137' }" />
                  </a-popconfirm>
                </a-tooltip>
              </div>
            </div>
            <div v-if="record.state === 'RUNNING'" style="display: flex; align-items: center">
              <LoadingOutlined :style="{ fontSize: '18px', color: '#00557c' }" />
              <div style="margin-left: 10px">扫描分析中...</div>
            </div>
            <div v-if="record.state === 'FAILED'" style="display: flex; align-items: center">
              <a-popconfirm v-model:open="record.popconfirm" title="扫描出错，请重试">
                <template #cancelButton>
                  <a-button class="cancel_btn" size="small" @click="retry(record)">重试</a-button>
                </template>
                <template #okButton></template>
                <ExclamationCircleOutlined :style="{ fontSize: '18px', color: '#ef0137' }" />
                <span style="margin-left: 10px; color: #ef0137; cursor: pointer">扫描失败</span>
              </a-popconfirm>
            </div>
          </template>
        </template>
        <template #emptyText>暂无数据</template>
      </a-table>
      <Drawer ref="drawer"></Drawer>
      <AddModal ref="addModal" @success="getComponents()"></AddModal>
      <UpdateModal ref="updateModal" @success="getComponents()"></UpdateModal>
      <WarnModal ref="warnModal" @ok="getComponents()"></WarnModal>
    </a-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onActivated } from 'vue'
import {
  PlusOutlined,
  FileTextOutlined,
  SyncOutlined,
  DeleteOutlined,
  RollbackOutlined,
  LoadingOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue'
import { GetComponents, DeleteComponent, GetComponentNameList } from '@/api/frontend'
import Drawer from '@/views/project/components/Drawer.vue'
import AddModal from './components/AddModal.vue'
import UpdateModal from './components/UpdateModal.vue'
import WarnModal from '@/components/WarnModal.vue'
import { message } from 'ant-design-vue'
import { arrToString, ROLE, permit } from '@/utils/util.js'
import { useStore } from 'vuex'

const store = useStore()

onMounted(() => {
  if (store.getters.getComSearch) {
    data.search = store.getters.getComSearch
    getComponents(data.search.number, data.search.size)
  } else getComponents()
})

const drawer = ref()
const addModal = ref()
const updateModal = ref()
const warnModal = ref()

const data = reactive({
  search: {
    name: '',
    version: '',
    language: 'java',
    type: 'opensource'
  },
  datasource: [],
  columns: [
    { title: '组件名称', dataIndex: 'name', key: 'name' },
    { title: '版本', dataIndex: 'version', key: 'version' },
    { title: '语言', dataIndex: 'language', key: 'language' },
    { title: '类型', dataIndex: 'type', key: 'type' },
    { title: '操作', dataIndex: 'action', key: 'action', width: 150 }
  ]
})
const pagination = reactive({
  current: 1,
  total: 0,
  pageSize: 8,
  showSizeChanger: false,
  onChange: (page, size) => {
    pagination.current = page
    getComponents(page, size)
  },
  hideOnSinglePage: true
})
const selection = reactive({
  nameList: []
})
const getNameList = async () => {
  selection.nameList = []
  if (data.search.name === '') return
  await GetComponentNameList({ name: data.search.name, language: data.search.language }).then((res) => {
    // if (res.code !== 200) {
    //   message.error(res.message)
    //   return
    // }
    selection.nameList = res.data.reduce((pre, curr) => {
      let exsist = false
      pre.forEach((item) => {
        if (curr.name === item) exsist = true
      })
      if (!exsist) pre.push(curr.name)
      return pre
    }, [])
  })
}
const chooseName = async (item) => {
  data.search.name = item
  await getNameList()
  getComponents()
}

const getComponents = (number = 1, size = 8) => {
  const params = {
    ...data.search,
    number,
    size
  }
  store.dispatch('saveComSearch', { ...data.search, number, size })
  GetComponents(params)
    .then((res) => {
      // console.log('GetComponents', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      data.datasource = res.data.content
      data.datasource.forEach((item) => {
        if (item.name === '') item.name = '-'
      })
      pagination.total = res.data.totalElements
      pagination.current = number
    })
    .catch((err) => {
      console.error(err)
    })
}

const addComponent = () => {
  addModal.value.open()
}
const showInfo = (record) => {
  drawer.value.open(record, true)
}
const updateComponent = (record) => {
  updateModal.value.open(record)
}
const retry = (record) => {
  record.popconfirm = false
  updateComponent(record)
}
const deleteComponent = (record) => {
  record.popconfirm = false
  const params = {
    name: record.name,
    version: record.version,
    language: record.language instanceof Array ? 'app' : record.language
  }
  DeleteComponent(params)
    .then((res) => {
      // console.log('DeleteComponent', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      if (!res.data || res.data.length === 0) {
        message.success('删除组件成功')
        getComponents()
      } else {
        warnModal.value.open(res.data, '有以下应用依赖该组件：')
      }
    })
    .catch((err) => {
      message(err)
    })
}
</script>

<style scoped>
.main {
  position: relative;
  height: 100%;
}
.title {
  font-weight: bold;
  font-size: 20px;
  margin-bottom: 15px;
}
.content_card {
  position: absolute;
  width: 100%;
  height: calc(100% - 30px);
  overflow-y: scroll;
}
.operations {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.column_name {
  cursor: pointer;
}
.column_name:hover {
  color: #00557c;
}
.action_icon {
  margin-right: 10px;
}
.cancel_btn:hover {
  border-color: #00557c;
  color: #00557c;
}
</style>
<style scoped src="@/atdv/primary-btn.css"></style>
<style scoped src="@/atdv/delete-btn.css"></style>
<style scoped src="@/atdv/input.css"></style>
<style scoped src="@/atdv/input-search.css"></style>
<style scoped src="@/atdv/delete-btn.css"></style>
<style scoped src="@/atdv/pagination.css"></style>
<style scoped src="@/atdv/radio-btn.css"></style>
<style scoped src="@/atdv/select.css"></style>
