import request from './request'
import type { AdminDashboardDTO, StudentDashboardDTO } from '@/types/api'

// 获取学生仪表盘数据
export function getStudentDashboardData(studentId: number): Promise<StudentDashboardDTO> {
  return request({
    url: '/dashboard/student',
    method: 'get',
    params: { studentId }
  })
}

// 获取管理员仪表盘数据
export function getAdminDashboardData(): Promise<AdminDashboardDTO> {
  return request({
    url: '/dashboard/admin',
    method: 'get'
  })
}