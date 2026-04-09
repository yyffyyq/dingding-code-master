import { createApp } from 'vue'
import App from './App.vue'

import router from './router/index.ts'

// 引入 Element Plus
import ElementPlus from 'element-plus'
// 引入 Element Plus 样式
import 'element-plus/dist/index.css'
import {createPinia} from "pinia";

const app = createApp(App)

// 全局注册
app.use(createPinia())
app.use(router)

app.use(ElementPlus)
app.mount('#app')