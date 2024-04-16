<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider class="sider" :collapsed="data.collapsed" :trigger="null" collapsible>
      <div class="title">
        <img style="width: 40px" src="@/assets/logo_big.png" />
        <div class="title_text" v-if="!data.collapsed">SCA</div>
      </div>
      <a-menu v-model:selectedKeys="data.selectedKeys" theme="light" mode="inline" @click="handleMenu">
        <a-menu-item v-if="permit('summary')" key="summary">
          <PieChartOutlined class="menu_icon" :style="{ fontSize: '18px' }" />
          <span>{{ getTitle('summary') }}</span>
        </a-menu-item>
        <a-menu-item v-if="permit('application')" key="application">
          <ScheduleOutlined class="menu_icon" :style="{ fontSize: '18px' }" />
          <span>{{ getTitle('application') }}</span>
        </a-menu-item>
        <a-menu-item v-if="permit('component')" key="component">
          <AppstoreOutlined class="menu_icon" :style="{ fontSize: '18px' }" />
          <span>{{ getTitle('component') }}</span>
        </a-menu-item>
        <a-menu-item v-if="permit('vulnerability')" key="vulnerability">
          <BugOutlined class="menu_icon" :style="{ fontSize: '18px' }" />
          <span>{{ getTitle('vulnerability') }}</span>
        </a-menu-item>
        <a-menu-item v-if="permit('license')" key="license">
          <FileProtectOutlined class="menu_icon" :style="{ fontSize: '18px' }" />
          <span>{{ getTitle('license') }}</span>
        </a-menu-item>
        <a-menu-item v-if="permit('buManage')" key="buManage">
          <GroupOutlined class="menu_icon" :style="{ fontSize: '18px' }" />
          <span>{{ getTitle('buManage') }}</span>
        </a-menu-item>
        <a-menu-item v-if="permit('userManage')" key="userManage">
          <UserOutlined class="menu_icon" :style="{ fontSize: '18px' }" />
          <span>{{ getTitle('userManage') }}</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <menu-unfold-outlined v-if="data.collapsed" class="trigger" @click="() => (data.collapsed = !data.collapsed)" />
        <menu-fold-outlined v-else class="trigger" @click="() => (data.collapsed = !data.collapsed)" />
        <div class="user">
          <a-dropdown style="height: 50px">
            <span>
              <a-avatar style="color: #ffffff; background-color: #00557c">
                <template #icon><UserOutlined /></template>
              </a-avatar>
              <span style="margin-left: 10px; font-size: 16px">{{ data.username }}</span>
            </span>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="logout">退出登录</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import routes from '@/router/routeTable'
import {
  AppstoreOutlined,
  SearchOutlined,
  ScheduleOutlined,
  LayoutOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  PieChartOutlined,
  UserOutlined,
  GroupOutlined,
  BugOutlined,
  FileProtectOutlined
} from '@ant-design/icons-vue'
import { Logout } from '@/api/frontend'
import { message } from 'ant-design-vue'

const router = useRouter()
const store = useStore()

onMounted(() => {
  data.selectedKeys = [router.currentRoute.value.meta.menu]
  data.username = JSON.parse(sessionStorage.getItem('user')).user.name
})

const data = reactive({
  collapsed: false,
  selectedKeys: [],
  username: ''
})
const handleMenu = ({ item, key, keyPath }) => {
  router.push(`/home/${key}`)
}
const getTitle = (key) => {
  const homeRoutes = routes.find((item) => item.name === 'home')
  return homeRoutes.children.find((item) => item.name === key)?.meta.title
}
const permit = (menu) => {
  const permission = JSON.parse(sessionStorage.getItem('user')).userBuAppRoles
  let res = true
  switch (menu) {
    case 'summary':
    case 'application':
    case 'component':
    case 'vulnerability':
    case 'license':
      res = !permission.some((item) => item.role === 'Admin')
      break
    case 'buManage':
      res = permission.some((item) => item.role === 'Admin')
      break
    case 'userManage':
      res = permission.some((item) => item.role === 'Admin')
      break
  }
  return res
}
const logout = () => {
  Logout()
    .then((res) => {
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      // console.log('Logout', res)
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
      router.push('/login')
    })
    .catch((err) => {
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
      router.push('/login')
      console.error(err)
    })
}
</script>

<style lang="less" scoped>
.sider {
  background: #fff;
}
.header {
  background: #fff;
  padding: 0 20px;
  height: 50px;
  line-height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.user {
  margin-right: 10px;
}
:deep(.ant-dropdown-trigger) {
  display: flex;
  align-items: center;
}
.content {
  margin: 15px;
}
.title {
  background-color: #fff;
  height: 50px;
  display: flex;
  align-items: center;
  padding-left: 20px;
}
.title_text {
  height: 40px;
  line-height: 40px;
  font-weight: bold;
  font-size: 30px;
  margin-left: 10px;
  color: #00557c;
  font-family: 'Arial Rounded MT';
}
.menu_icon {
  width: 18px;
  margin-right: 12px;
}
:deep(.ant-menu-item) {
  width: 90%;
  margin-bottom: 10px;
}
:deep(.ant-menu-title-content) {
  display: flex;
  align-items: center;
  font-size: 15px;
}
:deep(.ant-menu-item-selected) {
  background-color: #00557c;
  color: #fff;
  font-weight: 700;
}
</style>
