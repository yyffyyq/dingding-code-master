import { ref } from 'vue'
import { defineStore } from 'pinia'
import { health } from '@/api/modules/ding-user'

/**
 * 登录用户信息的全局包
 */

export const userLoginUserStore = defineStore('loginUser', () => {

    //全局默认值
    const loginUser = ref<API.SysUserVO>({
        nickName: '未查询到登录',
    })

    // 获取用户登录信息
    async function fetchLoginUser(){
        const res = await health()
        if(res.data.code === 0 && res.data.data){
            loginUser.value = res.data.data
        }
    }

    function setLoginUser(newLoginUser: any){
        loginUser.value = newLoginUser;
    }

    return{
        loginUser, fetchLoginUser, setLoginUser
    }

})
