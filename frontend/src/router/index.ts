import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/home/index.vue'
import UserLogin from '../views/loginUser/index.vue'
import UserInfo from '../views/userInfo/index.vue'
import UserManage from '../views/admin/UserManage.vue'
import Test from '../views/test/index.vue'

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
    ],
})

export default router
