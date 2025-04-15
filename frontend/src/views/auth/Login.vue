<template>
    <div class="auth-container">
      <div class="auth-form-wrapper">
        <h2 class="text-center">学生选课系统</h2>
        <div class="text-center m-b-10">用户登录</div>
        
        <n-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-placement="left"
          label-width="80"
          require-mark-placement="right-hanging"
        >
          <n-form-item label="用户名" path="username">
            <n-input v-model:value="formData.username" placeholder="请输入用户名" />
          </n-form-item>
          
          <n-form-item label="密码" path="password">
            <n-input
              v-model:value="formData.password"
              type="password"
              placeholder="请输入密码"
              show-password-on="click"
            />
          </n-form-item>
          
          <div class="form-actions">
            <n-button type="primary" @click="handleLogin" :loading="loading">登录</n-button>
            <n-button class="m-l-10" @click="goToRegister">注册账号</n-button>
          </div>
        </n-form>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, reactive } from 'vue'
  import { useRouter } from 'vue-router'
  import { useMessage } from 'naive-ui'
  import { useUserStore } from '../../stores/user'
  import { NForm, NFormItem, NInput, NButton } from 'naive-ui'
  import type { FormInst } from 'naive-ui'
  
  const userStore = useUserStore()
  const router = useRouter()
  const message = useMessage()
  
  const formRef = ref<FormInst | null>(null)
  const loading = ref(false)
  
  const formData = reactive({
    username: '',
    password: '',
  })
  
  const rules = {
    username: {
      required: true,
      message: '请输入用户名',
      trigger: 'blur',
    },
    password: {
      required: true,
      message: '请输入密码',
      trigger: 'blur',
    },
  }
  
  const handleLogin = async () => {
  if (!formRef.value) return
  
  try {
    loading.value = true
    await formRef.value.validate()
    
    console.log('登录请求参数:', formData.username, formData.password)
    const success = await userStore.login(formData.username, formData.password)
    
    if (success) {
      message.success('登录成功')
    } else {
      message.error('登录失败，请检查用户名和密码')
    }
  } catch (error) {
    console.error('登录失败详细错误:', error)
    message.error('登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
  
  const goToRegister = () => {
    router.push('/register')
  }
  </script>
  
  <style scoped lang="scss">
  .auth-container {
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #f0f2f5;
  }
  
  .auth-form-wrapper {
    width: 400px;
    padding: 40px;
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  }
  
  .text-center {
    text-align: center;
  }
  
  .m-b-10 {
    margin-bottom: 10px;
  }
  
  .m-l-10 {
    margin-left: 10px;
  }
  
  .form-actions {
    margin-top: 24px;
    display: flex;
    justify-content: flex-end;
  }
  </style>