<template>
  <div class="age-group-summary">
    <div class="summary-cards">
      <el-card v-for="summary in summaryList" :key="summary.ageGroup" class="summary-card">
        <template #header>
          <div class="card-header">
            <el-tag :type="getAgeGroupTagType(summary.ageGroup)" size="large">
              {{ summary.ageGroupName }}
            </el-tag>
            <span class="count">共 {{ summary.totalCount }} 件器材</span>
          </div>
        </template>
        <el-table :data="summary.equipments" border stripe size="small">
          <el-table-column prop="equipmentNo" label="编号" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="coldResistanceSpec" label="耐寒规格" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSummaryByAllAgeGroups } from '../api/summary'

const summaryList = ref([])

const loadData = async () => {
  const res = await getSummaryByAllAgeGroups()
  if (res.success) {
    summaryList.value = res.data
  }
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

.count {
  font-size: 14px;
  color: #666;
}
</style>