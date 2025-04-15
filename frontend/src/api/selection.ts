import request from './request'
import type { CourseSelection } from '@/types/api'

// 选课
export function selectCourse(studentId: number, courseId: number): Promise<CourseSelection> {
  return request({
    url: '/selections',
    method: 'post',
    params: { studentId, courseId }
  })
}

// 退课
export function dropCourse(studentId: number, courseId: number): Promise<boolean> {
  return request({
    url: '/selections',
    method: 'delete',
    params: { studentId, courseId }
  })
}

// 获取学生选课列表
export function getStudentCourses(studentId: number): Promise<CourseSelection[]> {
  return request({
    url: `/selections/student/${studentId}`,
    method: 'get'
  })
}

// 获取学生选课记录（包含详细信息）
export function getStudentCoursesWithDetails(studentId: number): Promise<CourseSelection[]> {
  return request({
    url: `/selections/student/${studentId}/details`,
    method: 'get'
  })
}

// 获取课程学生列表（管理员功能）
export function getCourseStudents(courseId: number): Promise<CourseSelection[]> {
  return request({
    url: `/selections/course/${courseId}`,
    method: 'get'
  })
}

// 设置成绩（管理员功能）
export function setScore(selectionId: number, score: number, scoreType?: string): Promise<boolean> {
  return request({
    url: `/selections/${selectionId}/score`,
    method: 'put',
    params: { score, scoreType }
  })
}