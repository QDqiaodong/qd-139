<template>
  <div class="notification-center">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
      <el-icon class="notification-bell" :size="22" @click="openDrawer">
        <component :is="icons.Bell" />
      </el-icon>
    </el-badge>

    <el-drawer v-model="showDrawer" title="站内通知（当班馆务）" size="420px">
      <el-empty v-if="notificationList.length === 0" description="暂无通知" />
      <div
        v-for="item in notificationList"
        :key="item.id"
        class="notification-item"
        :class="{ unread: !item.readFlag }"
      >
        <div class="notification-header">
          <span class="notification-title">
            <el-tag :type="item.readFlag ? 'info' : 'danger'" size="small">
              {{ item.readFlag ? '已读' : '未读' }}
            </el-tag>
            {{ item.title }}
          </span>
          <el-button
            v-if="!item.readFlag"
            size="small"
            type="primary"
            link
            @click="markRead(item)"
          >标为已读</el-button>
        </div>
        <div class="notification-content">{{ item.content }}</div>
        <div class="notification-time">{{ formatTime(item.createdAt) }}</div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getNotificationList, getUnreadCount, markNotificationRead } from '../api/notification'

const icons = { Bell }
const notificationList = ref([])
const unreadCount = ref(0)
const showDrawer = ref(false)
let timer = null

const loadUnreadCount = async () => {
  const res = await getUnreadCount()
  if (res.success) {
    unreadCount.value = res.data
  }
}

const loadList = async () => {
  const res = await getNotificationList()
  if (res.success) {
    notificationList.value = res.data
  }
}

const openDrawer = async () => {
  showDrawer.value = true
  await loadList()
}

// 本人打开通知后手动标已读；未标已读的通知一直保留在列表中
const markRead = async (item) => {
  const res = await markNotificationRead(item.id)
  if (res.success) {
    ElMessage.success('已标记为已读')
    loadList()
    loadUnreadCount()
  } else {
    ElMessage.error(res.message)
  }
}

const formatTime = (time) => (time ? String(time).replace('T', ' ').slice(0, 16) : '')

onMounted(() => {
  loadUnreadCount()
  loadList()
  timer = setInterval(() => {
    loadUnreadCount()
    if (showDrawer.value) {
      loadList()
    }
  }, 30000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.notification-center {
  display: flex;
  align-items: center;
}

.notification-bell {
  cursor: pointer;
  color: #333;
}

.notification-bell:hover {
  color: #409eff;
}

.notification-item {
  padding: 12px;
  margin-bottom: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #fff;
}

.notification-item.unread {
  border-left: 3px solid #f56c6c;
  background-color: #fef0f0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-title {
  font-weight: bold;
  font-size: 14px;
  color: #333;
}

.notification-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
}

.notification-time {
  font-size: 12px;
  color: #909399;
  text-align: right;
}
</style>
