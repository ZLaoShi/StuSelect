<template>
  <n-config-provider :theme="theme">
    <n-message-provider>
      <n-notification-provider>
        <n-dialog-provider>
          <n-loading-bar-provider>
            <router-view />
          </n-loading-bar-provider>
        </n-dialog-provider>
      </n-notification-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { 
  NConfigProvider, 
  NMessageProvider, 
  NNotificationProvider, 
  NDialogProvider,
  NLoadingBarProvider,
  useOsTheme,
  darkTheme
} from 'naive-ui'
import { computed } from 'vue'

// 使用操作系统主题
const osThemeRef = useOsTheme()
const theme = computed(() => {
  // 如果想使用暗色主题，可以取消注释下面的代码
  // return osThemeRef.value === 'dark' ? darkTheme : null
  return null
})
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
}

#app {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>