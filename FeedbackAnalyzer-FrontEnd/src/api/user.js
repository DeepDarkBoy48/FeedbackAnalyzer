//导入request.js请求工具
import request from '@/utils/request.js'

//提供调用注册接口的函数
export const userRegisterService = (registerData) => {
    //借助于UrlSearchParams完成传递x-www-form-urlencoded格式数据
    const params = new URLSearchParams();
    for (let key in registerData) {
        params.append(key, registerData[key])
    }
    return request.post('/user/register', params)
}

//提供调用登录接口的函数
export const userLoginService = (loginData) => {
    //借助于UrlSearchParams完成传递x-www-form-urlencoded格式数据
    const params = new URLSearchParams();
    for (let key in loginData) {
        params.append(key, loginData[key])
    }
    return request.post('/user/login', params)
}

//获取用户详细信息
export const userInfoService = ()=>{
    return request.get('/user/userInfo')
}

//更新用户信息
export const userInfoUpdateService = (userData) =>{
    return request.put('/user/update',userData);
}

//修改头像
export const userAvatarUpdateService = (avatarUrl)=>{
    const params = new URLSearchParams();
    params.append('avatarUrl',avatarUrl)
    return request.patch('/user/updateAvatar',params)
}

//修改密码
export const userPswUpdateService = (pswData)=>{
    return request.patch('/user/updatePwd',pswData)
}
