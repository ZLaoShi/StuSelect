<template>
  <div class="dashboard-container">
    <n-card title="系统概览" class="dashboard-card">
      <!-- 统计卡片 -->
      <n-grid :cols="4" :x-gap="16" :y-gap="16">
        <n-gi>
          <n-card bordered>
            <n-statistic label="学生总数" :value="dashboardData.totalStudentCount">
              <template #prefix>
                <n-icon><PeopleOutline /></n-icon>
              </template>
            </n-statistic>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card bordered>
            <n-statistic label="课程总数" :value="dashboardData.totalCourseCount">
              <template #prefix>
                <n-icon><BookmarkOutline /></n-icon>
              </template>
            </n-statistic>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card bordered>
            <n-statistic label="活跃课程" :value="dashboardData.activeCourseCount">
              <template #prefix>
                <n-icon><CheckmarkCircleOutline /></n-icon>
              </template>
            </n-statistic>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card bordered>
            <n-statistic label="选课总数" :value="dashboardData.totalSelectionCount">
              <template #prefix>
                <n-icon><ListOutline /></n-icon>
              </template>
            </n-statistic>
          </n-card>
        </n-gi>
      </n-grid>
    </n-card>

    <!-- 图表和表格 -->
    <n-grid :cols="2" :x-gap="16" :y-gap="16" class="chart-grid">
      <n-gi>
        <n-card title="课程类型分布" bordered class="dashboard-card">
          <div class="chart-container">
            <div ref="pieChartRef" class="pie-chart"></div>
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="热门课程排名" bordered class="dashboard-card">
          <n-data-table
            :columns="topCourseColumns"
            :data="dashboardData.topSelectedCourses"
            :bordered="false"
            :single-line="false"
            size="small"
          />
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 最近选课记录 -->
    <n-card title="最近选课记录" bordered class="dashboard-card">
      <n-data-table
        :columns="recentSelectionColumns"
        :data="dashboardData.recentSelections"
        :bordered="false"
        :single-line="false"
        size="small"
      />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { dashboard } from '@/api'
import type { AdminDashboardDTO } from '@/types/api'
import { 
  NCard, 
  NGrid, 
  NGi, 
  NStatistic, 
  NDataTable, 
  NIcon, 
  NSpin 
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { 
  PeopleOutline, 
  BookmarkOutline, 
  CheckmarkCircleOutline,
  ListOutline
} from '@vicons/ionicons5'
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册必要的 ECharts 组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  PieChart,
  CanvasRenderer
])

// 仪表盘数据
const dashboardData = ref<AdminDashboardDTO>({
  totalStudentCount: 0,
  totalCourseCount: 0,
  activeCourseCount: 0,
  totalSelectionCount: 0,
  courseTypeDistribution: {},
  topSelectedCourses: [],
  recentSelections: []
})

// 加载状态
const loading = ref(true)

// 热门课程表格列
const topCourseColumns: DataTableColumns = [
  { title: '课程代码', key: 'courseCode', width: 120 },
  { title: '课程名称', key: 'courseName', width: 200 },
  { title: '教师', key: 'teacherName', width: 120 },
  { 
    title: '选课情况', 
    key: 'selectedCount',
    render(row) {
      return `${row.selectedCount}/${row.capacity}`
    }
  }
]

// 最近选课记录表格列
const recentSelectionColumns: DataTableColumns = [
  { title: '学号', key: 'studentNo', width: 120 },
  { title: '学生姓名', key: 'studentName', width: 120 },
  { title: '课程代码', key: 'courseCode', width: 120 },
  { title: '课程名称', key: 'courseName', width: 200 },
  { 
    title: '选课时间', 
    key: 'selectTime',
    width: 180,
    render(row) {
      return formatDateTime(row.selectTime)
    }
  }
]

// 饼图DOM引用
const pieChartRef = ref<HTMLElement | null>(null)
let pieChart: echarts.ECharts | null = null

// 获取仪表盘数据
const fetchDashboardData = async () => {
  loading.value = true
  try {
    dashboardData.value = await dashboard.getAdminDashboardData()
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  } finally {
    loading.value = false
    nextTick(() => {
      initPieChart()
    })
  }
}

// 初始化饼图
const initPieChart = () => {
  if (!pieChartRef.value) return
  
  if (pieChart) {
    pieChart.dispose()
  }
  
  pieChart = echarts.init(pieChartRef.value)
  
  // 准备饼图数据
  const pieData = Object.entries(dashboardData.value.courseTypeDistribution).map(([name, value]) => ({
    name,
    value
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      data: pieData.map(item => item.name)
    },
    series: [
      {
        name: '课程类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: pieData
      }
    ]
  }
  
  pieChart.setOption(option)
}

// 格式化日期时间
const formatDateTime = (dateTimeStr: string) => {
  if (!dateTimeStr) return '-'
  
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`
}

// 补零
const padZero = (num: number) => {
  return num < 10 ? `0${num}` : num
}

// 窗口大小变化时重新调整图表大小
const handleResize = () => {
  if (pieChart) {
    pieChart.resize()
  }
}

onMounted(() => {
  fetchDashboardData()
  window.addEventListener('resize', handleResize)
})

// 组件卸载时移除事件监听和图表实例
const onUnmounted = () => {
  window.removeEventListener('resize', handleResize)
  if (pieChart) {
    pieChart.dispose()
    pieChart = null
  }
}
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dashboard-card {
  margin-bottom: 0;
}

.chart-grid {
  margin-top: 16px;
}

.chart-container {
  width: 100%;
  height: 300px;
  
  .pie-chart {
    width: 100%;
    height: 100%;
  }
}

.label-success {
  color: #18a058;
}

.label-warning {
  color: #f0a020;
}

.label-error {
  color: #d03050;
}
</style>