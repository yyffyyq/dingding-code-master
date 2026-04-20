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
          <div class="header-right">
            <el-button type="success" @click="updateUserList">更新考勤人员</el-button>
            <el-button type="primary" @click="syncAttendance">同步当日考勤情况</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-wrapper">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="员工姓名">
            <el-input
              v-model="searchForm.userName"
              placeholder="请输入员工姓名"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="电话号码">
            <el-input
              v-model="searchForm.phone"
              placeholder="请输入电话号码"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="查询日期">
            <el-date-picker
              v-model="searchForm.queryDate"
              type="date"
              placeholder="选择查询日期"
              value-format="YYYY-MM-DD"
              :disabled-date="disabledQueryDate"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 考勤人员列表 -->
      <el-table
        :data="userList"
        v-loading="loading"
        border
        stripe
        style="width: 100%; margin-bottom: 20px"
      >
        <el-table-column prop="userName" label="用户姓名" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="handleViewUserAttendance(scope.row)"
            >
              查看考勤
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

      <!-- 考勤情况展示区域 -->
      <el-divider content-position="left">
        <span class="section-title">考勤情况</span>
      </el-divider>

      <el-table
        :data="attendanceRecords"
        v-loading="attendanceLoading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="userName" label="用户姓名" align="center" />
        <el-table-column prop="groupId" label="考勤组ID" align="center" />
        <el-table-column label="考勤情况" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isNormal ? 'success' : 'danger'">
              {{ scope.row.isNormal ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="异常详情" align="center" min-width="200">
          <template #default="scope">
            <div v-if="!scope.row.isNormal" class="abnormal-detail">
              <span v-if="scope.row.timeResult" class="detail-item">
                时间结果: {{ scope.row.timeResult }}
              </span>
              <span v-if="scope.row.locationResult" class="detail-item">
                地点结果: {{ scope.row.locationResult }}
              </span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="workDate" label="工作日期" width="120" align="center" />
        <el-table-column label="考勤类型" width="100" align="center">
          <template #default="scope">
            {{ formatCheckType(scope.row.checkType) }}
          </template>
        </el-table-column>
        <el-table-column label="实际打卡时间" width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.userCheckTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="attendancePageNum"
          v-model:page-size="attendancePageSize"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="attendanceTotal"
          @size-change="handleAttendanceSizeChange"
          @current-change="handleAttendanceCurrentChange"
        />
      </div>
    </el-card>

    <!-- 查看单个员工考勤情况对话框 -->
    <el-dialog
      v-model="userAttendanceDialogVisible"
      :title="`${currentUserName} 考勤记录`"
      width="900px"
    >
      <div class="calendar-header">
        <span class="header-title">{{ currentCalendarMonth }} 考勤日历</span>
        <div class="legend">
          <div class="legend-item">
            <span class="legend-dot normal"></span>
            <span>正常</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot abnormal"></span>
            <span>异常</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot no-record"></span>
            <span>无记录</span>
          </div>
        </div>
      </div>
      
      <el-calendar v-model="userCalendarValue" v-loading="userAttendanceLoading">
        <template #date-cell="{ data }">
          <div
            class="calendar-cell"
            :class="getUserAttendanceClass(data.date)"
            @click="handleUserDateClick(data.date)"
          >
            <div class="date-number">{{ data.day.split('-')[2] }}</div>
            <div v-if="getUserAttendanceStatus(data.date)" class="attendance-status">
              <el-tag
                :type="getUserAttendanceStatus(data.date)?.isNormal ? 'success' : 'danger'"
                size="small"
                effect="dark"
              >
                {{ getUserAttendanceStatus(data.date)?.isNormal ? '正常' : '异常' }}
              </el-tag>
            </div>
          </div>
        </template>
      </el-calendar>
    </el-dialog>

    <!-- 员工当日考勤详情对话框 -->
    <el-dialog
      v-model="userDayAttendanceDialogVisible"
      :title="`${currentUserName} ${selectedUserDate} 考勤详情`"
      width="700px"
    >
      <el-empty v-if="selectedUserDateRecords.length === 0" description="当日无考勤记录" />
      <el-table
        v-else
        :data="selectedUserDateRecords"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="checkType" label="考勤类型" align="center">
          <template #default="scope">
            {{ formatCheckType(scope.row.checkType) }}
          </template>
        </el-table-column>
        <el-table-column label="考勤情况" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isNormal ? 'success' : 'danger'">
              {{ scope.row.isNormal ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="timeResult" label="时间结果" align="center" />
        <el-table-column prop="locationResult" label="地点结果" align="center" />
        <el-table-column label="实际打卡时间" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.userCheckTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { getGroupList, getUserkaoqin } from '@/api/userKaoqinController';
import {
  getAttendanceRecordsByGroupId,
  syncAttendanceRecordsByGroupId,
  getAttendanceRecordsByUserId
} from '@/api/dingtalkAttendanceRecordController';
import { ElMessage } from 'element-plus';
import { ArrowLeft } from '@element-plus/icons-vue';
import dayjs from 'dayjs';

const router = useRouter();
const route = useRoute();

const groupId = ref<string>('');
const groupName = ref<string>('');
const userList = ref<API.UserKaoqinVO[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const loading = ref(false);

// 搜索表单
const searchForm = ref({
  userName: '',
  phone: '',
  queryDate: ''
});

// 考勤记录相关
const attendanceRecords = ref<API.DingtalkAttendanceRecordVO[]>([]);
const attendanceLoading = ref(false);
const attendancePageNum = ref(1);
const attendancePageSize = ref(10);
const attendanceTotal = ref(0);

// 用户考勤详情对话框
const userAttendanceDialogVisible = ref(false);
const userAttendanceLoading = ref(false);
const userAttendanceRecords = ref<API.DingtalkAttendanceRecordVO[]>([]);
const currentUserId = ref<string>('');
const currentUserName = ref<string>('');
const userCalendarValue = ref(new Date());

// 用户当日考勤详情对话框
const userDayAttendanceDialogVisible = ref(false);
const selectedUserDate = ref<string>('');
const selectedUserDateRecords = ref<API.DingtalkAttendanceRecordVO[]>([]);

// 计算当前日历显示的月份
const currentCalendarMonth = computed(() => {
  return dayjs(userCalendarValue.value).format('YYYY年MM月');
});

// 计算默认日期范围（今天）
const today = dayjs().format('YYYY-MM-DD');

// 禁用查询日期（限制6天范围）
function disabledQueryDate(time: Date) {
  const sixDaysAgo = dayjs().subtract(6, 'day').startOf('day');
  const tomorrow = dayjs().add(1, 'day').startOf('day');
  return time.getTime() < sixDaysAgo.valueOf() || time.getTime() >= tomorrow.valueOf();
}

// 格式化考勤类型
function formatCheckType(checkType?: string) {
  if (!checkType) return '-';
  const typeMap: Record<string, string> = {
    'OffDuty': '下班打卡',
    'OnDuty': '上班打卡'
  };
  return typeMap[checkType] || checkType;
}

// 格式化日期时间（去掉T）
function formatDateTime(dateTime?: string) {
  if (!dateTime) return '-';
  return dateTime.replace('T', ' ');
}

// 更新考勤组考勤人员信息
async function updateUserList() {
  if (!groupId.value) {
    ElMessage.error('考勤组ID不能为空');
    return;
  }
  loading.value = true;
  try {
    const res = await getUserkaoqin({
      group_id: groupId.value
    });
    if (res?.data?.code === 0) {
      ElMessage.success('考勤人员更新成功');
      fetchUserList();
    }
  } catch (error) {
    console.error('获取考勤人员数据异常:', error);
    ElMessage.error('网络请求失败，请稍后再试');
  } finally {
    loading.value = false;
  }
}

// 同步当日考勤情况
async function syncAttendance() {
  if (!groupId.value) {
    ElMessage.error('考勤组ID不能为空');
    return;
  }
  attendanceLoading.value = true;
  try {
    const res = await syncAttendanceRecordsByGroupId({
      groupId: groupId.value,
      checkDateFrom: dayjs().subtract(1, 'day').format('YYYY-MM-DD HH:mm:ss'),
      checkDateTo: dayjs().format('YYYY-MM-DD HH:mm:ss')
    });
    if (res?.data?.code === 0) {
      ElMessage.success(`同步成功，共更新 ${res.data.data} 条记录`);
      fetchAttendanceRecords();
    } else {
      ElMessage.error(res?.data?.message || '同步考勤记录失败');
    }
  } catch (error) {
    console.error('同步考勤记录异常:', error);
    ElMessage.error('网络请求失败，请稍后再试');
  } finally {
    attendanceLoading.value = false;
  }
}

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
      groupId: groupId.value,
      userName: searchForm.value.userName || undefined
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

// 获取考勤记录列表
async function fetchAttendanceRecords() {
  if (!groupId.value) {
    return;
  }

  attendanceLoading.value = true;
  try {
    const res = await getAttendanceRecordsByGroupId({
      pageNum: attendancePageNum.value,
      pageSize: attendancePageSize.value,
      groupId: groupId.value,
      userName: searchForm.value.userName || undefined,
      queryDate: searchForm.value.queryDate || today
    });

    if (res?.data?.code === 0) {
      const pageData = res.data.data;
      attendanceRecords.value = pageData?.records || [];
      attendanceTotal.value = Number(pageData?.totalRow || 0);
    } else {
      ElMessage.error(res?.data?.message || '获取考勤记录失败');
    }
  } catch (error) {
    console.error('获取考勤记录异常:', error);
    ElMessage.error('网络请求失败，请稍后再试');
  } finally {
    attendanceLoading.value = false;
  }
}

// 搜索
function handleSearch() {
  pageNum.value = 1;
  attendancePageNum.value = 1;
  fetchUserList();
  fetchAttendanceRecords();
}

// 重置搜索
function handleReset() {
  searchForm.value = {
    userName: '',
    phone: '',
    queryDate: ''
  };
  pageNum.value = 1;
  attendancePageNum.value = 1;
  fetchUserList();
  fetchAttendanceRecords();
}

// 查看单个员工考勤情况
function handleViewUserAttendance(row: API.UserKaoqinVO) {
  currentUserId.value = row.userId || '';
  currentUserName.value = row.userName || '';
  userCalendarValue.value = new Date();
  userAttendanceDialogVisible.value = true;
  fetchUserAttendanceDetail();
}

// 获取单个员工考勤详情
async function fetchUserAttendanceDetail() {
  if (!currentUserId.value) {
    return;
  }
  userAttendanceLoading.value = true;
  try {
    const res = await getAttendanceRecordsByUserId({
      userId: currentUserId.value
    });

    if (res?.data?.code === 0) {
      userAttendanceRecords.value = res.data.data || [];
    } else {
      ElMessage.error(res?.data?.message || '获取员工考勤记录失败');
    }
  } catch (error) {
    console.error('获取员工考勤记录异常:', error);
    ElMessage.error('网络请求失败，请稍后再试');
  } finally {
    userAttendanceLoading.value = false;
  }
}

// 获取指定日期的考勤状态
function getUserAttendanceStatus(dateStr: string) {
  const date = dayjs(dateStr).format('YYYY-MM-DD');
  const records = userAttendanceRecords.value.filter(
    record => dayjs(record.workDate).format('YYYY-MM-DD') === date
  );

  if (records.length === 0) {
    return null;
  }

  // 如果有多条记录，只要有一条异常就显示异常
  const hasAbnormal = records.some(record => !record.isNormal);
  return {
    isNormal: !hasAbnormal,
    records
  };
}

// 获取考勤样式类
function getUserAttendanceClass(dateStr: string) {
  const status = getUserAttendanceStatus(dateStr);
  if (!status) {
    return 'no-record';
  }
  return status.isNormal ? 'normal' : 'abnormal';
}

// 处理日期点击
function handleUserDateClick(dateStr: string) {
  selectedUserDate.value = dayjs(dateStr).format('YYYY-MM-DD');
  const records = userAttendanceRecords.value.filter(
    record => dayjs(record.workDate).format('YYYY-MM-DD') === selectedUserDate.value
  );
  selectedUserDateRecords.value = records;
  userDayAttendanceDialogVisible.value = true;
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

function handleAttendanceSizeChange(val: number) {
  attendancePageSize.value = val;
  attendancePageNum.value = 1;
  fetchAttendanceRecords();
}

function handleAttendanceCurrentChange(val: number) {
  attendancePageNum.value = val;
  fetchAttendanceRecords();
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
  fetchAttendanceRecords();
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

.header-right {
  display: flex;
  gap: 10px;
}

.group-title {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.search-wrapper {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.section-title {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.abnormal-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item {
  font-size: 12px;
  color: #f56c6c;
}

.dialog-search {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-title {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.legend {
  display: flex;
  gap: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-dot.normal {
  background-color: #67c23a;
}

.legend-dot.abnormal {
  background-color: #f56c6c;
}

.legend-dot.no-record {
  background-color: #dcdfe6;
}

.calendar-cell {
  width: 100%;
  height: 100%;
  padding: 4px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
}

.calendar-cell:hover {
  opacity: 0.8;
}

.calendar-cell.normal {
  background-color: rgba(103, 194, 58, 0.1);
  border: 1px solid rgba(103, 194, 58, 0.3);
}

.calendar-cell.abnormal {
  background-color: rgba(245, 108, 108, 0.1);
  border: 1px solid rgba(245, 108, 108, 0.3);
}

.calendar-cell.no-record {
  background-color: transparent;
}

.date-number {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.attendance-status {
  display: flex;
  justify-content: center;
}

:deep(.el-calendar-day) {
  padding: 4px;
  height: 80px;
}

:deep(.el-calendar-day:hover) {
  background-color: transparent;
}

:deep(.current) .calendar-cell.normal {
  background-color: rgba(103, 194, 58, 0.2);
}

:deep(.current) .calendar-cell.abnormal {
  background-color: rgba(245, 108, 108, 0.2);
}
</style>
