<template>
    <div class="not-found-container">
      <n-result
        status="404"
        title="404 页面不存在"
        description="您访问的页面不存在，请检查URL是否正确"
      >
        <template #footer>
          <n-button @click="goHome">返回首页</n-button>
        </template>
      </n-result>
    </div>
  </template>
  
  <script setup lang="ts">
  import { useRouter } from 'vue-router'
  import { NResult, NButton } from 'naive-ui'
  
  const router = useRouter()
  
  const goHome = () => {
    router.push('/')
  }
  </script>
  
  <style scoped>
  .not-found-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
  }
  </style>