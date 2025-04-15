<template>
    <div class="home-container">
      <header class="header">
        <div class="header-content">
          <div class="logo">
            <img src="@/assets/logo.svg" alt="Logo" v-if="logoLoaded" />
            <div class="logo-text">学生选课系统</div>
          </div>
          <div class="header-nav">
            <n-button text @click="handleAbout">关于系统</n-button>
            <n-button text @click="handleHelp">使用帮助</n-button>
            <n-button 
              type="primary" 
              @click="gotoLogin" 
              v-if="!userStore.isLoggedIn"
              class="login-btn"
            >
              登录
            </n-button>
            <n-dropdown 
              v-if="userStore.isLoggedIn" 
              trigger="click" 
              :options="userOptions" 
              @select="handleUserAction"
            >
              <div class="user-profile">
                <n-avatar round size="small">{{ userStore.username.charAt(0) }}</n-avatar>
                <span class="username">{{ userStore.username }}</span>
              </div>
            </n-dropdown>
          </div>
        </div>
      </header>
      
      <main class="main-content">
        <div class="hero-section">
          <div class="hero-content">
            <h1 class="hero-title">高效便捷的选课体验</h1>
            <p class="hero-description">
              轻松选择课程，管理学习计划，追踪学习进度，一站式解决您的选课需求
            </p>
            <div class="hero-actions">
              <n-button type="primary" size="large" @click="gotoSelection" class="action-btn">
                开始选课
              </n-button>
              <n-button size="large" @click="gotoRegister" class="action-btn" v-if="!userStore.isLoggedIn">
                注册账号
              </n-button>
            </div>
          </div>
          <div class="hero-image">
            <img src="@/assets/home-illustration.svg" alt="选课插图" v-if="illustrationLoaded" />
          </div>
        </div>
        
        <div class="features-section">
          <h2 class="section-title">核心功能</h2>
          <div class="features-grid">
            <div class="feature-card">
              <n-icon size="48" color="#2080f0">
                <BookOutline />
              </n-icon>
              <h3>丰富的课程资源</h3>
              <p>提供多样化的课程选择，满足不同学习需求</p>
            </div>
            <div class="feature-card">
              <n-icon size="48" color="#2080f0">
                <TimeOutline />
              </n-icon>
              <h3>实时课程状态</h3>
              <p>课程余量实时更新，选课状态即时反馈</p>
            </div>
            <div class="feature-card">
              <n-icon size="48" color="#2080f0">
                <PersonOutline />
              </n-icon>
              <h3>个人学习管理</h3>
              <p>轻松管理已选课程，查看学习进度和成绩</p>
            </div>
            <div class="feature-card">
              <n-icon size="48" color="#2080f0">
                <NotificationsOutline />
              </n-icon>
              <h3>选课提醒</h3>
              <p>重要选课时间节点提醒，避免错过选课时间</p>
            </div>
          </div>
        </div>
      </main>
      
      <footer class="footer">
        <div class="footer-content">
          <div class="footer-info">
            <p>© {{ currentYear }} 学生选课系统 - 郑富燕</p>
          </div>
        </div>
      </footer>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, computed, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { useUserStore } from '@/stores/user'
  import { 
    BookOutline, 
    TimeOutline, 
    PersonOutline, 
    NotificationsOutline 
  } from '@vicons/ionicons5'
  import { NButton, NAvatar, NDropdown, NIcon } from 'naive-ui'
  
  const router = useRouter()
  const userStore = useUserStore()
  
  const currentYear = new Date().getFullYear()
  const logoLoaded = ref(true) // 默认为true，实际使用时可能需要检查图片加载
  const illustrationLoaded = ref(true) // 同上
  
  // 用户下拉菜单选项
  const userOptions = computed(() => {
    const options = [
      {
        label: userStore.isAdmin ? '管理控制台' : '我的课程',
        key: 'dashboard'
      },
      {
        label: '退出登录',
        key: 'logout'
      }
    ]
    return options
  })
  
  // 处理用户菜单操作
  const handleUserAction = (key: string) => {
    if (key === 'logout') {
      userStore.logout()
    } else if (key === 'dashboard') {
      if (userStore.isAdmin) {
        router.push('/admin/dashboard')
      } else {
        router.push('/student/dashboard')
      }
    }
  }
  
  // 导航处理函数
  const gotoLogin = () => {
    router.push('/login')
  }
  
  const gotoRegister = () => {
    router.push('/register')
  }
  
  const gotoSelection = () => {
    if (userStore.isLoggedIn) {
      if (userStore.isStudent) {
        router.push('/student/courses')
      } else {
        router.push('/admin/courses')
      }
    } else {
      router.push('/login')
    }
  }
  
  const handleAbout = () => {
    // 可以用弹窗或者导航到关于页面
    window.$message?.info('关于系统功能即将上线')
  }
  
  const handleHelp = () => {
    // 可以用弹窗或者导航到帮助页面
    window.$message?.info('使用帮助功能即将上线')
  }
  
  // 全局消息API
  declare global {
    interface Window {
      $message?: any
    }
  }
  
  onMounted(() => {
    // 如有必要，可以在这里检查图片是否加载成功
  })
  </script>
  
  <style scoped lang="scss">
  .home-container {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
  }
  
  .header {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    height: 64px;
    background-color: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(8px);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    z-index: 100;
    
    &-content {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 24px;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    
    &-nav {
      display: flex;
      align-items: center;
      gap: 20px;
    }
  }
  
  .logo {
    display: flex;
    align-items: center;
    gap: 10px;
    
    img {
      height: 36px;
    }
    
    &-text {
      font-size: 18px;
      font-weight: 600;
      color: #333;
    }
  }
  
  .user-profile {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: 4px;
    
    &:hover {
      background-color: rgba(0, 0, 0, 0.05);
    }
    
    .username {
      font-size: 14px;
    }
  }
  
  .main-content {
    padding-top: 64px;
    flex: 1;
  }
  
  .hero-section {
    max-width: 1200px;
    margin: 0 auto;
    padding: 80px 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 60px;
    
    @media (max-width: 992px) {
      flex-direction: column;
      padding: 40px 24px;
      text-align: center;
    }
  }
  
  .hero-content {
    flex: 1;
    
    .hero-title {
      font-size: 48px;
      font-weight: 700;
      margin-bottom: 24px;
      color: #333;
      line-height: 1.2;
      
      @media (max-width: 768px) {
        font-size: 36px;
      }
    }
    
    .hero-description {
      font-size: 18px;
      color: #666;
      margin-bottom: 40px;
      line-height: 1.6;
    }
    
    .hero-actions {
      display: flex;
      gap: 16px;
      
      @media (max-width: 992px) {
        justify-content: center;
      }
      
      @media (max-width: 576px) {
        flex-direction: column;
      }
      
      .action-btn {
        min-width: 140px;
      }
    }
  }
  
  .hero-image {
    flex: 1;
    max-width: 500px;
    
    img {
      width: 100%;
      height: auto;
    }
  }
  
  .features-section {
    padding: 80px 24px;
    background-color: #f5f7fa;
    
    .section-title {
      text-align: center;
      font-size: 32px;
      font-weight: 600;
      margin-bottom: 60px;
      color: #333;
    }
  }
  
  .features-grid {
    max-width: 1200px;
    margin: 0 auto;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 40px;
    
    @media (max-width: 992px) {
      grid-template-columns: repeat(2, 1fr);
    }
    
    @media (max-width: 576px) {
      grid-template-columns: 1fr;
    }
  }
  
  .feature-card {
    background-color: #fff;
    padding: 30px;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    text-align: center;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    }
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      margin: 20px 0 12px;
      color: #333;
    }
    
    p {
      font-size: 14px;
      color: #666;
      line-height: 1.5;
    }
  }
  
  .footer {
    background-color: #fff;
    border-top: 1px solid #eee;
    
    &-content {
      max-width: 1200px;
      margin: 0 auto;
      padding: 30px 24px;
      display: flex;
      justify-content: center;
    }
    
    &-info {
      font-size: 14px;
      color: #999;
      text-align: center;
    }
  }
  
  .login-btn {
    margin-left: 10px;
  }
  </style>