<template>
    <div class="student-management-container">
      <n-card title="学生管理" class="student-card">
        <!-- 搜索栏 -->
        <div class="search-bar">
          <n-input
            v-model:value="searchKeyword"
            placeholder="搜索学号、姓名或班级"
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
          :data="students"
          :loading="loading"
          :pagination="pagination"
          :row-key="(row) => row.id"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
          stripe
          size="medium"
        />
  
        <!-- 查看学生选课详情的模态框 -->
        <n-modal v-model:show="showCoursesModal" preset="card" title="已选课程列表" style="width: 800px">
          <div v-if="currentStudent" class="student-info">
            <h3>{{ currentStudent.studentNo }} - {{ currentStudent.realName || currentStudent.user?.realName || '未知' }}</h3>
            <p>班级: {{ currentStudent.className || '未知' }} | 专业: {{ currentStudent.major || '未知' }}</p>
          </div>
          <n-data-table
            :columns="courseColumns"
            :data="studentCourses"
            :loading="loadingCourses"
            size="small"
            :row-key="(row) => row.id"
          />
        </n-modal>
  
        <!-- 编辑学生信息的模态框 -->
        <n-modal v-model:show="showEditModal" preset="card" title="编辑学生信息" style="width: 600px">
          <n-form
            ref="formRef"
            :model="studentForm"
            :rules="rules"
            label-placement="left"
            label-width="auto"
            require-mark-placement="right-hanging"
          >
            <n-grid :cols="24" :x-gap="24">
              <n-form-item-gi :span="12" label="学号" path="studentNo">
                <n-input v-model:value="studentForm.studentNo" placeholder="请输入学号" />
              </n-form-item-gi>
              <n-form-item-gi :span="12" label="姓名" path="realName">
                <n-input v-model:value="studentForm.realName" placeholder="请输入姓名" />
              </n-form-item-gi>
              <n-form-item-gi :span="12" label="性别" path="gender">
                <n-select
                  v-model:value="studentForm.gender"
                  :options="genderOptions"
                  placeholder="请选择性别"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="12" label="出生日期" path="birthDate">
                <n-date-picker
                  v-model:value="studentForm.birthDate"
                  type="date"
                  style="width: 100%"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="12" label="年级" path="grade">
                <n-input-number
                  v-model:value="studentForm.grade"
                  placeholder="请输入年级"
                  :min="2000"
                  :max="2100"
                  style="width: 100%"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="12" label="专业" path="major">
                <n-input v-model:value="studentForm.major" placeholder="请输入专业" />
              </n-form-item-gi>
              <n-form-item-gi :span="12" label="班级" path="className">
                <n-input v-model:value="studentForm.className" placeholder="请输入班级" />
              </n-form-item-gi>
              <n-form-item-gi :span="12" label="入学日期" path="admissionDate">
                <n-date-picker
                  v-model:value="studentForm.admissionDate"
                  type="date"
                  style="width: 100%"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="12" label="邮箱" path="email">
                <n-input v-model:value="studentForm.email" placeholder="请输入邮箱" />
              </n-form-item-gi>
              <n-form-item-gi :span="12" label="电话" path="phone">
                <n-input v-model:value="studentForm.phone" placeholder="请输入电话" />
              </n-form-item-gi>
              <n-form-item-gi :span="12" label="账号状态" path="status">
                <n-select
                  v-model:value="studentForm.status"
                  :options="statusOptions"
                  placeholder="请选择状态"
                />
              </n-form-item-gi>
            </n-grid>
          </n-form>
  
          <template #footer>
            <div class="modal-footer">
              <n-button @click="showEditModal = false">取消</n-button>
              <n-button type="primary" @click="handleSaveStudent" :loading="submitting">
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
    NDropdown,
    NDatePicker
  } from 'naive-ui'
  import { 
    SearchOutline, 
    PencilOutline, 
    PersonOutline,
    BookOutline,
    EllipsisHorizontalOutline
  } from '@vicons/ionicons5'
  import { selection, student } from '@/api'
  import type { Student, CourseSelection, User } from '@/types/api'
  
  const message = useMessage()
  const formRef = ref<FormInst | null>(null)
  
  // 列表数据和加载状态
  const students = ref<(Student & { user?: User })[]>([])
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
  const showEditModal = ref(false)
  const submitting = ref(false)
  const studentForm = reactive({
    id: null as number | null,
    userId: null as number | null,
    studentNo: '',
    realName: '',
    gender: '',
    birthDate: null as number | null,
    grade: null as number | null,
    major: '',
    className: '',
    admissionDate: null as number | null,
    email: '',
    phone: '',
    status: 'active'
  })
  
  // 选课列表数据
  const showCoursesModal = ref(false)
  const currentStudent = ref<Student & { user?: User } | null>(null)
  const studentCourses = ref<CourseSelection[]>([])
  const loadingCourses = ref(false)
  
  // 下拉选项
  const genderOptions = [
    { label: '男', value: '男' },
    { label: '女', value: '女' }
  ]
  
  const statusOptions = [
    { label: '正常', value: 'active' },
    { label: '锁定', value: 'locked' },
    { label: '注销', value: 'deleted' }
  ]
  
  // 表单验证规则
  const rules = {
    studentNo: {
      required: true,
      message: '请输入学号',
      trigger: 'blur'
    },
    realName: {
      required: true,
      message: '请输入姓名',
      trigger: 'blur'
    },
    gender: {
      required: true,
      message: '请选择性别',
      trigger: 'blur'
    },
    className: {
      required: true,
      message: '请输入班级',
      trigger: 'blur'
    },
    major: {
      required: true,
      message: '请输入专业',
      trigger: 'blur'
    },
    status: {
      required: true,
      message: '请选择状态',
      trigger: 'blur'
    }
  }
  
  // 渲染账号状态标签
  const renderStatusTag = (status: string) => {
    const statusMap: Record<string, { type: string, text: string }> = {
      active: { type: 'success', text: '正常' },
      locked: { type: 'warning', text: '锁定' },
      deleted: { type: 'error', text: '注销' }
    }
    
    const tagInfo = statusMap[status] || { type: 'default', text: status }
    
    return h(NTag, { type: tagInfo.type as any }, { default: () => tagInfo.text })
  }
  
  // 创建操作下拉菜单
  const createActionDropdown = (row: Student & { user?: User }) => {
    const options: DropdownOption[] = [
      {
        label: '编辑信息',
        key: 'edit',
        icon: () => h(NIcon, null, { default: () => h(PencilOutline) })
      },
      {
        label: '查看选课',
        key: 'courses',
        icon: () => h(NIcon, null, { default: () => h(BookOutline) })
      }
    ]
    
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
  
  // 表格列定义
  const columns: DataTableColumns<Student & { user?: User }> = [
    { title: '学号', key: 'studentNo', width: 120 },
    { 
      title: '姓名', 
      key: 'realName',
      width: 120,
      render(row) {
        return row.realName || row.user?.realName || '未知'
      }
    },
    { title: '性别', key: 'gender', width: 80 },
    { title: '年级', key: 'grade', width: 100 },
    { title: '专业', key: 'major', width: 150 },
    { title: '班级', key: 'className', width: 150 },
    { 
      title: '邮箱', 
      key: 'email',
      width: 180,
      render(row) {
        return row.user?.email || '未知'
      }
    },
    { 
      title: '账号状态', 
      key: 'status',
      width: 100,
      render(row) {
        return renderStatusTag(row.user?.status || 'unknown')
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
  
  // 课程表格列定义
  const courseColumns: DataTableColumns<CourseSelection> = [
    { 
      title: '课程代码', 
      key: 'courseCode',
      width: 120,
      render(row) {
        return row.course?.courseCode || '未知'
      }
    },
    { 
      title: '课程名称', 
      key: 'courseName',
      width: 200,
      render(row) {
        return row.course?.courseName || '未知'
      }
    },
    { 
      title: '教师', 
      key: 'teacherName',
      width: 120,
      render(row) {
        return row.course?.teacherName || '未知'
      }
    },
    { 
      title: '学分', 
      key: 'credit',
      width: 80,
      render(row) {
        return row.course?.credits || '未知'
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
      width: 120,
      render(row) {
        return h(
          NPopconfirm,
          {
            onPositiveClick: () => handleRemoveCourse(row)
          },
          {
            default: () => '确定将该学生退出此课程吗？',
            trigger: () =>
              h(
                NButton,
                {
                  size: 'small',
                  type: 'error'
                },
                { default: () => '退课' }
              )
          }
        )
      }
    }
  ]
  
  // 初始化数据
  onMounted(() => {
    fetchStudents()
  })
  
  // 获取学生列表
  async function fetchStudents() {
    loading.value = true
    try {
      // 使用封装的API函数
      const data = await student.getStudents({
        page: pagination.page,
        size: pagination.pageSize,
        keyword: searchKeyword.value
      })
      
      students.value = data.records
      pagination.pageCount = data.pages
      
    } catch (error) {
      console.error('获取学生列表失败', error)
      message.error('获取学生列表失败')
    } finally {
      loading.value = false
    }
  }
  
  // 页码变化处理
  function handlePageChange(page: number) {
    pagination.page = page
    fetchStudents()
  }
  
  // 每页数量变化处理
  function handlePageSizeChange(pageSize: number) {
    pagination.pageSize = pageSize
    pagination.page = 1
    fetchStudents()
  }
  
  // 搜索处理
  function handleSearch() {
    pagination.page = 1
    fetchStudents()
  }
  
  // 表单重置
  function resetForm() {
    Object.assign(studentForm, {
      id: null,
      userId: null,
      studentNo: '',
      realName: '',
      gender: '',
      birthDate: null,
      grade: null,
      major: '',
      className: '',
      admissionDate: null,
      email: '',
      phone: '',
      status: 'active'
    })
  }
  
  // 操作处理
  async function handleAction(key: string, row: Student & { user?: User }) {
    switch (key) {
      case 'edit':
        handleEdit(row)
        break
      case 'courses':
        showStudentCourses(row)
        break
    }
  }
  
  // 编辑学生信息
  function handleEdit(row: Student & { user?: User }) {
    Object.assign(studentForm, {
      id: row.id,
      userId: row.userId,
      studentNo: row.studentNo,
      realName: row.user?.realName || '',
      gender: row.gender || '',
      birthDate: row.birthDate ? new Date(row.birthDate).getTime() : null,
      grade: row.grade,
      major: row.major || '',
      className: row.className || '',
      admissionDate: row.admissionDate ? new Date(row.admissionDate).getTime() : null,
      email: row.user?.email || '',
      phone: row.user?.phone || '',
      status: row.user?.status || 'active'
    })
    
    showEditModal.value = true
  }
  
  // 保存学生信息
  async function handleSaveStudent() {
    if (!formRef.value) return
    
    try {
      await formRef.value.validate()
      submitting.value = true
      
      // 准备学生数据
      const studentData = {
        id: studentForm.id,
        studentNo: studentForm.studentNo,
        gender: studentForm.gender,
        birthDate: studentForm.birthDate ? new Date(studentForm.birthDate).toISOString().split('T')[0] : null,
        grade: studentForm.grade,
        major: studentForm.major,
        className: studentForm.className,
        admissionDate: studentForm.admissionDate ? new Date(studentForm.admissionDate).toISOString().split('T')[0] : null
      }
      
      // 准备用户数据
      const userData = {
        id: studentForm.userId,
        realName: studentForm.realName,
        email: studentForm.email,
        phone: studentForm.phone,
        status: studentForm.status
      }
      
      // 使用封装的API函数
      await student.updateStudent(studentForm.id, studentData)
      await student.updateUser(studentForm.userId, userData)
      
      message.success('学生信息更新成功')
      showEditModal.value = false
      fetchStudents()
      
    } catch (error) {
      console.error('保存学生信息失败', error)
      message.error('保存学生信息失败')
    } finally {
      submitting.value = false
    }
  }
  
  // 显示学生选课列表
  async function showStudentCourses(student: Student & { user?: User }) {
    currentStudent.value = student
    showCoursesModal.value = true
    loadingCourses.value = true
    
    try {
      studentCourses.value = await selection.getStudentCourses(student.id)
    } catch (error) {
      console.error('获取学生选课列表失败', error)
      message.error('获取选课列表失败')
    } finally {
      loadingCourses.value = false
    }
  }
  
  // 退课处理
  async function handleRemoveCourse(row: CourseSelection) {
    try {
      await selection.dropCourse(row.studentId, row.courseId)
      message.success('退课成功')
      
      // 刷新选课列表
      if (currentStudent.value) {
        studentCourses.value = await selection.getStudentCourses(currentStudent.value.id)
      }
    } catch (error) {
      console.error('退课失败', error)
      message.error('操作失败')
    }
  }
  </script>
  
  <style scoped lang="scss">
  .student-management-container {
    padding: 16px;
    
    .student-card {
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
    
    .student-info {
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