import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/student',
    component: () => import('../layouts/StudentLayout.vue'),
    meta: { requiresAuth: true, role: 'student' },
    children: [
      {
        path: '',
        name: 'StudentLayout',
        redirect: { name: 'StudentDashboard' }
      },
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('../views/student/Dashboard.vue')
      },
      {
        path: 'courses',
        name: 'CourseSelection',
        component: () => import('../views/student/CourseSelection.vue')
      },
      {
        path: 'my-courses',
        name: 'MyCourses',
        component: () => import('../views/student/MyCourses.vue')
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('../views/student/Profile.vue')
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      {
        path: '',
        name: 'AdminLayout',
        redirect: { name: 'AdminDashboard' }
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue')
      },
      {
        path: 'courses',
        name: 'CourseManagement',
        component: () => import('../views/admin/CourseManagement.vue')
      },
      {
        path: 'students',
        name: 'StudentManagement',
        component: () => import('../views/admin/StudentManagement.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiredRole = to.meta.role as string | undefined

  // 如果页面刷新，尝试从token恢复用户信息
  if (userStore.token && (!userStore.userId || !userStore.role)) {
    try {
      await userStore.fetchUserInfo() // 确保用户信息已加载
      console.log('恢复的用户角色:', userStore.role) // 调试信息
    } catch (error) {
      console.error('恢复用户信息失败:', error)
      userStore.logout() // 如果恢复失败，注销用户
      next('/login')
      return
    }
  }

  // 不需要身份验证的路由直接通过
  if (!requiresAuth) {
    next()
    return
  }

  // 未登录但需要身份验证，重定向到登录页
  if (!userStore.isLoggedIn) {
    if (to.path !== '/login') {
      next('/login')
    } else {
      next()
    }
    return
  }

  // 已登录但角色不匹配
  if (requiredRole && userStore.role !== requiredRole) {
    console.log('角色不匹配:', userStore.role, requiredRole) // 调试信息
    
    // 防止无限重定向循环
    const adminPath = '/admin/dashboard'
    const studentPath = '/student/dashboard'
    
    if (userStore.role === 'admin') {
      if (to.path.startsWith('/student')) {
        next(adminPath)
        return
      }
    } else if (userStore.role === 'student') {
      if (to.path.startsWith('/admin')) {
        next(studentPath)
        return
      }
    }
  }

  // 所有条件都通过
  next()
})

export default router