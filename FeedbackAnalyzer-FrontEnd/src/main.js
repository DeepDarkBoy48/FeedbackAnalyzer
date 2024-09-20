import './assets/main.scss'

import { createApp } from 'vue'
//导入elementplus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
//导入路由器
import router from '@/router/index.js'
//导入pinia
import {createPinia} from "pinia";
//导入pinia持久化插件
import { createPersistedState } from 'pinia-persistedstate-plugin'
//导入中文语言包
// import locale from 'element-plus/dist/locale/zh-cn.js'

import App from './App.vue'
const app = createApp(App);
const pinia = createPinia();
const persist = createPersistedState();
pinia.use(persist)
app.use(pinia)
app.use(ElementPlus);
app.use(router);
//#app添加在index.html中的<div id="app"></div>
app.mount('#app')
// app.use(ElementPlus,{locale});
