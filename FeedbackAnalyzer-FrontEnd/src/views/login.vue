<script setup>
//导入icons，user和lock和loading
import { User, Lock, Loading } from '@element-plus/icons-vue'
import { ref } from 'vue'
//导入消息提示框element组件
import { ElMessage } from 'element-plus'
//控制注册与登录表单的显示， 默认显示注册
const isRegister = ref(0)

//用于注册的数据模型
const registerData = ref({
  username: '',
  password: '',
  rePassword: '',
  role: '',
})

const checkRePassword = (rule, value, callback) => {
  // 函数接收三个参数：
  // rule: 当前校验规则对象
  // value: 当前校验的值（即确认密码的值）
  // callback: 回调函数，用于返回校验结果
  if (value === '') {
    callback(new Error('Please reconfirm your password'))
  } else if (value !== registerData.value.password) {
    callback(new Error('Make sure you enter the same password twice'))
  } else {
    callback()// 验证通过
  }
}

//定义表单校验规则
const rules = {
  username: [
    {
      required: true, // 用户名是必填项
      message: 'Username is required', // 当用户名为空时，显示的提示信息
      trigger: 'blur' // 触发校验的事件，这里设置为失去焦点时触发
    },
    {
      min: 5, // 用户名的最小长度
      max: 16, // 用户名的最大长度
      message: 'User name length between 5 and 16 characters', // 当用户名长度不符合要求时，显示的提示信息
      trigger: 'blur' // 触发校验的事件，这里设置为失去焦点时触发
    }
  ],
  password: [
    { required: true, message: 'Please enter your password', trigger: 'blur' },
    { min: 5, max: 16, message: 'Length of 5~16 non-null characters', trigger: 'blur' }
  ],
  rePassword: [
    {
      validator: checkRePassword, // 自定义校验函数，用于验证确认密码是否与密码一致
      trigger: 'blur' // 触发校验的事件，这里设置为失去焦点时触发
    }
  ],
  role:[
    {
      required: true, // 用户名是必填项
      message: 'Role is required', // 当用户名为空时，显示的提示信息
      trigger: 'blur' // 触发校验的事件，这里设置为失去焦点时触发
    }
  ],
}

//调用后台接口，完成注册
import { userRegisterService } from "@/api/user.js"

const register = async () => {
  registerData.value.role=1;
  let result = await userRegisterService(registerData.value);
  ElMessage.success(result.msg ? result.msg : 'register success');
  ElMessage.success(result.data);
}

//绑定数据,复用注册表单的数据模型
//表单数据校验
//登录函数
import { userLoginService } from '@/api/user.js';
//导入路由
import { useRouter } from 'vue-router'
//导入pinia，类似于threadlocal
import { useTokenStore } from '@/stores/token.js'
import { jwtDecode } from 'jwt-decode';
const tokenStore = useTokenStore();
const route = useRouter()
const login = async () => {
  //调用接口完成登录
  let result = await userLoginService(registerData.value);
  //使用Element的message美化
  ElMessage.success(result.msg ? result.msg : 'Login Successful');
  // ElMessage.success(result.data);
  //把得到的token存储到pinia中
  tokenStore.setToken(result.data)
  //解析jwt
  let jwt = jwtDecode(result.data);
  let role = jwt.claims.role;
  //跳转到首页，路由完成跳转
  if (role === 0) {
    ElMessage.success(result.data);
    route.push('/student')
  } else {
    route.push('/teacher')
  }
}

//定义函数,清空数据模型的数据
const clearRegisterData = () => {
  registerData.value = {
    username: '',
    password: '',
    rePassword: ''
  }
}

</script>

<template>
  <el-row class="login-page">
    <el-col :span="12" class="bg"></el-col>
    <el-col :span="8" :offset="2" class="form">
      <!-- 注册表单 -->
      <!-- :model相当于v-model双向绑定 -->
      <!--      v-model="registerData.username"绑定字段内容-->
      <!-- :rules="rules" 绑定规则 -->
      <!-- prop="username"绑定规则中的字段 -->
      <el-form ref="form" size="large" autocomplete="off" v-if="isRegister == 1" :model="registerData" :rules="rules">
        <el-form-item>
          <h1>register</h1>
        </el-form-item>
        <el-form-item prop="username">
          <el-input :prefix-icon="User" placeholder="username" v-model="registerData.username"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input :prefix-icon="Lock" type="password" placeholder="password" v-model="registerData.password"></el-input>
        </el-form-item>
        <el-form-item prop="rePassword">
          <el-input :prefix-icon="Lock" type="password" placeholder="re-enter password"
            v-model="registerData.rePassword"></el-input>
        </el-form-item>
        <el-form-item prop="role">
          <!-- 隐藏的输入框，用户看不到，但表单提交时会包含这个值 -->
          <el-input
              type="hidden"
              v-model="registerData.role"
          ></el-input>
        </el-form-item>
<!--        <el-form-item prop="role">-->
<!--          <el-input :prefix-icon="Lock" type="text" placeholder="0 for student 1 for teacher"-->
<!--            v-model="registerData.role"></el-input>-->
<!--        </el-form-item>-->

        <!-- 注册按钮 -->
        <el-form-item>
          <el-button class="button" type="primary" auto-insert-space @click="register">
            register
          </el-button>
        </el-form-item>
        <el-form-item class="flex">
          <el-link type="info" :underline="false" @click="isRegister = 0; clearRegisterData()">
            ← log in
          </el-link>
        </el-form-item>
      </el-form>
      <!-- 登录表单 -->
      <el-form ref="form" size="large" autocomplete="off" v-else :model="registerData" :rules="rules">
        <el-form-item>
          <h1>log in</h1>
        </el-form-item>
        <el-form-item prop="username">
          <!-- prefix-icon="User"设置icons -->
          <el-input :prefix-icon="User" placeholder="username" v-model="registerData.username"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input name="password" :prefix-icon="Lock" type="password" placeholder="password"
            v-model="registerData.password"></el-input>
        </el-form-item>
        <el-form-item class="flex">

        </el-form-item>
        <!-- 登录按钮 -->
        <el-form-item>
          <el-button class="button" type="primary" auto-insert-space @click="login">log in</el-button>
        </el-form-item>
        <el-form-item class="flex">
          <el-link type="info" :underline="false" @click="isRegister = 1; clearRegisterData()">
            register →
          </el-link>
        </el-form-item>
      </el-form>
    </el-col>
  </el-row>
</template>

<style lang="scss" scoped>
/* 样式 */
.login-page {
  height: 100vh;
  background-color: #fff;

  .bg {
    background: url('@/assets/logo2.png') no-repeat 45% 20% / 240px auto,
      url('@/assets/login_bg.jpg') no-repeat center / cover;
    border-radius: 0 20px 20px 0;
  }

  .form {
    display: flex;
    flex-direction: column;
    justify-content: center;
    user-select: none;

    .title {
      margin: 0 auto;
    }

    .button {
      width: 100%;
    }

    .flex {
      width: 100%;
      display: flex;
      justify-content: space-between;
    }
  }
}
</style>