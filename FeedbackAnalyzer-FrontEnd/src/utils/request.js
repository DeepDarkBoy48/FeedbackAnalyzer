//定制请求的实例

//导入axios  npm install axios
import axios from 'axios';
//导入消息提示框element组件
import { ElMessage } from 'element-plus'
//定义一个变量,记录公共的前缀  ,  baseURL
const baseURL = '/api';
const instance = axios.create({ baseURL })
import {useTokenStore} from '@/stores/token.js'
// Add a request interceptor
instance.interceptors.request.use(
    config=>{

        //add token
        const tokenStore = useTokenStore();
        //判断有没有token 英文：if there is a token
        if(tokenStore.token){
            config.headers.Authorization = tokenStore.token
        }
        return config;
    },
    err=>{
        alert('service error');
        //请求错误的回调
        //wrong request
        return Promise.reject(err)
    }
)

/* import {useRouter} from 'vue-router'
const router = useRouter(); */

import router from '@/router'
//添加响应拦截器
instance.interceptors.response.use(
    result => {
        // return result.data;
        //判断业务状态码
        if (result.data.code === 0) {
            return result.data;
        }

        //操作失败
        // alert(result.data.msg ? result.data.msg : '服务异常')
        ElMessage.error(result.data.message ? result.data.message : 'request.js服务异常')
        //异步操作的状态转换为失败
        return Promise.reject(result.data)
    },
    err => {
        if(err.response.status===401){
            ElMessage.error('请先登录')
            router.push('/login')
        }else{
            ElMessage.error('request.js服务异常')
        }
        return Promise.reject(err);//异步的状态转化成失败的状态
    }
)

export default instance;