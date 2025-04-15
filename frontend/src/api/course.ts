import request from './request'
import type { Course, PaginationResponse } from '@/types/api'

// 获取课程列表
export function getCourses(params: { current?: number, size?: number, keyword?: string } = {}): Promise<PaginationResponse<Course>> {
  return request({
    url: '/courses',
    method: 'get',
    params
  })
}

// 获取课程详情
export function getCourseDetail(id: number): Promise<Course> {
  return request({
    url: `/courses/${id}`,
    method: 'get'
  })
}

// 创建课程（管理员功能）
export function createCourse(courseData: any): Promise<Course> {
  return request({
    url: '/courses',
    method: 'post',
    data: courseData
  })
}

// 更新课程信息（管理员功能）
export function updateCourse(id: number, courseData: any): Promise<boolean> {
  return request({
    url: `/courses/${id}`,
    method: 'put',
    data: courseData
  })
}

// 更新课程状态（管理员功能）
export function updateCourseStatus(id: number, status: string): Promise<boolean> {
  return request({
    url: `/courses/${id}/status`,
    method: 'put',
    params: { status }
  })
}