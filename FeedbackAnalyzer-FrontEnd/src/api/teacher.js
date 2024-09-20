import request from '@/utils/request.js'
import {useTokenStore} from "@/stores/token.js";
//文章分类列表查询

//文章分类添加
export const teacherGenerateFeedbackService = (form)=>{
    return request.post('/teacher/analyze/form',form)
}
//获取course列表
export const courseListService = (params)=>{
    return  request.get('/teacher/feedBack/course',{params:params})
}
//删除course
export const courseDeleteService = (id) =>{
    console.log(id)
    return request.delete('/teacher/feedBack/course?id='+id)
}
//获取courseitem列表
export const courseItemListService = (id)=>{
    return request.get('/teacher/feedBack/courseItem?courseId='+id)
}

//获取courseitem的feedback列表
export const courseItemFeedbackListService = (params)=>{
    return request.get('/teacher/feedBack/courseItemFeedback', { params });
}
//获取ai的分析
export const aiAnalyzeService = (dataToSend)=>{
    return request.post('/teacher/feedBack/aiAnalyze', dataToSend);
}