<template>
  <n-layout has-sider position="absolute" style="height: 100vh; width: 100vw;">
    <!-- 侧边栏 -->
    <n-layout-sider
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :collapsed="collapsed"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
      bordered
      position="absolute"
    >
      <div class="layout-sider-header">
        <div v-if="!collapsed" class="logo-full">
          <img class="logo-img" src="@/assets/logo.svg" alt="Logo" v-if="logoLoaded" />
          <div class="logo-text">学生选课系统</div>
        </div>
        <div v-else class="logo-collapsed">
          <img class="logo-img-small" src="@/assets/logo.svg" alt="Logo" v-if="logoLoaded" />
        </div>
      </div>
      
      <n-menu
        :value="activeKey"
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        @update:value="handleMenuUpdate"
      />
    </n-layout-sider>
    
    <!-- 主内容区 -->
    <n-layout :style="{ marginLeft: collapsed ? '64px' : '240px' }">
      <!-- 顶栏 -->
      <n-layout-header bordered position="absolute" style="z-index: 999; width: 100%;">
        <div class="layout-header-wrapper">
          <div class="header-left">
            <n-button text @click="collapsed = !collapsed">
              <template #icon>
                <n-icon size="18">
                  <MenuOutline />
                </n-icon>
              </template>
            </n-button>
          </div>
          
          <div class="header-right">
            <n-space>
              <n-badge :value="3" processing>
                <n-button text>
                  <template #icon>
                    <n-icon size="18">
                      <NotificationsOutline />
                    </n-icon>
                  </template>
                </n-button>
              </n-badge>
              
              <n-dropdown
                trigger="click"
                :options="userOptions"
                @select="handleUserAction"
              >
                <n-button text>
                  <div class="user-profile">
                    <n-avatar round size="small">{{ userStore.username.charAt(0) }}</n-avatar>
                    <span class="username">{{ userStore.username }}</span>
                  </div>
                </n-button>
              </n-dropdown>
            </n-space>
          </div>
        </div>
      </n-layout-header>
      
      <!-- 内容区 -->
      <n-layout-content :style="{ paddingTop: '64px' }">
        <div class="layout-content-wrapper">
          <router-view />
        </div>
      </n-layout-content>
      
      <!-- 底栏 -->
      <n-layout-footer bordered>
        <div class="layout-footer-wrapper">
          <p class="footer-text">© {{ currentYear }} 学生选课系统 - 版权所有</p>
        </div>
      </n-layout-footer>
    </n-layout>
  </n-layout>
</template>
  
<script setup lang="ts">
import { h, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  BookOutline,
  PersonOutline,
  HomeOutline,
  MenuOutline,
  NotificationsOutline,
  LogOutOutline,
  SettingsOutline
} from '@vicons/ionicons5'
import {
  NLayout,
  NLayoutSider,
  NLayoutHeader,
  NLayoutContent,
  NLayoutFooter,
  NMenu,
  NIcon,
  NButton,
  NAvatar,
  NDropdown,
  NSpace,
  NBadge
} from 'naive-ui'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const currentYear = new Date().getFullYear()
const collapsed = ref(false)
const logoLoaded = ref(true) // 默认为true，实际使用时可能需要检查图片加载

// 设置当前激活菜单
const activeKey = computed(() => {
  const path = route.path
  if (path.includes('/dashboard')) return 'dashboard'
  else if (path.includes('/courses')) return 'courses'
  else if (path.includes('/my-courses')) return 'my-courses'
  else if (path.includes('/profile')) return 'profile'
  return 'dashboard'
})

// 渲染图标
function renderIcon(icon: any) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

// 侧边栏菜单选项
const menuOptions = computed(() => [
  {
    label: '首页',
    key: 'dashboard',
    icon: renderIcon(HomeOutline),
    disabled: false
  },
  {
    label: '课程选择',
    key: 'courses',
    icon: renderIcon(BookOutline),
    disabled: false
  },
  {
    label: '我的课程',
    key: 'my-courses',
    icon: renderIcon(BookOutline),
    disabled: false
  },
  {
    label: '个人资料',
    key: 'profile',
    icon: renderIcon(PersonOutline),
    disabled: false
  }
])

// 用户下拉菜单选项
const userOptions = [
  {
    label: '个人设置',
    key: 'settings',
    icon: renderIcon(SettingsOutline)
  },
  {
    type: 'divider',
    key: 'd1'
  },
  {
    label: '退出登录',
    key: 'logout',
    icon: renderIcon(LogOutOutline)
  }
]

// 处理菜单点击
const handleMenuUpdate = (key: string) => {
  router.push(`/student/${key}`)
}

// 处理用户菜单操作
const handleUserAction = (key: string) => {
  if (key === 'logout') {
    userStore.logout()
  } else if (key === 'settings') {
    router.push('/student/profile')
  }
}
</script>
  
<style scoped lang="scss">
.layout-sider-header {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  
  .logo-full {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .logo-img {
      height: 30px;
    }
    
    .logo-text {
      font-size: 16px;
      font-weight: 600;
    }
  }
  
  .logo-collapsed {
    display: flex;
    justify-content: center;
    
    .logo-img-small {
      height: 30px;
    }
  }
}

.layout-header-wrapper {
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  box-sizing: border-box; /* 添加这一行 */
}

.header-right {
  display: flex;
  align-items: center;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0; /* 防止内容超出 */
  
  .username {
    font-size: 14px;
    max-width: 100px; /* 限制用户名最大宽度 */
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis; /* 超出部分显示省略号 */
  }
}

.layout-content-wrapper {
  padding: 24px;
  min-height: calc(100vh - 64px - 48px); // 扣除顶栏和底栏高度
}

.layout-footer-wrapper {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .footer-text {
    font-size: 14px;
    color: #999;
  }
}
</style>