// API响应类型定义

// 登录响应
export interface LoginResponse {
  token: string;
}

// 用户信息
export interface User {
  id: number;
  username: string;
  realName?: string;
  email?: string;
  phone?: string;
  role: string;
  status: string;
  lastLoginTime?: string;
  createdAt?: string;
  updatedAt?: string;
}

// 学生信息
export interface Student {
  id: number;
  userId: number;
  studentNo: string;
  gender?: string;
  birthDate?: string;
  grade?: number;
  major?: string;
  className?: string;
  admissionDate?: string;
  createdAt?: string;
  updatedAt?: string;
}

// 课程信息
export interface Course {
  id: number;
  courseCode: string;
  courseName: string;
  description?: string;
  teacherName: string;
  credits: number;
  capacity: number;
  selectedCount: number;
  startDate?: string;
  endDate?: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
  selectable?: boolean;
}

// 选课信息
export interface CourseSelection {
  id: number;
  studentId: number;
  courseId: number;
  selectTime: string;
  score?: number;
  scoreType?: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
  // 可能在查询时返回的嵌套数据
  student?: Student;
  course?: Course;
}

// 分页响应
export interface PaginationResponse<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

// 学生仪表盘数据
export interface StudentDashboardDTO {
  selectedCourseCount: number;
  availableCourseCount: number;
  courseTypeDistribution: Record<string, number>;
  recentSelectedCourses: Array<{
    id: number;
    courseName: string;
    courseCode: string;
    teacherName: string;
    credit: number;
    selectTime: string;
  }>;
  totalCredit: number;
}

// 管理员仪表盘数据
export interface AdminDashboardDTO {
  totalStudentCount: number;
  totalCourseCount: number;
  activeCourseCount: number;
  totalSelectionCount: number;
  courseTypeDistribution: Record<string, number>;
  topSelectedCourses: Array<{
    id: number;
    courseName: string;
    courseCode: string;
    teacherName: string;
    selectedCount: number;
    capacity: number;
  }>;
  recentSelections: Array<{
    id: number;
    selectTime: string;
    studentNo: string;
    studentName: string;
    courseName: string;
    courseCode: string;
  }>;
}