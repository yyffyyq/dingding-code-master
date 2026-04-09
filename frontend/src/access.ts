import { userLoginUserStore } from '@/stors/loginUser.ts'
import { message } from 'ant-design-vue'
import router from '@/router'

// 是否为首次登录用户
let firstFetchLoginUser = true

/**
 * 全局权限校验
 */
router.beforeEach(async (to,_from,next)=>{
    const loginUserStore = userLoginUserStore()
    let loginUser = loginUserStore.loginUser

    // 确保页面刷新，首次加载时，能够等后端返回用户信息后再校验权限
    if (firstFetchLoginUser){
        await loginUserStore.fetchLoginUser()
        loginUser = loginUserStore.loginUser
        firstFetchLoginUser = false
    }
    const toUrl = to.fullPath
    if (toUrl.startsWith('/admin')){
        if (!loginUser || loginUser.userRole !== 'admin'){
            message.error('没有权限')
            next(`/dingUser/login?redirect=${to.fullPath}`)
            return
        }
    }

    // 校验只有登录用户能够查看的页面
    if (toUrl.startsWith('/test')) {
        if (!loginUser || !loginUser.id) {
            message.error('请先登录')
            next(`/dingUser/login?redirect=${to.fullPath}`)
            return
        }
    }
    next()

})
