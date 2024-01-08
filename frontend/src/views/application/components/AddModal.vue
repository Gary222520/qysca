<template>
  <a-modal class="modal" v-model:open="data.open" title="新建项目" width="800px" :footer="null">
    <div style="display: flex; margin-top: 20px">
      <a-steps class="steps" direction="vertical" :current="data.currentStep" :items="data.steps"></a-steps>
      <div v-if="data.currentStep === 0">
        <div class="content">
          <a-card class="card" hoverable @click="handleClick('java')">
            <div class="card_title">
              <img class="img" src="@/assets/java.png" />
              <div class="name">Java</div>
            </div>
            <div class="card_text">
              <ul style="margin-bottom: 0">
                <li class="list_item">Java语言开发的软件</li>
                <li class="list_item">maven或gradle管理的项目</li>
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
                <li class="list_item">Python语言开发的软件</li>
                <li class="list_item">pip工具管理的项目</li>
              </ul>
            </div>
          </a-card>
        </div>
        <div class="content" style="margin-top: 10px">
          <a-card class="card" hoverable>
            <div class="card_title">
              <img class="img" src="@/assets/go.png" />
              <div class="name">Go</div>
            </div>
            <div class="card_text">
              <ul style="margin-bottom: 0">
                <li class="list_item">Go语言开发的软件</li>
                <li class="list_item">Go.mod工具管理的项目</li>
              </ul>
            </div>
          </a-card>
          <a-card class="card" hoverable>
            <div class="card_title">
              <img class="img" src="@/assets/javascript.png" />
              <div class="name">JavaScript</div>
            </div>
            <div class="card_text">
              <ul style="margin-bottom: 0">
                <li class="list_item">JavaScript语言开发的软件</li>
                <li class="list_item">npm工具管理的NodeJS项目</li>
              </ul>
            </div>
          </a-card>
        </div>
      </div>
      <div v-if="data.currentStep === 1">step 2</div>
    </div>
  </a-modal>
</template>

<script>
import { reactive } from 'vue'
export default {
  setup() {
    const data = reactive({
      open: false,
      currentStep: 0,
      steps: [{ title: '选择语言' }, { title: '上传项目' }],
      language: 'java'
    })
    const open = () => {
      data.open = true
    }
    const handleClick = (language) => {
      data.language = language
      data.currentStep = 1
    }
    return { data, open, handleClick }
  }
}
</script>

<style lang="less" scoped>
.steps {
  width: 150px;
}
:deep(.ant-steps-item) {
  height: 100px;
}
.content {
  width: 600px;
  display: flex;
}
.card {
  width: 50%;
  margin-right: 10px;
}
.card_title {
  display: flex;
  align-items: center;
}
.img {
  height: 60px;
  margin-left: 10px;
}
.name {
  font-size: 28px;
  font-weight: bold;
  margin-left: 20px;
}
.card_text {
  margin-top: 10px;
}
.list_item {
  margin-top: 5px;
}
</style>
