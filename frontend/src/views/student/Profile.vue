<template>
  <div class="profile-container">
    <n-card title="个人资料" class="profile-card">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <n-spin size="large" />
        <p>加载中...</p>
      </div>

      <!-- 展示模式 -->
      <div v-else class="profile-info">
        <div class="profile-header">
          <n-avatar
            :size="80"
            round
            color="#2080f0"
          >{{ userStore.username.charAt(0).toUpperCase() }}</n-avatar>
          <div class="profile-basic">
            <h2>{{ userInfo.realName || userStore.username }}</h2>
            <p class="user-role">{{ roleText }}</p>
          </div>
        </div>
        
        <n-divider />
        
        <div class="info-section">
          <h3 class="section-title">账号信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">用户名</span>
              <span class="value">{{ userStore.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱</span>
              <span class="value">{{ userInfo.email || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">电话</span>
              <span class="value">{{ userInfo.phone || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">账号状态</span>
              <span class="value">
                <n-tag type="success" v-if="userInfo.status === 'active'">正常</n-tag>
                <n-tag type="warning" v-else-if="userInfo.status === 'locked'">已锁定</n-tag>
                <n-tag type="error" v-else-if="userInfo.status === 'deleted'">已注销</n-tag>
                <n-tag v-else>{{ userInfo.status }}</n-tag>
              </span>
            </div>
          </div>
        </div>
        
        <div class="info-section">
          <h3 class="section-title">学生信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">学号</span>
              <span class="value">{{ studentInfo.studentNo || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">性别</span>
              <span class="value">{{ studentInfo.gender || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">出生日期</span>
              <span class="value">{{ formatDate(studentInfo.birthDate) }}</span>
            </div>
            <div class="info-item">
              <span class="label">专业</span>
              <span class="value">{{ studentInfo.major || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">班级</span>
              <span class="value">{{ studentInfo.className || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">年级</span>
              <span class="value">{{ studentInfo.grade || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">入学日期</span>
              <span class="value">{{ formatDate(studentInfo.admissionDate) }}</span>
            </div>
          </div>
        </div>
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { auth, student as studentApi } from '@/api'
import type { User, Student } from '@/types/api'
import { 
  NCard, 
  NAvatar, 
  NDivider, 
  NIcon, 
  NSpin,
  NTag,
  useMessage
} from 'naive-ui'

const userStore = useUserStore()
const message = useMessage()

// 状态管理
const loading = ref(true)

// 用户数据
const userInfo = ref<User>({
  id: 0,
  username: '',
  realName: '',
  email: '',
  phone: '',
  role: 'student',
  status: 'active'
})

// 学生数据
const studentInfo = ref<Student>({
  id: 0,
  userId: 0,
  studentNo: '',
  gender: '',
  birthDate: '',
  grade: 0,
  major: '',
  className: '',
  admissionDate: ''
})

// 计算角色文本
const roleText = computed(() => {
  switch (userStore.role) {
    case 'admin':
      return '管理员';
    case 'student':
      return '学生';
    default:
      return userStore.role;
  }
})

// 修改获取用户和学生信息的方法
const fetchUserInfo = async () => {
  loading.value = true
  try {
    // 如果是学生，通过user/{userId}接口获取
    if (userStore.userId) {
      // 1. 获取学生基本信息
      const data = await studentApi.getStudentByUserId(userStore.userId);
      studentInfo.value = data;
      
      // 2. 获取用户详细信息
      const userData = await studentApi.getUserBasicInfo(userStore.userId);
      userInfo.value = userData;
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    message.error('获取用户信息失败');
  } finally {
    loading.value = false;
  }
}

// 格式化日期
const formatDate = (dateStr: string | undefined) => {
  if (!dateStr) return '未设置'
  
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())}`
}

// 补零
const padZero = (num: number) => {
  return num < 10 ? `0${num}` : num
}

// 初始化
onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped lang="scss">
.profile-container {
  padding: 16px;
  
  .profile-card {
    max-width: 800px;
    margin: 0 auto;
  }
  
  .loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 0;
    
    p {
      margin-top: 12px;
      color: #8a8a8a;
    }
  }
  
  .profile-info {
    .profile-header {
      display: flex;
      align-items: center;
      margin-bottom: 24px;
      
      .profile-basic {
        margin-left: 24px;
        
        h2 {
          margin: 0 0 8px 0;
          font-size: 24px;
        }
        
        .user-role {
          margin: 0;
          color: #666;
        }
      }
    }
    
    .info-section {
      margin-bottom: 24px;
      
      .section-title {
        margin: 16px 0;
        font-size: 18px;
        color: #333;
      }
      
      .info-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
        gap: 16px;
        
        .info-item {
          display: flex;
          flex-direction: column;
          
          .label {
            font-size: 14px;
            color: #666;
            margin-bottom: 4px;
          }
          
          .value {
            font-size: 16px;
          }
        }
      }
    }
  }
}
</style>