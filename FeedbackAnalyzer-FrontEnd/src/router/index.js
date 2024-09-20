import {createRouter, createWebHistory} from 'vue-router'

//导入主路由组件
import loginVue from '@/views/login.vue'
import layoutVue from '@/views/layout.vue'
//子路由组件
// import ArticleCategoryVue from '@/views/article/ArticleCategory.vue'
// import ArticleManageVue from '@/views/article/ArticleManage.vue'
import UserAvatarVue from '@/views/user/UserAvatar.vue'
import UserInfoVue from '@/views/user/UserInfo.vue'
import UserResetPasswordVue from '@/views/user/UserResetPassword.vue'
import student from "@/views/student/student.vue";
import teacher from "@/views/teacher/teacher.vue";
import teacherGenerateFeedbackVue from "@/views/teacher/teacherGenerateFeedback.vue";
import teacherViewResultVue from "@/views/teacher/teacherViewResult.vue";

//定义路由关系
const routes = [
    {path: '/login', component: loginVue},
    // {
    //     path: '/', component: layoutVue, redirect: '/article/category', children: [
    //         {path: '/article/category', component: ArticleCategoryVue},
    //         {path: '/article/manage', component: ArticleManageVue},
    //         {path: '/user/info', component: UserInfoVue},
    //         {path: '/user/avatar', component: UserAvatarVue},
    //         {path: '/user/resetPassword', component: UserResetPasswordVue}
    //     ]
    // },
    {path: '/', redirect: '/login'},
    {
        path: '/student', component: student, redirect: '/student/article/category', children: [
            // {path: '/student/article/category', component: ArticleCategoryVue},
            // {path: '/student/article/manage', component: ArticleManageVue},
            {path: '/student/user/info', component: UserInfoVue},
            {path: '/student/user/avatar', component: UserAvatarVue},
            {path: '/student/user/resetPassword', component: UserResetPasswordVue}
        ]
    },
    {
        path: '/teacher', component: teacher, redirect: '/teacher/ViewResult', children: [
            // {path: '/teacher/article/category', component: ArticleCategoryVue},
            // {path: '/teacher/article/manage', component: ArticleManageVue},
            {path: '/teacher/user/info', component: UserInfoVue},
            {path: '/teacher/user/avatar', component: UserAvatarVue},
            {path: '/teacher/user/resetPassword', component: UserResetPasswordVue},
            {path: '/teacher/GenerateFeedback', component: teacherGenerateFeedbackVue},
            {path: '/teacher/ViewResult', component: teacherViewResultVue},
        ]
    },
]

//创建路由器
const router = createRouter({
    history: createWebHistory(),
    routes: routes
})

//导出路由
export default router;














