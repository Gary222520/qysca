<template>
  <div>
    <div class="title">扫描分析</div>
    <div class="content">
      <a-card class="card" hoverable @click="handleClick">
        <div class="card_title">
          <img class="img" src="@/assets/java.png" />
          <div class="name">Java</div>
        </div>
        <div class="card_text">
          <ul>
            <li class="list_item">针对Java语言开发的软件进行成分依赖分析</li>
            <li class="list_item">支持maven构建并管理的项目</li>
            <li class="list_item">支持gradle构建并管理的项目</li>
          </ul>
        </div>
      </a-card>
      <a-card class="card" hoverable>
        <div class="card_title">
          <img class="img" src="@/assets/python.png" />
          <div class="name">Python</div>
        </div>
        <div class="card_text">
          <ul>
            <li class="list_item">针对Python语言开发的软件进行成分依赖分析</li>
            <li class="list_item">支持采用pip包管理器管理的项目</li>
          </ul>
        </div>
      </a-card>
    </div>
    <div class="content">
      <a-card class="card" hoverable>
        <div class="card_title">
          <img class="img" src="@/assets/go.png" />
          <div class="name">Go</div>
        </div>
        <div class="card_text">
          <ul>
            <li class="list_item">针对Go语言开发的软件进行成分依赖分析</li>
            <li class="list_item">支持Go.mod包管理工具管理的项目</li>
          </ul>
        </div>
      </a-card>
      <a-card class="card" hoverable>
        <div class="card_title">
          <img class="img" src="@/assets/javascript.png" />
          <div class="name">JavaScript</div>
        </div>
        <div class="card_text">
          <ul>
            <li class="list_item">针对JavaScript语言开发的软件进行成分依赖分析</li>
            <li class="list_item">支持npm包管理工具管理的NodeJS项目</li>
          </ul>
        </div>
      </a-card>
    </div>
    <ScanModal ref="scanModal"></ScanModal>
  </div>
</template>

<script>
import { reactive, ref } from 'vue'
import ScanModal from './components/ScanModal.vue'
export default {
  components: {
    ScanModal
  },
  setup() {
    const scanModal = ref()
    const data = reactive({})
    const handleClick = () => {
      scanModal.value.open()
    }
    return { scanModal, data, handleClick }
  }
}
</script>

<style scoped>
.title {
  font-weight: bold;
  font-size: 24px;
  margin-bottom: 15px;
}
.content {
  display: flex;
  margin-bottom: 15px;
}
.card {
  width: 50%;
  height: 300px;
  margin-right: 20px;
}
.card_title {
  display: flex;
  align-items: center;
  /* justify-content: center; */
}
.img {
  height: 80px;
  margin-left: 15px;
}
.name {
  font-size: 40px;
  font-weight: bold;
  margin-left: 20px;
}
.card_text {
  margin-top: 20px;
  font-size: 16px;
}
.list_item {
  margin-bottom: 10px;
}
/* .card:hover {
  background-color: #6f005f;
  color: #fff;
} */
</style>
