<template>
  <div class="course-management-container">
    <n-card title="课程管理" class="course-card">
      <template #header-extra>
        <n-button type="primary" @click="showAddModal = true">
          <template #icon>
            <n-icon><AddOutline /></n-icon>
          </template>
          添加课程
        </n-button>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <n-input
          v-model:value="searchKeyword"
          placeholder="搜索课程名称、代码或教师"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #suffix>
            <n-icon :component="SearchOutline" @click="handleSearch" />
          </template>
        </n-input>
      </div>

      <!-- 数据表格 -->
      <n-data-table
        :columns="columns"
        :data="courses"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row) => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
        stripe
        size="medium"
      />

      <!-- 添加/编辑课程的模态框 -->
      <n-modal v-model:show="showAddModal" preset="card" title="添加课程" style="width: 600px">
        <n-form
          ref="formRef"
          :model="courseForm"
          :rules="rules"
          label-placement="left"
          label-width="auto"
          require-mark-placement="right-hanging"
        >
          <n-grid :cols="24" :x-gap="24">
            <n-form-item-gi :span="12" label="课程代码" path="courseCode">
              <n-input v-model:value="courseForm.courseCode" placeholder="请输入课程代码" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="课程名称" path="courseName">
              <n-input v-model:value="courseForm.courseName" placeholder="请输入课程名称" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="学分" path="credit">
              <n-input-number
                v-model:value="courseForm.credit"
                placeholder="请输入学分"
                :min="0"
                :step="0.5"
              />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="课程类型" path="courseType">
              <n-select
                v-model:value="courseForm.courseType"
                :options="courseTypeOptions"
                placeholder="请选择课程类型"
              />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="教师姓名" path="teacherName">
              <n-input v-model:value="courseForm.teacherName" placeholder="请输入教师姓名" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="容量" path="capacity">
              <n-input-number
                v-model:value="courseForm.capacity"
                placeholder="请输入容量"
                :min="0"
                :step="1"
              />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="学期" path="semester">
              <n-input v-model:value="courseForm.semester" placeholder="例：2025-春季" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="教室" path="classroom">
              <n-input v-model:value="courseForm.classroom" placeholder="请输入教室" />
            </n-form-item-gi>
            <n-form-item-gi :span="24" label="上课时间" path="schedule">
              <n-input v-model:value="courseForm.schedule" placeholder="例：周一 08:00-10:00, 周三 15:00-17:00" />
            </n-form-item-gi>
            <n-form-item-gi :span="24" label="课程描述" path="description">
              <n-input
                v-model:value="courseForm.description"
                type="textarea"
                placeholder="请输入课程描述"
                :autosize="{ minRows: 3, maxRows: 5 }"
              />
            </n-form-item-gi>
          </n-grid>
        </n-form>

        <template #footer>
          <div class="modal-footer">
            <n-button @click="showAddModal = false">取消</n-button>
            <n-button type="primary" @click="handleSaveCourse" :loading="submitting">
              保存
            </n-button>
          </div>
        </template>
      </n-modal>

      <!-- 查看学生列表的模态框 -->
      <n-modal v-model:show="showStudentsModal" preset="card" title="已选学生列表" style="width: 800px">
        <div v-if="currentCourse" class="course-info">
          <h3>{{ currentCourse.courseName }} ({{ currentCourse.courseCode }})</h3>
          <p>教师: {{ currentCourse.teacherName }} | 已选人数: {{ currentCourse.selectedCount }}/{{ currentCourse.capacity }}</p>
        </div>
        <n-data-table
          :columns="studentColumns"
          :data="courseStudents"
          :loading="loadingStudents"
          size="small"
          :row-key="(row) => row.id"
        />
      </n-modal>

      <!-- 添加录入成绩的模态框 -->
      <n-modal v-model:show="showScoreModal" preset="card" title="录入成绩" style="width: 400px">
        <div v-if="currentSelection" class="student-score-info">
          <p><strong>学生:</strong> {{ currentSelection.student?.studentNo || '未知' }} - {{ currentSelection.student?.realName || currentSelection.student?.user?.realName || '未知' }}</p>
          <p><strong>课程:</strong> {{ currentSelection.course?.courseName || currentCourse?.courseName || '未知' }}</p>
        </div>

        <n-form
          ref="scoreFormRef"
          :model="scoreForm"
          :rules="scoreRules"
          label-placement="left"
          label-width="auto"
          require-mark-placement="right-hanging"
        >
          <n-form-item label="成绩" path="score">
            <n-input-number
              v-model:value="scoreForm.score"
              placeholder="请输入成绩"
              :min="0"
              :max="100"
              :step="1"
              style="width: 100%"
            />
          </n-form-item>
          <n-form-item label="成绩类型" path="scoreType">
            <n-select
              v-model:value="scoreForm.scoreType"
              :options="scoreTypeOptions"
              placeholder="请选择成绩类型"
            />
          </n-form-item>
        </n-form>

        <template #footer>
          <div class="modal-footer">
            <n-button @click="showScoreModal = false">取消</n-button>
            <n-button type="primary" @click="saveScore" :loading="submittingScore">
              保存
            </n-button>
          </div>
        </template>
      </n-modal>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import type { FormInst, DataTableColumns, DropdownOption } from 'naive-ui'
import { 
  NCard, 
  NButton, 
  NDataTable, 
  NModal, 
  NForm, 
  NFormItem, 
  NFormItemGi,
  NInput, 
  NInputNumber,
  NSelect,
  NGrid,
  NIcon,
  NSpace,
  NTag,
  NPopconfirm,
  NDropdown
} from 'naive-ui'
import { 
  AddOutline, 
  SearchOutline, 
  PencilOutline, 
  TrashOutline, 
  PeopleOutline,
  EllipsisHorizontalOutline
} from '@vicons/ionicons5'
import { course, selection } from '@/api'
import type { Course, CourseSelection } from '@/types/api'

const message = useMessage()
const formRef = ref<FormInst | null>(null)

// 列表数据和加载状态
const courses = ref<Course[]>([])
const loading = ref(true)
const searchKeyword = ref('')
const pagination = reactive({
  page: 1,
  pageSize: 10,
  pageCount: 1,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page: number) => {
    pagination.page = page
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.pageSize = pageSize
    pagination.page = 1
  }
})

// 表单数据
const showAddModal = ref(false)
const submitting = ref(false)
const isEditing = ref(false)
const courseForm = reactive({
  id: null as number | null,
  courseCode: '',
  courseName: '',
  credit: 0,
  courseType: '',
  teacherName: '',
  capacity: 0,
  selectedCount: 0,
  semester: '',
  classroom: '',
  schedule: '',
  description: '',
  status: 'active'
})

// 学生列表数据
const showStudentsModal = ref(false)
const currentCourse = ref<Course | null>(null)
const courseStudents = ref<CourseSelection[]>([])
const loadingStudents = ref(false)

// 下拉选项
const courseTypeOptions = [
  { label: '必修课', value: '必修' },
  { label: '选修课', value: '选修' },
  { label: '专业核心课', value: '专业核心' },
  { label: '通识课', value: '通识' }
]

// 表单验证规则
const rules = {
  courseCode: {
    required: true,
    message: '请输入课程代码',
    trigger: 'blur'
  },
  courseName: {
    required: true,
    message: '请输入课程名称',
    trigger: 'blur'
  },
  credit: {
    required: true,
    type: 'number' as const, // 添加 as const 将字符串字面量转换为字面量类型
    message: '请输入学分',
    trigger: 'blur'
  },
  courseType: {
    required: true,
    message: '请选择课程类型',
    trigger: 'blur'
  },
  teacherName: {
    required: true,
    message: '请输入教师姓名',
    trigger: 'blur'
  },
  capacity: {
    required: true,
    type: 'number' as const, // 添加 as const
    message: '请输入容量',
    trigger: 'blur'
  }
}

// 课程表格列定义
const renderCourseStatusTag = (status: string) => {
  const statusMap: Record<string, { type: string, text: string }> = {
    active: { type: 'success', text: '正常' },
    cancelled: { type: 'error', text: '已取消' },
    finished: { type: 'info', text: '已结束' }
  }
  
  const tagInfo = statusMap[status] || { type: 'default', text: status }
  
  return h(NTag, { type: tagInfo.type as any }, { default: () => tagInfo.text })
}

const createActionDropdown = (row: Course) => {
  const options: DropdownOption[] = [
    {
      label: '编辑课程',
      key: 'edit',
      icon: () => h(NIcon, null, { default: () => h(PencilOutline) })
    },
    {
      label: '查看学生',
      key: 'students',
      icon: () => h(NIcon, null, { default: () => h(PeopleOutline) })
    },
    {
      type: 'divider',
      key: 'd1'
    }
  ]
  
  if (row.status === 'active') {
    options.push({
      label: '取消课程',
      key: 'cancel',
      icon: () => h(NIcon, null, { default: () => h(TrashOutline) })
    })
  } else if (row.status === 'cancelled') {
    options.push({
      label: '恢复课程',
      key: 'restore',
      icon: () => h(NIcon, null, { default: () => h(AddOutline) })
    })
  }
  
  return h(
    NDropdown,
    {
      trigger: 'click',
      options,
      onSelect: (key) => handleAction(key, row)
    },
    {
      default: () => h(
        NButton,
        { circle: true, quaternary: true },
        { icon: () => h(NIcon, null, { default: () => h(EllipsisHorizontalOutline) }) }
      )
    }
  )
}

const columns: DataTableColumns<Course> = [
  { title: '课程代码', key: 'courseCode', width: 120 },
  { title: '课程名称', key: 'courseName', width: 200 },
  { title: '教师', key: 'teacherName', width: 120 },
  { title: '学分', key: 'credit', width: 80 },
  { title: '类型', key: 'courseType', width: 100 },
  { 
    title: '选课情况', 
    key: 'capacity',
    width: 120,
    render(row) {
      return `${row.selectedCount || 0}/${row.capacity}`
    }
  },
  { title: '学期', key: 'semester', width: 120 },
  { 
    title: '状态', 
    key: 'status',
    width: 100,
    render(row) {
      return renderCourseStatusTag(row.status)
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    fixed: 'right',
    render(row) {
      return createActionDropdown(row)
    }
  }
]

// 学生表格列定义
const studentColumns: DataTableColumns<CourseSelection> = [
  { 
    title: '学号', 
    key: 'studentNo',
    width: 120,
    render(row) {
      return row.student?.studentNo || '未知'
    }
  },
  { 
    title: '姓名', 
    key: 'realName',
    width: 120,
    render(row) {
      return row.student?.realName || row.student?.user?.realName || '未知'
    }
  },
  { 
    title: '班级', 
    key: 'className',
    width: 150,
    render(row) {
      return row.student?.className || '未知'
    }
  },
  { title: '选课时间', key: 'selectTime', width: 180 },
  { 
    title: '成绩', 
    key: 'score',
    width: 100,
    render(row) {
      if (row.score === null || row.score === undefined) return '未录入'
      return `${row.score}分`
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render(row) {
      return h(
        NSpace,
        {},
        {
          default: () => [
            h(
              NButton,
              {
                size: 'small',
                onClick: () => handleSetScore(row)
              },
              { default: () => '录入成绩' }
            ),
            h(
              NPopconfirm,
              {
                onPositiveClick: () => handleRemoveStudent(row)
              },
              {
                default: () => '确定将该学生移出课程吗？',
                trigger: () =>
                  h(
                    NButton,
                    {
                      size: 'small',
                      type: 'error'
                    },
                    { default: () => '移出' }
                  )
              }
            )
          ]
        }
      )
    }
  }
]

// 初始化数据
onMounted(() => {
  fetchCourses()
})

// 获取课程列表
async function fetchCourses() {
  loading.value = true
  try {
    const response = await course.getCourses({
      current: pagination.page,
      size: pagination.pageSize,
      keyword: searchKeyword.value
    })
    
    courses.value = response.records
    pagination.pageCount = response.pages
    
  } catch (error) {
    console.error('获取课程列表失败', error)
    message.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

// 页码变化处理
function handlePageChange(page: number) {
  pagination.page = page
  fetchCourses()
}

// 每页数量变化处理
function handlePageSizeChange(pageSize: number) {
  pagination.pageSize = pageSize
  pagination.page = 1
  fetchCourses()
}

// 搜索处理
function handleSearch() {
  pagination.page = 1
  fetchCourses()
}

// 表单重置
function resetForm() {
  Object.assign(courseForm, {
    id: null,
    courseCode: '',
    courseName: '',
    credit: 0,
    courseType: '',
    teacherName: '',
    capacity: 0,
    selectedCount: 0,
    semester: '',
    classroom: '',
    schedule: '',
    description: '',
    status: 'active'
  })
  isEditing.value = false
}

// 操作处理
async function handleAction(key: string, row: Course) {
  switch (key) {
    case 'edit':
      handleEdit(row)
      break
    case 'students':
      showStudentList(row)
      break
    case 'cancel':
      confirmCancelCourse(row)
      break
    case 'restore':
      restoreCourse(row)
      break
  }
}

// 编辑课程
function handleEdit(row: Course) {
  Object.assign(courseForm, { ...row })
  isEditing.value = true
  showAddModal.value = true
}

// 保存课程
async function handleSaveCourse() {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEditing.value && courseForm.id) {
      // 更新课程
      await course.updateCourse(courseForm.id, courseForm)
      message.success('课程更新成功')
    } else {
      // 新增课程
      await course.createCourse(courseForm)
      message.success('课程添加成功')
    }
    
    showAddModal.value = false
    resetForm()
    fetchCourses()
    
  } catch (error) {
    console.error('保存课程失败', error)
    message.error('保存课程失败')
  } finally {
    submitting.value = false
  }
}

// 确认取消课程
function confirmCancelCourse(row: Course) {
  if (row.selectedCount && row.selectedCount > 0) {
    message.warning(`该课程已有${row.selectedCount}名学生选课，确定要取消吗？`)
  }
  updateCourseStatus(row.id, 'cancelled')
}

// 恢复课程
function restoreCourse(row: Course) {
  updateCourseStatus(row.id, 'active')
}

// 更新课程状态
async function updateCourseStatus(courseId: number, status: string) {
  try {
    await course.updateCourseStatus(courseId, status)
    message.success(`课程${status === 'active' ? '恢复' : '取消'}成功`)
    fetchCourses()
  } catch (error) {
    console.error('更新课程状态失败', error)
    message.error('操作失败')
  }
}

// 显示学生列表
async function showStudentList(row: Course) {
  currentCourse.value = row
  showStudentsModal.value = true
  loadingStudents.value = true
  
  try {
    courseStudents.value = await selection.getCourseStudents(row.id)
  } catch (error) {
    console.error('获取课程学生列表失败', error)
    message.error('获取学生列表失败')
  } finally {
    loadingStudents.value = false
  }
}

// 录入成绩
function handleSetScore(row: CourseSelection) {
  currentSelection.value = row
  scoreForm.score = row.score || 0
  scoreForm.scoreType = row.scoreType || '百分制'
  showScoreModal.value = true
}

// 保存成绩
async function saveScore() {
  if (!scoreFormRef.value || !currentSelection.value) return
  
  try {
    await scoreFormRef.value.validate()
    submittingScore.value = true
    
    const success = await selection.setScore(
      currentSelection.value.id,
      scoreForm.score,
      scoreForm.scoreType
    )
    
    if (success) {
      message.success('成绩录入成功')
      
      // 刷新学生列表
      if (currentCourse.value) {
        courseStudents.value = await selection.getCourseStudents(currentCourse.value.id)
      }
      
      showScoreModal.value = false
    } else {
      message.error('成绩录入失败')
    }
  } catch (error) {
    console.error('成绩录入失败', error)
    message.error('成绩录入失败')
  } finally {
    submittingScore.value = false
  }
}

// 成绩模态框相关数据
const showScoreModal = ref(false)
const submittingScore = ref(false)
const currentSelection = ref<CourseSelection | null>(null)
const scoreFormRef = ref<FormInst | null>(null)

const scoreForm = reactive({
  score: 0,
  scoreType: '百分制'
})

const scoreTypeOptions = [
  { label: '百分制', value: '百分制' },
  { label: '五分制', value: '五分制' },
  { label: '等级制', value: '等级制' },
]

const scoreRules = {
  score: {
    required: true,
    type: 'number' as const,
    message: '请输入成绩',
    trigger: 'blur'
  },
  scoreType: {
    required: true,
    message: '请选择成绩类型',
    trigger: 'blur'
  }
}
</script>

<style scoped lang="scss">
.student-score-info {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
  
  p {
    margin: 6px 0;
    color: #666;
  }
}

.course-management-container {
  padding: 16px;
  
  .course-card {
    margin-bottom: 20px;
  }
  
  .search-bar {
    margin-bottom: 16px;
    max-width: 400px;
  }
  
  .modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 16px;
  }
  
  .course-info {
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid #eee;
    
    h3 {
      margin: 0 0 8px 0;
    }
    
    p {
      margin: 0;
      color: #666;
    }
  }
}
</style>