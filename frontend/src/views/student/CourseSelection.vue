<template>
  <div class="course-selection-container">
    <n-card title="课程选择" class="selection-card">
      <!-- 简化后的搜索区域 -->
      <div class="filter-section">
        <n-space vertical :size="16">
          <n-input 
            v-model:value="searchKeyword" 
            placeholder="搜索课程名称、课程代码或教师..." 
            clearable
            @keyup.enter="handleSearch"
          >
            <template #suffix>
              <n-icon :component="SearchOutline" class="search-icon" @click="handleSearch" />
            </template>
          </n-input>
          
          <div class="action-buttons">
            <n-space>
              <n-button type="primary" @click="handleSearch">查询</n-button>
              <n-button @click="resetFilters">重置</n-button>
            </n-space>
          </div>
        </n-space>
      </div>

      <!-- 课程列表 -->
      <div class="course-list-section">
        <n-data-table
          ref="tableRef"
          :columns="columns"
          :data="courses"
          :loading="loading"
          :pagination="pagination"
          :row-key="(row) => row.id"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
          :scroll-x="1200"
          striped
        />
      </div>
    </n-card>

    <!-- 课程详情模态框 -->
    <n-modal v-model:show="showDetailModal" class="detail-modal" preset="card" title="课程详情" style="max-width: 600px">
      <div v-if="currentCourse" class="course-detail">
        <div class="course-header">
          <h2>{{ currentCourse.courseName }}</h2>
          <n-tag :type="currentCourse.status === 'active' ? 'success' : 'error'">
            {{ currentCourse.status === 'active' ? '可选' : '不可选' }}
          </n-tag>
        </div>
        
        <div class="course-info">
          <div class="info-item"><span class="label">课程代码：</span>{{ currentCourse.courseCode }}</div>
          <div class="info-item"><span class="label">学分：</span>{{ currentCourse.credit }}</div>
          <div class="info-item"><span class="label">类型：</span>{{ currentCourse.courseType }}</div>
          <div class="info-item"><span class="label">教师：</span>{{ currentCourse.teacherName }}</div>
          <div class="info-item"><span class="label">学期：</span>{{ currentCourse.semester }}</div>
          <div class="info-item"><span class="label">教室：</span>{{ currentCourse.classroom }}</div>
          <div class="info-item"><span class="label">上课时间：</span>{{ currentCourse.schedule }}</div>
          <div class="info-item"><span class="label">选课情况：</span>{{ currentCourse.selectedCount }}/{{ currentCourse.capacity }}</div>
        </div>
        
        <n-divider />
        
        <div class="course-description">
          <p class="label">课程描述：</p>
          <p class="description-text">{{ currentCourse.description || '暂无描述' }}</p>
        </div>
        
        <div class="action-buttons">
          <n-button 
            type="primary" 
            :disabled="!currentCourse?.selectable"
            @click="selectCourse(currentCourse)"
            :loading="submitting"
          >
            选择课程
          </n-button>
        </div>
      </div>
    </n-modal>

    <!-- 选课确认框 -->
    <n-modal v-model:show="showConfirmModal" preset="dialog" title="确认选课">
      <template #content>
        <p>确定要选择 <strong>{{ currentCourse?.courseName }}</strong> 吗？</p>
        <p>该课程由 {{ currentCourse?.teacherName }} 授课，学分为 {{ currentCourse?.credit }}。</p>
      </template>
      <template #action>
        <n-space>
          <n-button @click="showConfirmModal = false">取消</n-button>
          <n-button type="primary" @click="confirmSelectCourse" :loading="submitting">确认选课</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import { 
  NCard, 
  NInput, 
  NIcon, 
  NButton, 
  NSpace, 
  NDataTable, 
  NPagination, 
  NModal,
  NTag,
  NDivider,
  useMessage
} from 'naive-ui'
import { 
  SearchOutline, 
  InformationCircleOutline,
  AddOutline,
  CheckmarkCircleOutline
} from '@vicons/ionicons5'
import { course, selection } from '@/api'
import { useUserStore } from '@/stores/user'
import type { Course } from '@/types/api'
import type { DataTableColumns } from 'naive-ui'

const message = useMessage()
const userStore = useUserStore()

// 简化后只保留关键字搜索
const searchKeyword = ref('')

// 课程数据
const courses = ref<Course[]>([])
const loading = ref(false)
const pagination = reactive({
  page: 1,
  pageSize: 10,
  pageCount: 1,
  showSizePicker: true,
  pageSizes: [10, 20, 30, 50]
})

// 选课操作相关
const currentCourse = ref<Course | null>(null)
const showDetailModal = ref(false)
const showConfirmModal = ref(false)
const submitting = ref(false)

// 已选课程的ID集合，用于检查是否已选
const selectedCourseIds = ref<Set<number>>(new Set())

// 获取已选课程列表
const fetchSelectedCourses = async () => {
  if (!userStore.studentId) return
  
  try {
    const selections = await selection.getStudentCourses(userStore.studentId)
    selectedCourseIds.value = new Set(
      selections
        .filter(item => item.status === 'selected')
        .map(item => item.courseId)
    )
  } catch (error) {
    console.error('获取已选课程失败:', error)
  }
}

// 检查是否可以选择该课程
const canSelect = (course: Course) => {
  if (!course) return false
  return course.selectable === true
}

// 表格列定义
const columns = ref<DataTableColumns<Course>>([
  { 
    title: '课程代码', 
    key: 'courseCode', 
    width: 120,
    fixed: 'left'
  },
  { 
    title: '课程名称', 
    key: 'courseName', 
    width: 200,
    fixed: 'left',
    ellipsis: {
      tooltip: true
    }
  },
  { title: '教师', key: 'teacherName', width: 120 },
  { title: '学分', key: 'credit', width: 80 },
  { title: '类型', key: 'courseType', width: 100 },
  { 
    title: '选课情况', 
    key: 'capacity', 
    width: 100,
    render(row) {
      const percent = Math.floor((row.selectedCount / row.capacity) * 100)
      let type = 'success'
      
      if (percent >= 80) {
        type = 'warning'
      }
      if (percent >= 100) {
        type = 'error'
      }
      
      return h('div', { class: 'capacity-wrapper' }, [
        h('div', { class: 'capacity-text' }, `${row.selectedCount}/${row.capacity}`),
        h('div', { class: 'capacity-bar-bg' }, [
          h('div', { 
            class: `capacity-bar capacity-bar-${type}`,
            style: { width: `${Math.min(100, percent)}%` }
          })
        ])
      ])
    }
  },
  { title: '学期', key: 'semester', width: 100 },
  { title: '教室', key: 'classroom', width: 100 },
  { title: '上课时间', key: 'schedule', width: 200 },
  { 
    title: '状态', 
    key: 'status', 
    width: 80,
    render(row) {
      return h(
        NTag,
        { type: row.selectable ? 'success' : 'error' },
        { default: () => row.selectable ? '可选' : '不可选' }
      )
    }
  },
  { 
    title: '已选', 
    key: 'selected', 
    width: 80,
    render(row) {
      return selectedCourseIds.value.has(row.id) 
        ? h(NIcon, { color: '#18a058', size: 20 }, { default: () => h(CheckmarkCircleOutline) })
        : ''
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render(row) {
      return h(
        NSpace,
        { justify: 'center' },
        {
          default: () => [
            h(
              NButton,
              {
                tertiary: true,
                size: 'small',
                type: 'info',
                onClick: () => viewCourseDetail(row)
              },
              { default: () => '详情' }
            ),
            h(
              NButton,
              {
                tertiary: true,
                size: 'small',
                type: 'primary',
                disabled: !row.selectable, // 使用 selectable 字段决定按钮是否禁用
                onClick: () => selectCourse(row)
              },
              { 
                default: () => row.selectable ? '选课' : '不可选'
              }
            )
          ]
        }
      )
    }
  }
])

// 查看课程详情
const viewCourseDetail = (row: Course) => {
  currentCourse.value = row
  showDetailModal.value = true
}

// 选择课程
const selectCourse = (row: Course) => {
  currentCourse.value = row
  showConfirmModal.value = true
}

// 确认选课
const confirmSelectCourse = async () => {
  if (!currentCourse.value || !userStore.studentId) return
  
  submitting.value = true
  try {
    await selection.selectCourse(userStore.studentId, currentCourse.value.id)
    message.success('选课成功')
    
    // 更新已选课程列表和当前页面数据
    await fetchSelectedCourses()
    await fetchCourses()
    
    showConfirmModal.value = false
    showDetailModal.value = false
  } catch (error: any) {
    console.error('选课失败:', error)
    message.error(error.message || '选课失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 获取课程列表
const fetchCourses = async () => {
  loading.value = true
  try {
    // 简化后只使用分页和关键字搜索
    const params = {
      current: pagination.page,
      size: pagination.pageSize,
      keyword: searchKeyword.value
    }
    
    const response = await course.getCourses(params)
    courses.value = response.records
    pagination.pageCount = response.pages
    
  } catch (error) {
    console.error('获取课程列表失败:', error)
    message.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  fetchCourses()
}

// 重置筛选条件
const resetFilters = () => {
  searchKeyword.value = ''
  handleSearch()
}

// 页码变化处理
const handlePageChange = (page: number) => {
  pagination.page = page
  fetchCourses()
}

// 每页数量变化处理
const handlePageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize
  pagination.page = 1
  fetchCourses()
}

// 初始化
onMounted(async () => {
  // 先获取已选课程列表，再获取可选课程
  await fetchSelectedCourses()
  await fetchCourses()
})
</script>

<style scoped lang="scss">
.course-selection-container {
  padding: 16px;
  
  .selection-card {
    margin-bottom: 16px;
  }
  
  .filter-section {
    margin-bottom: 20px;
    
    .search-icon {
      cursor: pointer;
    }
    
    .action-buttons {
      display: flex;
      justify-content: flex-end;
      margin-top: 8px;
    }
  }
  
  .course-list-section {
    .capacity-wrapper {
      position: relative;
      width: 100%;
      
      .capacity-text {
        font-size: 12px;
        margin-bottom: 4px;
      }
      
      .capacity-bar-bg {
        width: 100%;
        height: 6px;
        background-color: #eee;
        border-radius: 3px;
        overflow: hidden;
        
        .capacity-bar {
          height: 100%;
          border-radius: 3px;
          transition: width 0.3s ease;
          
          &.capacity-bar-success {
            background-color: #18a058;
          }
          
          &.capacity-bar-warning {
            background-color: #f0a020;
          }
          
          &.capacity-bar-error {
            background-color: #d03050;
          }
        }
      }
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