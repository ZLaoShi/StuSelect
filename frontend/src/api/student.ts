import request from './request'
import type { PaginationResponse, Student, User } from '@/types/api'

// 获取学生列表
export function getStudents(params: { page?: number, size?: number, keyword?: string } = {}): Promise<PaginationResponse<Student & { user?: User }>> {
  return request({
    url: '/students',
    method: 'get',
    params
  })
}

// 获取学生详情
export function getStudentDetail(id: number): Promise<Student & { user?: User }> {
  return request({
    url: `/students/${id}`,
    method: 'get'
  })
}

// 更新学生信息
export function updateStudent(id: number, data: any): Promise<boolean> {
  return request({
    url: `/students/${id}`,
    method: 'put',
    data
  })
}

// 更新用户信息
export function updateUser(id: number, data: any): Promise<boolean> {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  })
}

// 通过用户ID获取学生信息
export function getStudentByUserId(userId: number): Promise<Student> {
  return request({
    url: `/students/user/${userId}`,
    method: 'get'
  });
}

// 获取用户基本信息
export function getUserBasicInfo(userId: number): Promise<User> {
  return request({
    url: `/users/${userId}`,
    method: 'get'
  });
}

