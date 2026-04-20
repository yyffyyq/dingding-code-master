<template>
  <el-card class="user-info-card" shadow="never">
    <template #header>
      <div class="card-header">
        <span class="header-title">用户信息</span>
      </div>
    </template>

    <div class="user-info-content">
      <div class="avatar-section">
        <el-avatar
          :size="80"
          :src="loginUserStore.loginUser.avarUrl || defaultAvatar"
          fit="cover"
        />
      </div>
      <div class="info-section">
        <div class="info-item">
          <span class="info-label">昵称：</span>
          <span class="info-value">{{ loginUserStore.loginUser.nickName || '未设置' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">角色：</span>
          <el-tag :type="getRoleTagType(loginUserStore.loginUser.userRole)">
            {{ getRoleText(loginUserStore.loginUser.userRole) }}
          </el-tag>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { userLoginUserStore } from '@/stores/login-user';

const loginUserStore = userLoginUserStore();

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 获取角色文本
function getRoleText(role?: string) {
  if (!role) return '普通用户';
  const roleMap: Record<string, string> = {
    'admin': '管理员',
    'user': '普通用户',
    'super_admin': '超级管理员'
  };
  return roleMap[role] || role;
}

// 获取角色标签类型
function getRoleTagType(role?: string) {
  if (!role) return 'info';
  const typeMap: Record<string, any> = {
    'admin': 'danger',
    'user': 'success',
    'super_admin': 'warning'
  };
  return typeMap[role] || 'info';
}

onMounted(() => {
  loginUserStore.fetchLoginUser();
});
</script>

<style scoped>
.user-info-card {
  width: 100%;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.user-info-content {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px;
}

.avatar-section {
  flex-shrink: 0;
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-label {
  font-size: 14px;
  color: #909399;
  min-width: 50px;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
</style>
