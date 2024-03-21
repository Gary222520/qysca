<template>
  <a-modal v-model:open="data.open" width="550px" :footer="null">
    <template #title>
      <div style="font-size: 20px">部门成员信息</div>
    </template>
    <div class="operations">
      <a-button type="primary" @click="addBuMember()"><PlusOutlined />添加成员</a-button>
    </div>
    <div class="table">
      <a-table :data-source="table.datasource" :columns="table.columns" bordered :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            {{ record.name }}
            <span v-if="record.role === 'Bu Rep'"><a-tag>Bu Rep</a-tag></span>
          </template>
          <template v-if="column.key === 'action'">
            <a-tooltip v-if="!hasBuRep() && record.role !== 'Bu PO'">
              <template #title>授予Bu Rep</template>
              <UsergroupAddOutlined
                :style="{ fontSize: '18px', color: '#6f005f', marginRight: '10px' }"
                @click="addBuRep(record)" />
            </a-tooltip>
            <a-tooltip v-if="record.role === 'Bu Rep'">
              <template #title>撤销Bu Rep</template>
              <UsergroupDeleteOutlined
                :style="{ fontSize: '18px', color: '#6f005f', marginRight: '10px' }"
                @click="deleteBuRep(record)" />
            </a-tooltip>
            <a-tooltip v-if="record.role !== 'Bu Rep' && record.role !== 'Bu PO'">
              <template #title>授予Bu PO</template>
              <UserAddOutlined
                :style="{ fontSize: '18px', color: '#6f005f', marginRight: '10px' }"
                @click="addBuPO(record)" />
            </a-tooltip>
            <a-tooltip v-if="record.role === 'Bu PO'">
              <template #title>撤销Bu PO</template>
              <UserDeleteOutlined
                :style="{ fontSize: '18px', color: '#6f005f', marginRight: '10px' }"
                @click="deleteBuPO(record)" />
            </a-tooltip>
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
    <AddPOModal ref="addPOModal" @success="getBuMemberList()"></AddPOModal>
    <DeletePOModal ref="deletePOModal" @success="getBuMemberList()"></DeletePOModal>
  </a-modal>
</template>

<script setup>
import {
  PlusOutlined,
  FileTextOutlined,
  DeleteOutlined,
  UsergroupAddOutlined,
  UsergroupDeleteOutlined,
  UserAddOutlined,
  UserDeleteOutlined
} from '@ant-design/icons-vue'
import { reactive, ref, defineExpose, defineEmits, onMounted } from 'vue'
import { GetBuMemberList, DeleteBuMember, AddBuRep, DeleteBuRep } from '@/api/frontend'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'
import AddMember from './AddMember.vue'
import AddPOModal from './AddPOModal.vue'
import DeletePOModal from './DeletePOModal.vue'

const emit = defineEmits(['success'])
const store = useStore()

const addMember = ref()
const addPOModal = ref()
const deletePOModal = ref()

const data = reactive({
  open: false,
  bu: {}
})
const table = reactive({
  datasource: [],
  columns: [
    { title: '成员编号', dataIndex: 'uid', key: 'uid', width: 150 },
    { title: '成员名称', dataIndex: 'name', key: 'name', width: 200 },
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

const addBuMember = () => {
  addMember.value.open(data.bu)
}

const deleteMember = (record) => {
  DeleteBuMember({ name: data.bu.name, uid: record.uid })
    .then((res) => {
      // console.log('DeleteBuMember', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      message.success('删除成员成功')
      getBuMemberList()
    })
    .catch((e) => {
      message.error(e)
    })
}

const addBuRep = (record) => {
  AddBuRep({ buName: data.bu.name, uid: record.uid })
    .then((res) => {
      // console.log('AddBuRep', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      message.success('授予Bu Rep成功')
      getBuMemberList()
    })
    .catch((e) => {
      message.error(e)
    })
}

const deleteBuRep = (record) => {
  DeleteBuRep({ buName: data.bu.name, uid: record.uid })
    .then((res) => {
      // console.log('DeleteBuRep', res)
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      message.success('撤销Bu Rep成功')
      getBuMemberList()
    })
    .catch((e) => {
      message.error(e)
    })
}

const addBuPO = (record) => {
  addPOModal.value.open(record)
}

const deleteBuPO = (record) => {
  deletePOModal.value.open(record)
}

const hasBuRep = () => {
  return table.datasource.some((item) => item.role === 'Bu Rep')
}

defineExpose({ open })
</script>

<style scoped>
.operations {
  margin-top: 20px;
}
.table {
  width: 500px;
  margin-top: 10px;
}
</style>
<style scoped src="@/atdv/primary-btn.css"></style>
<style scoped src="@/atdv/input.css"></style>
<style scoped src="@/atdv/select.css"></style>
