<template>
  <div class="dashboard-container">
    <n-card title="我的学习概览" class="welcome-card">
      <template #header-extra>
        <n-button text type="primary" @click="refreshData">
          <template #icon>
            <n-icon><RefreshOutline /></n-icon>
          </template>
          刷新数据
        </n-button>
      </template>

      <!-- 数据统计卡片 -->
      <n-grid :cols="3" :x-gap="16" :y-gap="16" class="stats-grid">
        <n-gi>
          <n-card bordered class="stat-card">
            <n-statistic label="已选课程" :value="dashboardData.selectedCourseCount">
              <template #prefix>
                <n-icon><BookOutline /></n-icon>
              </template>
            </n-statistic>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card bordered class="stat-card">
            <n-statistic label="可选课程" :value="dashboardData.availableCourseCount">
              <template #prefix>
                <n-icon><AddCircleOutline /></n-icon>
              </template>
            </n-statistic>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card bordered class="stat-card">
            <n-statistic label="已获学分" :value="dashboardData.totalCredit">
              <template #prefix>
                <n-icon><StarOutline /></n-icon>
              </template>
            </n-statistic>
          </n-card>
        </n-gi>
      </n-grid>
      
      <!-- 课程分布和最近选课 -->
      <n-grid :cols="2" :x-gap="16" :y-gap="16" class="content-grid">
        <n-gi>
          <n-card title="课程类型分布" bordered class="chart-card">
            <div v-if="!loading && Object.keys(dashboardData.courseTypeDistribution).length > 0">
              <div ref="pieChartRef" class="pie-chart"></div>
            </div>
            <div v-else-if="loading" class="chart-loading">
              <n-spin size="medium" />
              <p>加载中...</p>
            </div>
            <n-empty v-else description="暂无选课数据" />
          </n-card>
        </n-gi>
        <n-gi>
          <n-card title="最近选择的课程" bordered class="courses-card">
            <n-data-table
              v-if="!loading && dashboardData.recentSelectedCourses.length > 0"
              :columns="courseColumns"
              :data="dashboardData.recentSelectedCourses"
              :bordered="false"
              :single-line="false"
              size="small"
            />
            <div v-else-if="loading" class="table-loading">
              <n-spin size="medium" />
              <p>加载中...</p>
            </div>
            <n-empty v-else description="暂无选课记录" />
          </n-card>
        </n-gi>
      </n-grid>
      
      <!-- 快捷操作 -->
      <n-card title="快捷操作" bordered class="actions-card">
        <n-space>
          <n-button type="primary" @click="goToCourseSelection">
            <template #icon>
              <n-icon><AddOutline /></n-icon>
            </template>
            选课中心
          </n-button>
          <n-button @click="goToMyCourses">
            <template #icon>
              <n-icon><ListOutline /></n-icon>
            </template>
            我的课程
          </n-button>
        </n-space>
      </n-card>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { dashboard } from '@/api'
import type { StudentDashboardDTO } from '@/types/api'
import { 
  NCard, 
  NGrid, 
  NGi, 
  NStatistic, 
  NDataTable, 
  NIcon, 
  NEmpty, 
  NSpace, 
  NButton,
  NSpin
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { 
  BookOutline, 
  AddCircleOutline, 
  StarOutline,
  AddOutline,
  ListOutline,
  RefreshOutline
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

const router = useRouter()
const userStore = useUserStore()

// 仪表盘数据
const dashboardData = ref<StudentDashboardDTO>({
  selectedCourseCount: 0,
  availableCourseCount: 0,
  courseTypeDistribution: {},
  recentSelectedCourses: [],
  totalCredit: 0
})

// 加载状态
const loading = ref(true)

// 饼图DOM引用
const pieChartRef = ref<HTMLElement | null>(null)
let pieChart: echarts.ECharts | null = null

// 课程表格列定义
const courseColumns: DataTableColumns = [
  { title: '课程代码', key: 'courseCode', width: 120 },
  { title: '课程名称', key: 'courseName', ellipsis: true },
  { title: '教师', key: 'teacherName', width: 100 },
  { title: '学分', key: 'credit', width: 60 },
  { 
    title: '选课时间', 
    key: 'selectTime', 
    width: 150,
    render(row) {
      return formatDateTime(row.selectTime)
    } 
  }
]

// 获取仪表盘数据
const fetchDashboardData = async () => {
  if (!userStore.studentId) {
    loading.value = false
    return
  }
  
  loading.value = true
  try {
    dashboardData.value = await dashboard.getStudentDashboardData(userStore.studentId)
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  } finally {
    loading.value = false
    nextTick(() => {
      initPieChart()
    })
  }
}

// 手动刷新数据
const refreshData = () => {
  fetchDashboardData()
}

// 初始化饼图
const initPieChart = () => {
  if (!pieChartRef.value || Object.keys(dashboardData.value.courseTypeDistribution).length === 0) return
  
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

// 页面导航
const goToCourseSelection = () => {
  router.push('/student/courses')
}

const goToMyCourses = () => {
  router.push('/student/my-courses')
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

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (pieChart) {
    pieChart.dispose()
    pieChart = null
  }
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 16px;
  
  .welcome-card {
    margin-bottom: 16px;
  }
  
  .stats-grid {
    margin-top: 16px;
    margin-bottom: 16px;
    
    .stat-card {
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
    }
  }
  
  .content-grid {
    margin-bottom: 16px;
    
    .chart-card, .courses-card {
      height: 320px;
      overflow: hidden;
    }
  }
  
  .pie-chart {
    width: 100%;
    height: 280px;
  }
  
  .chart-loading, .table-loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 280px;
    
    p {
      margin-top: 12px;
      color: #8a8a8a;
    }
  }
  
  .actions-card {
    margin-top: 16px;
  }
}
</style>