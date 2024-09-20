<script setup>
import layout from '../layout.vue'
import {
  Management,
  Promotion,
  UserFilled,
  User,
  Crop,
  EditPen,
  SwitchButton,
  CaretBottom
} from '@element-plus/icons-vue'
</script>

<template>
  <layout>

    <template #item>
      <el-menu-item index="/teacher/GenerateFeedback">
        <el-icon>
          <Promotion/>
        </el-icon>
        <span>Create Task</span>
      </el-menu-item>
    </template>
    <template #result>
      <el-menu-item index="/teacher/ViewResult">
        <el-icon>
          <Promotion/>
        </el-icon>
        <span>Manage Task</span>
      </el-menu-item>
    </template>


    <template #userInfo>
      <el-menu-item index="/teacher/user/info">
        <el-icon>
          <User/>
        </el-icon>
        <span>Basic information</span>
      </el-menu-item>
    </template>


    <template #userAvatar>
      <el-menu-item index="/teacher/user/avatar">
        <el-icon>
          <Crop/>
        </el-icon>
        <span>Change Avatar</span>
      </el-menu-item>
    </template>

    <template #userResetPassword>
      <el-menu-item index="/teacher/user/resetPassword">
        <el-icon>
          <EditPen/>
        </el-icon>
        <span>Reset Password</span>
      </el-menu-item>
    </template>
  </layout>
</template>


<style scoped>
</style>
