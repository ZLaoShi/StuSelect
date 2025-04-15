<template>
  <div class="my-courses-container">
    <n-card title="我的课程" class="my-courses-card">
      <template #header-extra>
        <n-button text type="primary" @click="refreshData">
          <template #icon>
            <n-icon><RefreshOutline /></n-icon>
          </template>
          刷新数据
        </n-button>
      </template>

      <!-- 课程列表 -->
      <n-tabs type="line" animated>
        <n-tab-pane name="selected" tab="已选课程">
          <div v-if="loading" class="loading-container">
            <n-spin size="medium" />
            <p>加载中...</p>
          </div>
          <div v-else-if="selectedCourses.length === 0" class="empty-container">
            <n-empty description="暂无已选课程">
              <template #extra>
                <n-button type="primary" @click="gotoSelection">去选课</n-button>
              </template>
            </n-empty>
          </div>
          <n-data-table
            v-else
            :columns="selectedColumns"
            :data="selectedCourses"
            :pagination="pagination"
            :bordered="false"
            striped
            size="medium"
          />
        </n-tab-pane>
        
        <n-tab-pane name="completed" tab="已完成课程">
          <div v-if="loading" class="loading-container">
            <n-spin size="medium" />
            <p>加载中...</p>
          </div>
          <div v-else-if="completedCourses.length === 0" class="empty-container">
            <n-empty description="暂无已完成课程" />
          </div>
          <n-data-table
            v-else
            :columns="completedColumns"
            :data="completedCourses"
            :pagination="pagination"
            :bordered="false"
            striped
            size="medium"
          />
        </n-tab-pane>
        
        <n-tab-pane name="dropped" tab="已退选课程">
          <div v-if="loading" class="loading-container">
            <n-spin size="medium" />
            <p>加载中...</p>
          </div>
          <div v-else-if="droppedCourses.length === 0" class="empty-container">
            <n-empty description="暂无已退选课程" />
          </div>
          <n-data-table
            v-else
            :columns="droppedColumns"
            :data="droppedCourses"
            :pagination="pagination"
            :bordered="false"
            striped
            size="medium"
          />
        </n-tab-pane>
      </n-tabs>
    </n-card>

    <!-- 课程详情模态框 -->
    <n-modal v-model:show="showDetailModal" class="detail-modal" preset="card" title="课程详情" style="max-width: 600px">
      <div v-if="currentCourse" class="course-detail">
        <div class="course-header">
          <h2>{{ currentCourse.course.courseName }}</h2>
          <n-tag :type="currentCourse.course.status === 'active' ? 'success' : 'error'">
            {{ currentCourse.course.status === 'active' ? '进行中' : '已结束' }}
          </n-tag>
        </div>
        
        <div class="course-info">
          <div class="info-item"><span class="label">课程代码：</span>{{ currentCourse.course.courseCode }}</div>
          <div class="info-item"><span class="label">学分：</span>{{ currentCourse.course.credit }}</div>
          <div class="info-item"><span class="label">类型：</span>{{ currentCourse.course.courseType }}</div>
          <div class="info-item"><span class="label">教师：</span>{{ currentCourse.course.teacherName }}</div>
          <div class="info-item"><span class="label">学期：</span>{{ currentCourse.course.semester }}</div>
          <div class="info-item"><span class="label">教室：</span>{{ currentCourse.course.classroom }}</div>
          <div class="info-item"><span class="label">上课时间：</span>{{ currentCourse.course.schedule }}</div>
          <div class="info-item"><span class="label">选课情况：</span>{{ currentCourse.course.selectedCount }}/{{ currentCourse.course.capacity }}</div>
          <div class="info-item" v-if="currentCourse.score !== null">
            <span class="label">成绩：</span>{{ currentCourse.score }} ({{ currentCourse.scoreType || '百分制' }})
          </div>
          <div class="info-item"><span class="label">选课时间：</span>{{ formatDateTime(currentCourse.selectTime) }}</div>
        </div>
        
        <n-divider />
        
        <div class="course-description">
          <p class="label">课程描述：</p>
          <p class="description-text">{{ currentCourse.course.description || '暂无描述' }}</p>
        </div>
        
        <div class="action-buttons" v-if="currentCourse.status === 'selected'">
          <n-popconfirm @positive-click="confirmDropCourse">
            <template #trigger>
              <n-button type="error" :loading="submitting">
                退选课程
              </n-button>
            </template>
            确定要退选 {{ currentCourse.course.courseName }} 吗？
          </n-popconfirm>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { selection } from '@/api'
import type { CourseSelection } from '@/types/api'
import { 
  NCard, 
  NDataTable, 
  NTabs, 
  NTabPane, 
  NButton, 
  NEmpty, 
  NModal, 
  NSpin,
  NDivider,
  NTag,
  NPopconfirm,
  NIcon,
  useMessage
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { 
  BookOutline, 
  TimeOutline, 
  RefreshOutline,
  InformationCircleOutline
} from '@vicons/ionicons5'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

// 数据状态
const loading = ref(false)
const submitting = ref(false)
const allCourseSelections = ref<CourseSelection[]>([])
const currentCourse = ref<CourseSelection | null>(null)
const showDetailModal = ref(false)

// 分页配置
const pagination = {
  pageSize: 10
}

// 按状态分组的课程
const selectedCourses = computed(() => {
  return allCourseSelections.value.filter(course => course.status === 'selected')
})

const completedCourses = computed(() => {
  return allCourseSelections.value.filter(course => course.status === 'completed')
})

const droppedCourses = computed(() => {
  return allCourseSelections.value.filter(course => course.status === 'dropped')
})

// 已选课程表格列
const selectedColumns: DataTableColumns<CourseSelection> = [
  { 
    title: '课程代码', 
    key: 'courseCode',
    width: 120,
    render(row) {
      return row.course?.courseCode || '-'
    }
  },
  { 
    title: '课程名称', 
    key: 'courseName',
    width: 200,
    render(row) {
      return row.course?.courseName || '-'
    }
  },
  { 
    title: '教师', 
    key: 'teacherName',
    width: 120,
    render(row) {
      return row.course?.teacherName || '-'
    }
  },
  { 
    title: '学分', 
    key: 'credit',
    width: 80,
    render(row) {
      return row.course?.credit || '-'
    }
  },
  { 
    title: '上课时间', 
    key: 'schedule',
    width: 200,
    render(row) {
      return row.course?.schedule || '-'
    }
  },
  { 
    title: '教室', 
    key: 'classroom',
    width: 120,
    render(row) {
      return row.course?.classroom || '-'
    }
  },
  { 
    title: '选课时间', 
    key: 'selectTime',
    width: 180,
    render(row) {
      return formatDateTime(row.selectTime)
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render(row) {
      return h('div', { class: 'action-buttons' }, [
        h(
          NButton,
          {
            size: 'small',
            type: 'info',
            onClick: () => viewCourseDetail(row)
          },
          { default: () => '详情' }
        ),
        h(
          NPopconfirm,
          {
            onPositiveClick: () => dropCourse(row)
          },
          {
            trigger: () => h(
              NButton,
              {
                size: 'small',
                type: 'error',
                style: 'margin-left: 8px'
              },
              { default: () => '退选' }
            ),
            default: () => `确定要退选 ${row.course?.courseName || '此课程'} 吗？`
          }
        )
      ])
    }
  }
]

// 已完成课程表格列
const completedColumns: DataTableColumns<CourseSelection> = [
  { 
    title: '课程代码', 
    key: 'courseCode',
    width: 120,
    render(row) {
      return row.course?.courseCode || '-'
    }
  },
  { 
    title: '课程名称', 
    key: 'courseName',
    width: 200,
    render(row) {
      return row.course?.courseName || '-'
    }
  },
  { 
    title: '教师', 
    key: 'teacherName',
    width: 120,
    render(row) {
      return row.course?.teacherName || '-'
    }
  },
  { 
    title: '学分', 
    key: 'credit',
    width: 80,
    render(row) {
      return row.course?.credit || '-'
    }
  },
  { 
    title: '成绩', 
    key: 'score',
    width: 100,
    render(row) {
      if (row.score === null || row.score === undefined) return '-'
      return `${row.score} (${row.scoreType || '百分制'})`
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render(row) {
      return h(
        NButton,
        {
          size: 'small',
          type: 'info',
          onClick: () => viewCourseDetail(row)
        },
        { default: () => '详情' }
      )
    }
  }
]

// 已退选课程表格列
const droppedColumns: DataTableColumns<CourseSelection> = [
  { 
    title: '课程代码', 
    key: 'courseCode',
    width: 120,
    render(row) {
      return row.course?.courseCode || '-'
    }
  },
  { 
    title: '课程名称', 
    key: 'courseName',
    width: 200,
    render(row) {
      return row.course?.courseName || '-'
    }
  },
  { 
    title: '教师', 
    key: 'teacherName',
    width: 120,
    render(row) {
      return row.course?.teacherName || '-'
    }
  },
  { 
    title: '学分', 
    key: 'credit',
    width: 80,
    render(row) {
      return row.course?.credit || '-'
    }
  },
  { 
    title: '选课时间', 
    key: 'selectTime',
    width: 180,
    render(row) {
      return formatDateTime(row.selectTime)
    }
  },
  { 
    title: '退课时间', 
    key: 'updatedAt',
    width: 180,
    render(row) {
      return formatDateTime(row.updatedAt || '')
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render(row) {
      return h(
        NButton,
        {
          size: 'small',
          type: 'info',
          onClick: () => viewCourseDetail(row)
        },
        { default: () => '详情' }
      )
    }
  }
]

// 格式化日期时间
function formatDateTime(dateTimeStr: string) {
  if (!dateTimeStr) return '-'
  
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`
}

// 数字补零
function padZero(num: number) {
  return num < 10 ? `0${num}` : num
}

// 获取所有选课数据
async function fetchCourseSelections() {
  if (!userStore.studentId) {
    return
  }
  
  loading.value = true
  try {
    // 使用新的联查接口
    const data = await selection.getStudentCoursesWithDetails(userStore.studentId)
    allCourseSelections.value = data
  } catch (error) {
    console.error('获取选课数据失败:', error)
    message.error('获取选课数据失败')
  } finally {
    loading.value = false
  }
}

// 刷新数据
function refreshData() {
  fetchCourseSelections()
}

// 查看课程详情
function viewCourseDetail(course: CourseSelection) {
  currentCourse.value = course
  showDetailModal.value = true
}

// 退选课程
async function dropCourse(course: CourseSelection) {
  if (!userStore.studentId) return
  
  submitting.value = true
  try {
    await selection.dropCourse(userStore.studentId, course.courseId)
    message.success('退选成功')
    
    // 刷新数据
    await fetchCourseSelections()
    
    // 如果是在详情页面退选的，关闭详情页面
    if (showDetailModal.value && currentCourse.value?.id === course.id) {
      showDetailModal.value = false
    }
  } catch (error: any) {
    console.error('退选失败:', error)
    message.error(error.message || '退选失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 从详情页面确认退选
function confirmDropCourse() {
  if (currentCourse.value) {
    dropCourse(currentCourse.value)
  }
}

// 跳转到选课页面
function gotoSelection() {
  router.push('/student/courses')
}

// 组件挂载时获取数据
onMounted(() => {
  fetchCourseSelections()
})
</script>

<style scoped lang="scss">
.my-courses-container {
  padding: 16px;
  
  .my-courses-card {
    margin-bottom: 16px;
  }
  
  .loading-container, .empty-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 0;
    
    p {
      margin-top: 12px;
      color: #8a8a8a;
    }
  }
  
  .course-detail {
    .course-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      h2 {
        margin: 0;
      }
    }
    
    .course-info {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;
      margin-bottom: 16px;
      
      .info-item {
        .label {
          font-weight: 600;
          color: #666;
          margin-right: 4px;
        }
      }
    }
    
    .course-description {
      margin-bottom: 20px;
      
      .label {
        font-weight: 600;
        color: #666;
        margin-bottom: 8px;
      }
      
      .description-text {
        white-space: pre-line;
        line-height: 1.6;
        color: #333;
      }
    }
    
    .action-buttons {
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>