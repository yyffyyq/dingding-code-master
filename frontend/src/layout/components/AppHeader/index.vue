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
import { ElMessage } from 'element-plus'
import { DING_CLIENT_ID } from '@/config/constants'
import {userLoginUserStore} from "@/stores/login-user";
import { StringCaseUtils } from '@/utils/string-case-utils';

// 菜单基础配置项
const originItems = [
  { key: '/', label: '首页' },
  {
    key: '/admin',
    label: '管理员',
    children: [
      { key: '/admin/usermanage', label: '用户管理' },
      { key: '/admin/attendance-group', label: '考勤组管理' }
    ]
  },
  { key: '/test', label: '测试页面(需登录)' }
]

// 过滤导航栏目录内容做权限控制
const filterMenus = (menus: any[]) => {
  return menus
      .map(menu => {
        const newMenu = { ...menu }
        if (newMenu.children) {
          newMenu.children = filterMenus(newMenu.children)
        }
        return newMenu
      })
      .filter((menu) => {
        const menuKey = menu?.key as string

        if (menu.children && menu.children.length === 0) {
          return false
        }

        if (menuKey?.startsWith('/admin')) {
          const loginUser = loginUserStore.loginUser
          if (!loginUser || StringCaseUtils.toLowerCase(loginUser.userRole || " ") !== 'admin') {
            return false
          }
        }

        if (menuKey?.startsWith('/test')) {
          if (!isLoggedIn.value) {
            return false
          }
        }

        return true
      })
}

const menuItems = computed(() => filterMenus(originItems))

const loginUserStore = userLoginUserStore()

const router = useRouter()
const route = useRoute()

const activeMenu = route.path

const isLoggedIn = ref(false)
const userInfo = ref({
  nickName: '',
  avarUrl: ''
})

const checkLoginStatus = async () => {
  try {
    const res = await health()
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
    isLoggedIn.value = false
  }
}

onMounted(() => {
  checkLoginStatus()
})

const handleSelect = (key: string) => {
  router.push(key)
}

const handleLogout = async () => {
  try {
    const res = await logout();

    if (res.data.code === 0){
      ElMessage.success("退出成功")
    }

    isLoggedIn.value = false;
    userInfo.value = { nickName: '', avarUrl: '' };
    loginUserStore.loginUser = {nickName: "未登录"};
    localStorage.removeItem('user_info');

    ElMessage.success('系统退出成功，正在清理钉钉授权状态...');

    const clientId = DING_CLIENT_ID;
    const returnUrl = encodeURIComponent('http://localhost:5173/user/userLogin');
    const dingLogoutUrl = `https://login.dingtalk.com/oauth2/logout?client_id=${clientId}&continue=${returnUrl}`;
    window.location.href = dingLogoutUrl;

  } catch (error) {
    console.error('退出异常:', error);
    ElMessage.error('退出失败，请稍后再试');
  }
}

</script>

<style src="./head-style.css"></style>

<style scoped>
.nav-wrapper {
  width: 100%;
}

.main-nav {
  display: flex;
  align-items: center;
  position: relative;
  padding: 0 20px;
}

.flex-spacer {
  flex-grow: 1;
}

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

.custom-login-btn {
  font-size: 18px;
  font-weight: 500;
}

.custom-profile-menu :deep(.el-sub-menu__title) {
  display: flex;
  align-items: center;
}

.user-profile-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-nickname {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  transition: color 0.3s;
}

.custom-profile-menu :deep(.el-sub-menu__title:hover) .user-nickname,
.custom-profile-menu.is-active .user-nickname {
  color: var(--el-color-primary, #409eff);
}
</style>

<style>
.el-menu--popup .el-menu-item {
  justify-content: center !important;
  font-weight: 500;
}
</style>
