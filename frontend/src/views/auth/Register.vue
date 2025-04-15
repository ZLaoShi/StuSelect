<template>
    <div class="auth-container">
      <div class="auth-form-wrapper register-form">
        <h2 class="text-center">学生选课系统</h2>
        <div class="text-center m-b-10">用户注册</div>
        
        <n-steps :current="currentStep" :status="stepStatus">
          <n-step title="账号信息" description="创建基本账号" />
          <n-step title="学生信息" description="填写学生详细信息" />
          <n-step title="注册完成" description="注册成功" />
        </n-steps>
        
        <div class="step-content m-t-10">
          <!-- 第一步：账号信息 -->
          <div v-if="currentStep === 0">
            <n-form
              ref="userFormRef"
              :model="userForm"
              :rules="userRules"
              label-placement="left"
              label-width="80"
            >
              <n-form-item label="用户名" path="username">
                <n-input v-model:value="userForm.username" placeholder="请输入用户名" />
              </n-form-item>
              
              <n-form-item label="密码" path="password">
                <n-input
                  v-model:value="userForm.password"
                  type="password"
                  placeholder="请输入密码"
                  show-password-on="click"
                />
              </n-form-item>
              
              <n-form-item label="确认密码" path="confirmPassword">
                <n-input
                  v-model:value="userForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  show-password-on="click"
                />
              </n-form-item>
              
              <n-form-item label="真实姓名" path="realName">
                <n-input v-model:value="userForm.realName" placeholder="请输入真实姓名" />
              </n-form-item>
              
              <n-form-item label="电子邮箱" path="email">
                <n-input v-model:value="userForm.email" placeholder="请输入电子邮箱" />
              </n-form-item>
              
              <n-form-item label="手机号码" path="phone">
                <n-input v-model:value="userForm.phone" placeholder="请输入手机号码" />
              </n-form-item>
            </n-form>
          </div>
          
          <!-- 第二步：学生信息 -->
          <div v-if="currentStep === 1">
            <n-form
              ref="studentFormRef"
              :model="studentForm"
              :rules="studentRules"
              label-placement="left"
              label-width="80"
            >
              <n-form-item label="学号" path="studentNo">
                <n-input v-model:value="studentForm.studentNo" placeholder="请输入学号" />
              </n-form-item>
              
              <n-form-item label="性别" path="gender">
                <n-radio-group v-model:value="studentForm.gender">
                  <n-radio value="男">男</n-radio>
                  <n-radio value="女">女</n-radio>
                </n-radio-group>
              </n-form-item>
              
              <n-form-item label="出生日期" path="birthDate">
                <n-date-picker v-model:value="studentForm.birthDate" type="date" />
              </n-form-item>
              
              <n-form-item label="年级" path="grade">
                <n-input-number v-model:value="studentForm.grade" placeholder="请输入年级" />
              </n-form-item>
              
              <n-form-item label="专业" path="major">
                <n-input v-model:value="studentForm.major" placeholder="请输入专业" />
              </n-form-item>
              
              <n-form-item label="班级" path="className">
                <n-input v-model:value="studentForm.className" placeholder="请输入班级" />
              </n-form-item>
              
              <n-form-item label="入学日期" path="admissionDate">
                <n-date-picker v-model:value="studentForm.admissionDate" type="date" />
              </n-form-item>
            </n-form>
          </div>
          
          <!-- 第三步：注册成功 -->
          <div v-if="currentStep === 2">
            <n-result
              status="success"
              title="注册成功"
              description="您已成功注册学生选课系统，现在可以登录使用。"
            >
              <template #footer>
                <n-button type="primary" @click="goToLogin">去登录</n-button>
              </template>
            </n-result>
          </div>
        </div>
        
        <div class="form-actions">
          <n-button v-if="currentStep > 0 && currentStep < 2" @click="prevStep">上一步</n-button>
          <n-button
            v-if="currentStep < 2"
            type="primary"
            @click="nextStep"
            :loading="loading"
          >
            {{ currentStep === 1 ? '提交' : '下一步' }}
          </n-button>
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, reactive } from 'vue'
  import { useRouter } from 'vue-router'
  import { useMessage } from 'naive-ui'
  import { register, createStudentInfo } from '../../api/auth'
  import { 
    NForm, 
    NFormItem, 
    NInput, 
    NButton, 
    NSteps, 
    NStep, 
    NRadioGroup, 
    NRadio, 
    NDatePicker, 
    NInputNumber, 
    NResult 
  } from 'naive-ui'
  
  const router = useRouter()
  const message = useMessage()
  
  const currentStep = ref(0)
  const stepStatus = ref('process')
  const loading = ref(false)
  
  const userFormRef = ref(null)
  const studentFormRef = ref(null)
  
  const userForm = reactive({
    username: '',
    password: '',
    confirmPassword: '',
    realName: '',
    email: '',
    phone: '',
  })
  
  const studentForm = reactive({
    studentNo: '',
    gender: '男',
    birthDate: null,
    grade: 2025,
    major: '',
    className: '',
    admissionDate: null,
  })
  
  const userRules = {
    username: {
      required: true,
      message: '请输入用户名',
      trigger: 'blur',
    },
    password: {
      required: true,
      message: '请输入密码',
      trigger: 'blur',
      validator: (_rule: any, value: string) => {
        if (value.length < 6) {
          return new Error('密码长度不能少于6位')
        }
        return true
      }
    },
    confirmPassword: {
      required: true,
      message: '请再次输入密码',
      trigger: 'blur',
      validator: (_rule: any, value: string) => {
        if (value !== userForm.password) {
          return new Error('两次输入的密码不一致')
        }
        return true
      }
    },
    realName: {
      required: true,
      message: '请输入真实姓名',
      trigger: 'blur',
    },
    email: {
      required: true,
      message: '请输入电子邮箱',
      trigger: 'blur',
      validator: (_rule: any, value: string) => {
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
        if (!emailRegex.test(value)) {
          return new Error('请输入有效的电子邮箱')
        }
        return true
      }
    },
    phone: {
      required: true,
      message: '请输入手机号码',
      trigger: 'blur',
      validator: (_rule: any, value: string) => {
        const phoneRegex = /^1[3-9]\d{9}$/
        if (!phoneRegex.test(value)) {
          return new Error('请输入有效的手机号码')
        }
        return true
      }
    },
  }
  
  const studentRules = {
    studentNo: {
      required: true,
      message: '请输入学号',
      trigger: 'blur',
    },
    gender: {
      required: true,
      message: '请选择性别',
      trigger: 'change',
    },
    birthDate: {
      type: 'number',
      required: true,
      message: '请选择出生日期',
      trigger: 'change',
    },
    grade: {
      type: 'number',
      required: true,
      message: '请输入年级',
      trigger: 'blur',
    },
    major: {
      required: true,
      message: '请输入专业',
      trigger: 'blur',
    },
    className: {
      required: true,
      message: '请输入班级',
      trigger: 'blur',
    },
    admissionDate: {
      type: 'number',
      required: true,
      message: '请选择入学日期',
      trigger: 'change',
    },
  }
  
  const nextStep = async () => {
    if (currentStep.value === 0) {
      // 验证用户表单
      try {
        await userFormRef.value?.validate()
        currentStep.value++ // 只是进入下一步，不注册
      } catch (error) {
        console.error('表单验证失败:', error)
      }
    } else if (currentStep.value === 1) {
      // 验证学生表单并提交注册
      try {
        await studentFormRef.value?.validate()
        await handleRegister() // 两步表单都验证通过后才注册
      } catch (error) {
        console.error('表单验证失败:', error)
      }
    }
  }
  
  const prevStep = () => {
    if (currentStep.value > 0) {
      currentStep.value--
    }
  }
  
  const handleRegister = async () => {
    try {
      loading.value = true
      
      // 注册用户及学生信息（一次性处理）
      const userData = {
        username: userForm.username,
        password: userForm.password,
        realName: userForm.realName,
        email: userForm.email,
        phone: userForm.phone,
        role: 'student',
        // 添加学生信息
        student: {
          studentNo: studentForm.studentNo,
          gender: studentForm.gender,
          birthDate: studentForm.birthDate ? new Date(studentForm.birthDate).toISOString().split('T')[0] : null,
          grade: studentForm.grade,
          major: studentForm.major,
          className: studentForm.className,
          admissionDate: studentForm.admissionDate ? new Date(studentForm.admissionDate).toISOString().split('T')[0] : null,
        }
      }
      
      // 调用一个统一的注册接口，同时创建用户和学生信息
      await register(userData)
      
      // 注册成功
      message.success('注册成功')
      currentStep.value++
    } catch (error: any) {
      console.error('注册失败:', error)
      message.error(error?.error || '注册失败，请稍后重试')
    } finally {
      loading.value = false
    }
  }
  
  const goToLogin = () => {
    router.push('/login')
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
  
  .register-form {
    width: 600px;
    
    .step-content {
      margin-top: 20px;
      padding: 20px 0;
    }
  }
  
  .text-center {
    text-align: center;
  }
  
  .m-b-10 {
    margin-bottom: 10px;
  }
  
  .m-t-10 {
    margin-top: 10px;
  }
  
  .form-actions {
    margin-top: 24px;
    display: flex;
    justify-content: space-between;
  }
  </style>