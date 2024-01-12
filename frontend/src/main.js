import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import Antd from 'ant-design-vue'

import uploader from 'vue-simple-uploader'
import 'vue-simple-uploader/dist/style.css'

createApp(App).use(store).use(router).use(Antd).use(uploader).mount('#app')
