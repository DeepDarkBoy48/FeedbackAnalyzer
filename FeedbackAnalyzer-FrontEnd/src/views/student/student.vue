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
    <template #category>
      <el-menu-item index="/student/article/category">
        <el-icon>
          <Promotion/>
        </el-icon>
        <span>文章分类</span>
      </el-menu-item>
    </template>

    <template #manage>
      <el-menu-item index="/student/article/manage">
        <el-icon>
          <Management/>
        </el-icon>
        <span>文章管理</span>
      </el-menu-item>
    </template>

    <template #title>
      <div>
        <!-- 这里是 teacher.vue 特有的内容 -->
        <h1>Student's Specific Content</h1>
        <p>This is the student page.</p>
      </div>
    </template>

    <template #userInfo>
      <el-menu-item index="/student/user/info">
        <el-icon>
          <User/>
        </el-icon>
        <span>基本资料</span>
      </el-menu-item>
    </template>


    <template #userAvatar>
      <el-menu-item index="/student/user/avatar">
        <el-icon>
          <Crop/>
        </el-icon>
        <span>更换头像</span>
      </el-menu-item>
    </template>

    <template #userResetPassword>
      <el-menu-item index="/student/user/resetPassword">
        <el-icon>
          <EditPen/>
        </el-icon>
        <span>重置密码</span>
      </el-menu-item>
    </template>
  </layout>
</template>


<style scoped>
/* 这里是 teacher.vue 特有的样式 */
</style>

