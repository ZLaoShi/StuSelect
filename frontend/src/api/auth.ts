import request from './request'
import type { LoginResponse, User, Student } from '@/types/api'

// 用户登录
export function login(username: string, password: string): Promise<LoginResponse> {
  return request({
    url: '/auth/login',
    method: 'post',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    data: new URLSearchParams({
      username,
      password
    })
  })
}

// 注册
export function register(data: {
  username: string;
  password: string;
  realName: string;
  email: string;
  phone: string;
  role: string;
  student?: {
    studentNo: string;
    gender: string;
    birthDate: string | null;
    grade: number;
    major: string;
    className: string;
    admissionDate: string | null;
  }
}): Promise<any> {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 获取学生信息
export function getUserInfo(userId: number): Promise<Student> {
  return request({
    url: `/students/user/${userId}`,
    method: 'get'
  })
}