<template>
  <div class="user-manage-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>用户管理中心</span>
        </div>
      </template>

      <el-table :data="users" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />

        <el-table-column prop="avarUrl" label="头像" width="100" align="center">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avarUrl" />
          </template>
        </el-table-column>

        <el-table-column prop="nickName" label="昵称" align="center" />

        <el-table-column prop="userRole" label="角色(点击修改)" align="center" width="180">
          <template #default="scope">
            <el-select
                v-model="scope.row.userRole"
                placeholder="请选择角色"
                size="small"
                @change="handleRoleChange(scope.row)"
            >
              <el-option label="管理员" value="ADMIN">
                <span style="color: #f56c6c; font-weight: bold;">管理员</span>
              </el-option>
              <el-option label="普通用户" value="USER">
                <span style="color: #409eff;">普通用户</span>
              </el-option>
            </el-select>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { sysUserListpage, update } from '@/api/sysUserController';
import { ElMessage } from 'element-plus';

const users = ref<any[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const loading = ref(false);

// 获取用户列表
async function fetchUsers() {
  loading.value = true;
  try {
    const res = await sysUserListpage({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    });

    if (res?.data?.code === 0) {
      const pageData = res.data.data;
      users.value = pageData?.records || [];
      total.value = Number(pageData?.totalRow || 0);
    } else {
      ElMessage.error(res?.data?.message || '获取用户数据失败');
    }
  } catch (error) {
    console.error('获取用户数据异常:', error);
    ElMessage.error('网络请求失败，请稍后再试');
  } finally {
    loading.value = false;
  }
}

// 💡 新增：处理角色下拉框变更事件
async function handleRoleChange(row: any) {
  try {
    // 调用更新接口
    const res = await update({
      id: row.id,
      userRole: row.userRole
    });

    if (res?.data?.code === 0 && res?.data?.data === true) {
      ElMessage.success(`已成功将用户【${row.nickName}】的角色修改为 ${row.userRole === 'ADMIN' ? '管理员' : '普通用户'}`);
    } else {
      ElMessage.error(res?.data?.message || '角色更新失败');
      // 如果后端报错，为了防止前端显示错误的数据，重新拉取列表覆盖
      fetchUsers();
    }
  } catch (error) {
    console.error('更新角色异常:', error);
    ElMessage.error('网络请求异常，权限修改失败');
    // 网络异常时，也刷新列表还原刚才误操作的下拉框
    fetchUsers();
  }
}

function handleSizeChange(val: number) {
  pageSize.value = val;
  pageNum.value = 1;
  fetchUsers();
}

function handleCurrentChange(val: number) {
  pageNum.value = val;
  fetchUsers();
}

onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
.user-manage-container {
  padding: 20px;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>