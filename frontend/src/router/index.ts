import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/home/index.vue'
import UserLogin from '../views/login-user/index.vue'
import UserInfo from '../views/user-info/index.vue'
import UserManage from '../views/admin/UserManage.vue'
import AttendanceGroupManage from '../views/admin/AttendanceGroupManage.vue'
import AttendanceGroupUserManage from '../views/admin/AttendanceGroupUserManage.vue'
import Test from '../views/test/index.vue'
import { userLoginUserStore } from '@/stores/login-user'
import { message } from 'ant-design-vue'
import { StringCaseUtils } from '@/utils/string-case-utils'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            component: HomeView,
        },
        {
            path: '/user/userLogin',
            name: '用户登录',
            component: UserLogin,
        },
        {
            path: '/user/userInfo',
            name: '用户信息',
            component: UserInfo,
        },
        {
            path: '/test',
            name: '测试登录用户才可以查看',
            component: Test
        },
        {
            path: '/admin/usermanage',
            name: '系统用户管理页面',
            component: UserManage
        },
        {
            path: '/admin/attendance-group',
            name: '考勤组管理页面',
            component: AttendanceGroupManage
        },
        {
            path: '/admin/attendance-group-user',
            name: '考勤组人员管理页面',
            component: AttendanceGroupUserManage
        },
    ],
})

// 是否为首次登录用户
let firstFetchLoginUser = true

/**
 * 全局权限校验
 */
router.beforeEach(async (to, _from, next) => {
    const loginUserStore = userLoginUserStore()
    let loginUser = loginUserStore.loginUser

    // 确保页面刷新，首次加载时，能够等后端返回用户信息后再校验权限
    if (firstFetchLoginUser) {
        await loginUserStore.fetchLoginUser()
        loginUser = loginUserStore.loginUser
        firstFetchLoginUser = false
    }
    const toUrl = to.fullPath
    if (toUrl.startsWith('/admin')) {
        if (!loginUser || StringCaseUtils.toLowerCase(loginUser.userRole || '') !== 'admin') {
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

export default router
