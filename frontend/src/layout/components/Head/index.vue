<template>
  <div class="nav-wrapper">
    <el-menu
        :default-active="activeMenu"
        class="main-nav el-menu-demo"
        mode="horizontal"
        @select="handleSelect"
        :ellipsis="false"
    >
      <template v-for="item in menuItems" :key="item.key">
        <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.key">
          <template #title>{{ item.label }}</template>
          <el-menu-item
              v-for="child in item.children"
              :key="child.key"
              :index="child.key"
          >
            {{ child.label }}
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item v-else :index="item.key">
          {{ item.label }}
        </el-menu-item>
      </template>

      <div class="center-logo">考勤打卡数据转换</div>

      <div class="flex-spacer"></div>

      <el-menu-item v-if="!isLoggedIn" index="/user/userLogin" class="custom-login-btn">
        登录
      </el-menu-item>

      <el-sub-menu v-else index="user-profile" class="custom-profile-menu">
        <template #title>
          <div class="user-profile-box">
            <el-avatar :size="32" :src="userInfo.avarUrl" />
            <span class="user-nickname">{{ userInfo.nickName }}</span>
          </div>
        </template>
        <el-menu-item index="/user/userInfo">个人中心</el-menu-item>
        <el-menu-item index="logout" @click="handleLogout">退出登录</el-menu-item>
      </el-sub-menu>

    </el-menu>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted, computed} from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { health, logout } from '@/api/dingUserController'
import { ElMessage } from 'element-plus' // 推荐引入消息提示
import { DING_CLIENT_ID } from '@/config/constants'
import {userLoginUserStore} from "@/stors/loginUser.ts";
import { StringCaseUtils } from '@/util/stringCaseUtils.ts';

// 菜单基础配置项
const originItems = [
  { key: '/', label: '首页' },
  {
    key: '/admin',
    label: '管理员',
    children: [
      { key: '/admin/usermanage', label: '用户管理' },
      { key: '/admin/other', label: '其他管理页面' } // 你可以在这里继续添加管理员页面
    ]
  },
  { key: '/test', label: '测试页面(需登录)' }
]

// 过滤导航栏目录内容做权限控制
const filterMenus = (menus: any[]) => {
  return menus
      .map(menu => {
        // 浅拷贝当前菜单项，防止修改原数组
        const newMenu = { ...menu }
        // 如果有子菜单，递归过滤子菜单
        if (newMenu.children) {
          newMenu.children = filterMenus(newMenu.children)
        }
        return newMenu
      })
      .filter((menu) => {
        const menuKey = menu?.key as string

        // 1. 如果该菜单有 children，但过滤后 children 空了，则不显示父菜单
        if (menu.children && menu.children.length === 0) {
          return false
        }

        // 2. 管理员权限页面拦截
        if (menuKey?.startsWith('/admin')) {
          const loginUser = loginUserStore.loginUser
          if (!loginUser || StringCaseUtils.toLowerCase(loginUser.userRole || " ") !== 'admin') {
            return false
          }
        }

        // 3. 需要登录才可见的页面拦截
        if (menuKey?.startsWith('/test')) {
          if (!isLoggedIn.value) {
            return false
          }
        }

        return true
      })
}

// 再次获取导航栏目录内容
const menuItems = computed(() => filterMenus(originItems))

// 获取用户登录状态
const loginUserStore = userLoginUserStore()

const router = useRouter()
const route = useRoute()

// 自动匹配当前路由，导航栏高亮
const activeMenu = route.path

// 用户登录状态控制
const isLoggedIn = ref(false)
const userInfo = ref({
  nickName: '',
  avarUrl: ''
})

// 查询当前会话是否已登录
const checkLoginStatus = async () => {
  try {
    const res = await health()
    // 根据你的接口文档，code === 0 代表请求成功并且拿到了用户信息
    if (res.data.code === 0 && res.data) {
      isLoggedIn.value = true
      userInfo.value = {
        nickName: res.data.data?.nickName || '',
        avarUrl: res.data.data?.avarUrl || ''
      }
      loginUserStore.loginUser = res.data.data || {nickName: "未登录"}
    } else {
      isLoggedIn.value = false
      loginUserStore.loginUser = {nickName: "未登录"};
    }
  } catch (error) {
    console.error("获取登录状态异常", error)
    isLoggedIn.value = false // 网络异常时也当做未登录处理
  }
}

// 页面一加载就去查状态
onMounted(() => {
  checkLoginStatus()
})

// 监听路由跳转
const handleSelect = (key: string) => {
  router.push(key)
}

/**
 * 用户退出功能
 */
const handleLogout = async () => {
  try {
    // 1. 调用退出接口
    const res = await logout();

    if (res.data.code === 0){
      ElMessage.success("退出成功")
    }

    // 2. 清理前端 Vue 内部的响应式状态
    isLoggedIn.value = false;
    userInfo.value = { nickName: '', avarUrl: '' };
    loginUserStore.loginUser = {nickName: "未登录"};
    localStorage.removeItem('user_info'); // 如果你之前存了 localstorage，也一并清掉

    ElMessage.success('系统退出成功，正在清理钉钉授权状态...');

    // 3. 【核心】跳转到钉钉的退出页面清空 Cookie
    // 注意：clientId 必须和你登录时用的一模一样
    const clientId = DING_CLIENT_ID;

    // 退出后钉钉再把你跳回哪里？通常是你的登录页或首页
    const returnUrl = encodeURIComponent('http://localhost:5173/user/userLogin');

    // 这是钉钉官方提供的登出地址模板
    const dingLogoutUrl = `https://login.dingtalk.com/oauth2/logout?client_id=${clientId}&continue=${returnUrl}`;
    // 直接让浏览器跳走
    window.location.href = dingLogoutUrl;

  } catch (error) {
    console.error('退出异常:', error);
    ElMessage.error('退出失败，请稍后再试');
  }
}

</script>

<style src="../Head/css/head-style.css"></style>

<style scoped>
/* ================= 还原 Element 原生经典样式 ================= */

.nav-wrapper {
  width: 100%;
}

/* 导航栏整体容器 */
.main-nav {
  display: flex;
  align-items: center;
  position: relative;
  padding: 0 20px;
}

/* 占据剩余空间，将右侧元素推向最右边 */
.flex-spacer {
  flex-grow: 1;
}

/* 绝对居中的标题 / Logo */
.center-logo {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  pointer-events: none;
}

/* 登录按钮 */
.custom-login-btn {
  font-size: 18px;
  font-weight: 500;
}

/* ================= 用户下拉菜单区域专属优化 ================= */

/* 1. 修复带有自定义 DOM（头像）时的垂直居中及箭头对齐问题 */
.custom-profile-menu :deep(.el-sub-menu__title) {
  display: flex;
  align-items: center;
}

/* 2. 头像与昵称容器的间距 */
.user-profile-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 3. 昵称文字默认颜色保持与主导航一致 */
.user-nickname {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  transition: color 0.3s;
}

/* 4. 当鼠标悬停在整个下拉组件上时，让内部的昵称也同步变为主题蓝 */
.custom-profile-menu :deep(.el-sub-menu__title:hover) .user-nickname,
.custom-profile-menu.is-active .user-nickname {
  color: var(--el-color-primary, #409eff);
}
</style>

<style>
/* 使弹出的下拉菜单内部的选项文本居中，与顶部导航栏风格保持高度统一 */
.el-menu--popup .el-menu-item {
  justify-content: center !important;
  font-weight: 500;
}
</style>