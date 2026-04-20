<template>
  <div class="user-info-root">
    <div class="user-info-container">
      <!-- 用户信息卡片 -->
      <UserInfoCard />

      <!-- 考勤日历 -->
      <el-card class="attendance-calendar-card" shadow="never">
        <template #header>
          <div class="calendar-header">
            <span class="header-title">我的考勤记录</span>
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
        </template>

        <el-calendar v-model="calendarValue">
          <template #date-cell="{ data }">
            <div
              class="calendar-cell"
              :class="getAttendanceClass(data.date)"
              @click="handleDateClick(data.date)"
            >
              <div class="date-number">{{ data.day.split('-')[2] }}</div>
              <div v-if="getAttendanceStatus(data.date)" class="attendance-status">
                <el-tag
                  :type="getAttendanceStatus(data.date)?.isNormal ? 'success' : 'danger'"
                  size="small"
                  effect="dark"
                >
                  {{ getAttendanceStatus(data.date)?.isNormal ? '正常' : '异常' }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-calendar>
      </el-card>
    </div>

    <!-- 考勤详情对话框 -->
    <el-dialog
      v-model="attendanceDialogVisible"
      :title="`${selectedDate} 考勤详情`"
      width="700px"
    >
      <el-empty v-if="selectedDateRecords.length === 0" description="当日无考勤记录" />
      <el-table
        v-else
        :data="selectedDateRecords"
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
import { ref, onMounted } from 'vue';
import dayjs from 'dayjs';
import { ElMessage } from 'element-plus';
import UserInfoCard from './components/UserInfoCard.vue';
import { getMyAttendanceRecords } from '@/api/dingtalkAttendanceRecordController';

const calendarValue = ref(new Date());
const attendanceRecords = ref<API.DingtalkAttendanceRecordVO[]>([]);
const loading = ref(false);

// 对话框相关
const attendanceDialogVisible = ref(false);
const selectedDate = ref<string>('');
const selectedDateRecords = ref<API.DingtalkAttendanceRecordVO[]>([]);

// 获取考勤记录
async function fetchAttendanceRecords() {
  loading.value = true;
  try {
    const res = await getMyAttendanceRecords({
      queryDate: dayjs().format('YYYY-MM-DD')
    });

    if (res?.data?.code === 0) {
      attendanceRecords.value = res.data.data || [];
    } else {
      ElMessage.error(res?.data?.message || '获取考勤记录失败');
    }
  } catch (error) {
    console.error('获取考勤记录异常:', error);
    ElMessage.error('网络请求失败，请稍后再试');
  } finally {
    loading.value = false;
  }
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

// 获取指定日期的考勤状态
function getAttendanceStatus(dateStr: string) {
  const date = dayjs(dateStr).format('YYYY-MM-DD');
  const records = attendanceRecords.value.filter(
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
function getAttendanceClass(dateStr: string) {
  const status = getAttendanceStatus(dateStr);
  if (!status) {
    return 'no-record';
  }
  return status.isNormal ? 'normal' : 'abnormal';
}

// 处理日期点击
function handleDateClick(dateStr: string) {
  selectedDate.value = dayjs(dateStr).format('YYYY-MM-DD');
  const records = attendanceRecords.value.filter(
    record => dayjs(record.workDate).format('YYYY-MM-DD') === selectedDate.value
  );
  selectedDateRecords.value = records;
  attendanceDialogVisible.value = true;
}

onMounted(() => {
  fetchAttendanceRecords();
});
</script>

<style scoped>
.user-info-root {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.user-info-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.attendance-calendar-card {
  margin-top: 20px;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
