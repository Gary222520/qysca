<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider class="sider" :collapsed="data.collapsed" :trigger="null" collapsible>
      <div class="title">
        <img style="width: 40px" src="@/assets/logo_big.png" />
        <div class="title_text" v-if="!data.collapsed">SCA</div>
      </div>
      <a-menu v-model:selectedKeys="data.selectedKeys" theme="light" mode="inline" @click="handleMenu">
        <!-- <a-menu-item key="summary">
          <PieChartOutlined class="menu_icon" :style="{ fontSize: '18px' }" />
          <span>{{ getTitle('summary') }}</span>
        </a-menu-item>
        <a-menu-item key="scan">
          <SearchOutlined class="menu_icon" :style="{ fontSize: '18px' }" />
          <span>{{ getTitle('scan') }}</span>
        </a-menu-item> -->
        <a-menu-item key="application">
          <ScheduleOutlined class="menu_icon" :style="{ fontSize: '18px' }" />
          <span>{{ getTitle('application') }}</span>
        </a-menu-item>
        <a-menu-item key="project">
          <LayoutOutlined class="menu_icon" :style="{ fontSize: '18px' }" />
          <span>{{ getTitle('project') }}</span>
        </a-menu-item>
        <a-menu-item key="component">
          <AppstoreOutlined class="menu_icon" :style="{ fontSize: '18px' }" />
          <span>{{ getTitle('component') }}</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <menu-unfold-outlined v-if="data.collapsed" class="trigger" @click="() => (data.collapsed = !data.collapsed)" />
        <menu-fold-outlined v-else class="trigger" @click="() => (data.collapsed = !data.collapsed)" />
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
import routes from '@/router/routeTable'
import {
  AppstoreOutlined,
  SearchOutlined,
  ScheduleOutlined,
  LayoutOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  PieChartOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const data = reactive({
  collapsed: false,
  selectedKeys: [router.currentRoute.value.meta.menu]
})
const handleMenu = ({ item, key, keyPath }) => {
  router.push(`/home/${key}`)
}
const getTitle = (key) => {
  const homeRoutes = routes.find((item) => item.name === 'home')
  return homeRoutes.children.find((item) => item.name === key)?.meta.title
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
  color: #6f005f;
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
  background-color: #6f005f;
  color: #fff;
  font-weight: 700;
}
</style>
