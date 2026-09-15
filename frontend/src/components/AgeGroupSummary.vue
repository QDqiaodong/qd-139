<template>
  <div class="age-group-summary">
    <div class="summary-cards">
      <el-card v-for="summary in summaryList" :key="summary.ageGroup" class="summary-card">
        <template #header>
          <div class="card-header">
            <el-tag :type="getAgeGroupTagType(summary.ageGroup)" size="large">
              {{ summary.ageGroupName }}
            </el-tag>
            <div class="counts">
              <span>共 {{ summary.totalCount }} 件</span>
              <span class="count-active">在用 {{ summary.activeCount }} 件</span>
              <el-button
                link
                type="danger"
                :disabled="summary.inactiveCount === 0"
                @click="openInactiveDialog(summary)"
              >
                停用 {{ summary.inactiveCount }} 件
              </el-button>
            </div>
          </div>
        </template>
        <el-table :data="summary.activeEquipments" border stripe size="small">
          <el-table-column prop="equipmentNo" label="编号" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="coldResistanceSpec" label="耐寒规格" />
          <template #empty>暂无在用器材</template>
        </el-table>
      </el-card>
    </div>

    <el-dialog
      v-model="showInactiveDialog"
      :title="`${currentAgeGroupName}停用器材名单（共 ${currentInactiveList.length} 件）`"
      width="480px"
    >
      <el-table :data="currentInactiveList" border stripe size="small">
        <el-table-column prop="equipmentNo" label="编号" width="160" />
        <el-table-column prop="name" label="名称" />
        <template #empty>无停用器材</template>
      </el-table>
      <template #footer>
        <el-button type="primary" @click="showInactiveDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSummaryByAllAgeGroups } from '../api/summary'

const summaryList = ref([])
const showInactiveDialog = ref(false)
const currentAgeGroupName = ref('')
const currentInactiveList = ref([])

const loadData = async () => {
  const res = await getSummaryByAllAgeGroups()
  if (res.success) {
    summaryList.value = res.data
  }
}

const openInactiveDialog = (summary) => {
  currentAgeGroupName.value = summary.ageGroupName
  currentInactiveList.value = summary.inactiveEquipments || []
  showInactiveDialog.value = true
}

const getAgeGroupTagType = (ageGroup) => {
  const map = { CHILD: 'info', ADULT: 'success', ELDERLY: 'warning' }
  return map[ageGroup] || 'info'
}

onMounted(loadData)
</script>

<style scoped>
.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
}

.summary-card {
  height: fit-content;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.counts {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #666;
}

.count-active {
  color: #67c23a;
}
</style>
