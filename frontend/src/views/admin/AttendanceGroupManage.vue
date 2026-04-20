<template>
  <div class="attendance-group-manage-container">
    <el-card shadow="never">
      <template #header>
        <div class="header-wrapper">
          <div class="card-header">
            <span>考勤组管理</span>
          </div>
          <div class="button-example">
            <div class="button-row">
              <el-button type="success" @click="updateGroup">更新考勤组数据</el-button>
            </div>
          </div>
        </div>
      </template>

      <!-- 考勤组列表展示 -->
      <el-table :data="groupList" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="groupId" label="考勤组ID" width="180" align="center" />

        <el-table-column prop="groupName" label="考勤组名称" align="center" />

        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button
                type="primary"
                size="small"
                @click="handleEnterGroup(scope.row)"
            >
              进入考勤组
            </el-button>
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
import { useRouter } from 'vue-router';
import { getGroupList1,getSimpleGroup } from '@/api/groupKaoqinController';
import { ElMessage } from 'element-plus';

const router = useRouter();

const groupList = ref<API.GroupKaoqinVO[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const loading = ref(false);

// 更新考勤组数据
async function updateGroup(){
  try {
    const res = await getSimpleGroup();
    if (res?.data?.code === 0) {
      ElMessage.success('更新考勤组数据成功');
      fetchGroupList();
    } else {
      ElMessage.error(res?.data?.message || '更新考勤组数据失败');
    }
  } catch (error) {
    console.error('更新考勤组数据异常:', error);
    ElMessage.error('网络请求失败，请稍后再试');
  }
}

// 获取考勤组列表
async function fetchGroupList() {
  loading.value = true;
  try {
    const res = await getGroupList1({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    });

    if (res?.data?.code === 0) {
      const pageData = res.data.data;
      // 为每个考勤组添加人数字段（后端暂无该字段，使用模拟数据）
      groupList.value = (pageData?.records || []).map((group: API.GroupKaoqinVO) => ({
        ...group,
        memberCount: Math.floor(Math.random() * 50) + 1 // 模拟人数，实际应从后端获取
      }));
      total.value = Number(pageData?.totalRow || 0);
    } else {
      ElMessage.error(res?.data?.message || '获取考勤组数据失败');
    }
  } catch (error) {
    console.error('获取考勤组数据异常:', error);
    ElMessage.error('网络请求失败，请稍后再试');
  } finally {
    loading.value = false;
  }
}

// 进入考勤组
function handleEnterGroup(row: API.GroupKaoqinVO) {
  router.push({
    path: '/admin/attendance-group-user',
    query: {
      groupId: row.groupId,
      groupName: row.groupName
    }
  });
}

function handleSizeChange(val: number) {
  pageSize.value = val;
  pageNum.value = 1;
  fetchGroupList();
}

function handleCurrentChange(val: number) {
  pageNum.value = val;
  fetchGroupList();
}

onMounted(() => {
  fetchGroupList();
});
</script>

<style scoped>
.attendance-group-manage-container {
  padding: 20px;
}

.header-wrapper {
  display: flex;
  justify-content: space-between; /* 左右分开 */
  align-items: center;            /* 垂直居中 */
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


.button-example {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.button-row {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: center;
}

.button-row > * {
  margin: 0;
}

</style>
