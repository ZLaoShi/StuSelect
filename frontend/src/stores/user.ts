import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, getUserInfo } from '@/api/auth'
import router from '@/router/router'
import type { Student } from '@/types/api'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userId = ref<number | null>(null)
  const username = ref<string>('')
  const role = ref<string>('')
  const studentId = ref<number | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'admin')
  const isStudent = computed(() => role.value === 'student')

  async function login(username: string, password: string) {
    try {
      const response = await apiLogin(username, password)
      token.value = response.token // 现在这里有正确的类型
      localStorage.setItem('token', response.token)
      await fetchUserInfo()
      
      // 根据角色导航到相应的仪表板
      if (role.value === 'admin') {
        router.push('/admin/dashboard')
      } else {
        router.push('/student/dashboard')
      }
      return true
    } catch (error) {
      console.error('登录失败:', error)
      return false
    }
  }

  async function fetchUserInfo() {
    if (!token.value) return
    
    try {
      // 从token中解析用户信息
      const tokenParts = token.value.split('.')
      if (tokenParts.length !== 3) return
      
      const payload = JSON.parse(atob(tokenParts[1]))
      console.log('Token payload:', payload) // 调试信息
      
      // 设置用户信息
      userId.value = payload.userId || null
      username.value = payload.sub || ''
      role.value = payload.role || ''
      
      // 如果角色不是admin或student，设置为默认值
      if (role.value !== 'admin' && role.value !== 'student') {
        console.error('无效的用户角色:', role.value)
        role.value = ''
      }
      
      // 如果是学生，获取学生信息
      if (role.value === 'student' && userId.value) {
        try {
          const studentInfo = await getUserInfo(userId.value)
          studentId.value = studentInfo.id
        } catch (error) {
          console.error('获取学生信息失败:', error)
        }
      }
    } catch (error) {
      console.error('解析用户信息失败:', error)
    }
  }

  function logout() {
    token.value = ''
    userId.value = null
    username.value = ''
    role.value = ''
    studentId.value = null
    localStorage.removeItem('token')
    router.push('/')
  }

  return {
    token,
    userId,
    username,
    role,
    studentId,
    isLoggedIn,
    isAdmin,
    isStudent,
    login,
    fetchUserInfo,
    logout
  }
})