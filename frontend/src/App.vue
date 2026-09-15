<template>
  <div class="app-container">
    <el-container>
      <el-aside width="220px" class="sidebar">
        <div class="logo">
          <span>极地模拟体验馆</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          background-color="#2a3f5f"
          text-color="#fff"
          active-text-color="#409eff"
          @select="handleMenuSelect"
        >
          <el-menu-item index="equipment">
            <el-icon><component :is="icons.List" /></el-icon>
            <span>低温配套器材管理</span>
          </el-menu-item>
          <el-menu-item index="inspection">
            <el-icon><component :is="icons.Tools" /></el-icon>
            <span>耐寒送检台账</span>
          </el-menu-item>
          <el-menu-item index="session">
            <el-icon><component :is="icons.Calendar" /></el-icon>
            <span>体验场次管理</span>
          </el-menu-item>
          <el-menu-item index="changeLog">
            <el-icon><component :is="icons.Document" /></el-icon>
            <span>年龄段变更台账</span>
          </el-menu-item>
          <el-menu-item index="summary">
            <el-icon><component :is="icons.DataBoard" /></el-icon>
            <span>年龄段分组汇总</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <h2>{{ pageTitle }}</h2>
          <NotificationCenter />
        </el-header>
        <el-main class="main-content">
          <EquipmentList v-if="activeMenu === 'equipment'" />
          <InspectionLedger v-else-if="activeMenu === 'inspection'" />
          <SessionList v-else-if="activeMenu === 'session'" />
          <ChangeLogList v-else-if="activeMenu === 'changeLog'" />
          <AgeGroupSummary v-else-if="activeMenu === 'summary'" />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { List, Tools, Calendar, Document, DataBoard } from '@element-plus/icons-vue'
import EquipmentList from './components/EquipmentList.vue'
import InspectionLedger from './components/InspectionLedger.vue'
import SessionList from './components/SessionList.vue'
import ChangeLogList from './components/ChangeLogList.vue'
import AgeGroupSummary from './components/AgeGroupSummary.vue'
import NotificationCenter from './components/NotificationCenter.vue'

const icons = { List, Tools, Calendar, Document, DataBoard }
const activeMenu = ref('equipment')

const handleMenuSelect = (index) => {
  activeMenu.value = index
}

const pageTitle = computed(() => {
  const titles = {
    equipment: '低温配套器材管理',
    inspection: '耐寒送检台账',
    session: '体验场次管理',
    changeLog: '年龄段变更台账',
    summary: '年龄段分组汇总'
  }
  return titles[activeMenu.value] || ''
})
</script>

<style scoped>
.app-container {
  height: 100vh;
  display: flex;
}

.sidebar {
  background-color: #2a3f5f;
  color: #fff;
}

.logo {
  padding: 20px;
  font-size: 16px;
  font-weight: bold;
  text-align: center;
  border-bottom: 1px solid #3a4f6f;
}

.sidebar-menu {
  border-right: none;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #eee;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.main-content {
  padding: 20px;
}
</style>
