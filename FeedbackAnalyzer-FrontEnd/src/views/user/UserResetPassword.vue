<script setup>
import {ref} from 'vue'
//密码模型
const passwordModel = ref({
  old_pwd: '',
  new_pwd: '',
  re_pwd: ''
})

const checkRePassword = (rule, value, callback) => {
  // 函数接收三个参数：
  // rule: 当前校验规则对象
  // value: 当前校验的值（即确认密码的值）
  // callback: 回调函数，用于返回校验结果
  if (value === '') {
    callback(new Error('请再次确认密码'))
  } else if (value !== passwordModel.value.new_pwd) {
    callback(new Error('请确保两次输入的密码一样'))
  } else {
    callback()// 验证通过
  }
}

const rules = {
  old_pwd: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 5, max: 16, message: '长度为5~16位非空字符', trigger: 'blur'}
  ],
  new_pwd: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 5, max: 16, message: '长度为5~16位非空字符', trigger: 'blur'}
  ],
  re_pwd: [
    {
      validator: checkRePassword, // 自定义校验函数，用于验证确认密码是否与密码一致
      trigger: 'blur' // 触发校验的事件，这里设置为失去焦点时触发
    }
  ]
}
import {ElMessage} from "element-plus";
import {userPswUpdateService} from '@/api/user.js';
//提交密码
const setpwd = async ()=>{
  let result = await userPswUpdateService(passwordModel.value);
  if (result.code === 0){
    ElMessage.success(result.message ? result.message : '添加成功')
  }else {
    ElMessage.error(result.message ? result.message : '添加失败')
  }
}

</script>
<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>basic information</span>
      </div>
    </template>
    <el-row>
      <el-col :span="12">
        <el-form label-width="100px" size="large" :model="passwordModel" :rules="rules">
          <el-form-item label="old" prop="old_pwd">
            <el-input v-model="passwordModel.old_pwd"></el-input>
          </el-form-item>
          <el-form-item label="new" prop="new_pwd">
            <el-input v-model="passwordModel.new_pwd"></el-input>
          </el-form-item >
          <el-form-item label="re-enter" prop="re_pwd">
            <el-input v-model="passwordModel.re_pwd"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="setpwd()">submit</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
  </el-card>
</template>