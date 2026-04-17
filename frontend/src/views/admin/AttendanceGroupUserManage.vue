<template>
  <div class="attendance-group-user-manage-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="text" @click="handleBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <span class="group-title">考勤组：{{ groupName }}</span>
          </div>
        </div>
      </template>

      <el-table :data="userList" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="userName" label="用户姓名" align="center" />

        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
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
import { useRouter, useRoute } from 'vue-router';
import { getGroupList } from '@/api/userKaoqinController';
import { ElMessage } from 'element-plus';
import { ArrowLeft } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();

const groupId = ref<string>('');
const groupName = ref<string>('');
const userList = ref<API.UserKaoqinVO[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const loading = ref(false);

// 获取考勤组人员列表
async function fetchUserList() {
  if (!groupId.value) {
    ElMessage.error('考勤组ID不能为空');
    return;
  }

  loading.value = true;
  try {
    const res = await getGroupList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      groupId: groupId.value
    });

    if (res?.data?.code === 0) {
      const pageData = res.data.data;
      userList.value = pageData?.records || [];
      total.value = Number(pageData?.totalRow || 0);
    } else {
      ElMessage.error(res?.data?.message || '获取考勤人员数据失败');
    }
  } catch (error) {
    console.error('获取考勤人员数据异常:', error);
    ElMessage.error('网络请求失败，请稍后再试');
  } finally {
    loading.value = false;
  }
}

// 返回上一页
function handleBack() {
  router.push('/admin/attendance-group');
}

function handleSizeChange(val: number) {
  pageSize.value = val;
  pageNum.value = 1;
  fetchUserList();
}

function handleCurrentChange(val: number) {
  pageNum.value = val;
  fetchUserList();
}

onMounted(() => {
  const queryGroupId = route.query.groupId as string;
  const queryGroupName = route.query.groupName as string;

  if (!queryGroupId) {
    ElMessage.error('缺少考勤组ID参数');
    router.push('/admin/attendance-group');
    return;
  }

  groupId.value = queryGroupId;
  groupName.value = queryGroupName || '未命名考勤组';
  fetchUserList();
});
</script>

<style scoped>
.attendance-group-user-manage-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.group-title {
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
