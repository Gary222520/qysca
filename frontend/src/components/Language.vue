<template>
  <div class="main">
    <div class="content">
      <a-card class="card" hoverable @click="selectLanguage('java')">
        <div class="card_title">
          <img class="img" src="@/assets/java.png" />
          <div class="name">Java</div>
        </div>
        <div class="card_text">
          <ul style="margin-bottom: 0">
            <li class="list_item">Java语言开发的应用</li>
            <li class="list_item">maven或gradle管理的应用</li>
          </ul>
        </div>
      </a-card>
      <a-card class="card" hoverable @click="selectLanguage('python')">
        <div class="card_title">
          <img class="img" src="@/assets/python.png" />
          <div class="name">Python</div>
        </div>
        <div class="card_text">
          <ul>
            <li class="list_item">Python语言开发的应用</li>
            <li class="list_item">pip工具管理的应用</li>
          </ul>
        </div>
      </a-card>
    </div>
    <div class="content" style="margin-top: 10px">
      <a-card class="card" hoverable @click="selectLanguage('golang')">
        <div class="card_title">
          <img class="img" src="@/assets/go.png" />
          <div class="name">GoLang</div>
        </div>
        <div class="card_text">
          <ul style="margin-bottom: 0">
            <li class="list_item">Go语言开发的应用</li>
            <li class="list_item">go.mod工具管理的应用</li>
          </ul>
        </div>
      </a-card>
      <a-card class="card" hoverable @click="selectLanguage('javaScript')">
        <div class="card_title">
          <img class="img" src="@/assets/javascript.png" />
          <div class="name">JavaScript</div>
        </div>
        <div class="card_text">
          <ul style="margin-bottom: 0">
            <li class="list_item">JavaScript语言开发的应用</li>
            <li class="list_item">npm工具管理的Node.js应用</li>
          </ul>
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, defineEmits } from 'vue'

const data = reactive({})
const emit = defineEmits(['select'])

const selectLanguage = (lang) => {
  emit('select', lang)
}
</script>

<style scoped>
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
