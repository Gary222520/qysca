<template>
  <a-modal v-model:open="data.open" :footer="null">
    <template #title>
      <div style="font-size: 20px">部门成员信息</div>
    </template>
    <div class="operations">
      <a-button type="primary" @click="addBuMember(true)"><PlusOutlined />添加Bu Rep</a-button>
      <a-button type="primary" @click="addBuMember(false)" style="margin-left: 10px"><PlusOutlined />添加成员</a-button>
    </div>
    <div class="table">
      <a-table :data-source="table.datasource" :columns="table.columns" bordered :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-tooltip>
              <template #title>删除</template>
              <a-popconfirm v-model:open="record.popconfirm" title="确定删除该成员吗？">
                <template #cancelButton>
                  <a-button class="cancel_btn" size="small" @click="record.popconfirm = false">取消</a-button>
                </template>
                <template #okButton>
                  <a-button danger type="primary" size="small" @click="deleteMember(record)">删除</a-button>
                </template>
                <DeleteOutlined :style="{ fontSize: '18px', color: '#ff4d4f' }" />
              </a-popconfirm>
            </a-tooltip>
          </template>
        </template>
        <template #emptyText>暂无部门</template>
      </a-table>
    </div>
    <AddMember ref="addMember" @success="getBuMemberList()"></AddMember>
  </a-modal>
</template>

<script setup>
import { PlusOutlined, FileTextOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { reactive, ref, defineExpose, defineEmits, onMounted } from 'vue'
import { GetBuMemberList, DeleteBuMember } from '@/api/frontend'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'
import AddMember from './AddMember.vue'

const emit = defineEmits(['success'])
const store = useStore()

const addMember = ref()

const data = reactive({
  open: false,
  bu: {}
})
const table = reactive({
  datasource: [],
  columns: [
    { title: '成员编号', dataIndex: 'uid', key: 'uid', width: 150 },
    { title: '成员名称', dataIndex: 'name', key: 'name', width: 150 },
    { title: '操作', dataIndex: 'action', key: 'action', width: 150 }
  ]
})

const open = (record) => {
  data.open = true
  data.bu = record
  getBuMemberList()
}
const close = () => {
  data.open = false
}
const clear = () => {}

const getBuMemberList = () => {
  GetBuMemberList({ name: data.bu.name })
    .then((res) => {
      // console.log('GetBuMemberList', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      table.datasource = res.data
    })
    .catch((e) => {
      message.error(e)
    })
}

const addBuMember = (isRep) => {
  addMember.value.open(isRep, data.bu)
}

const deleteMember = (record) => {
  DeleteBuMember({ name: data.bu.name, uid: record.uid })
    .then((res) => {
      // console.log('DeleteBuMember', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      getBuMemberList()
    })
    .catch((e) => {
      message.error(e)
    })
}

defineExpose({ open })
</script>

<style scoped>
.operations {
  margin-top: 20px;
}
.table {
  width: 450px;
  margin-top: 10px;
}
</style>
<style scoped src="@/atdv/primary-btn.css"></style>
<style scoped src="@/atdv/input.css"></style>
<style scoped src="@/atdv/select.css"></style>
