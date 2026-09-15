<template>
  <div class="inspection-ledger">
    <div class="filter-bar">
      <div class="filter-left">
        <el-select
          v-model="filterEquipmentId"
          placeholder="按器材筛选"
          clearable
          class="filter-select"
        >
          <el-option
            v-for="eq in equipmentFilterOptions"
            :key="eq.equipmentNo"
            :label="`${eq.equipmentNo} ${eq.name}`"
            :value="eq.id"
          />
        </el-select>
        <el-select
          v-model="filterStatus"
          placeholder="按状态筛选"
          clearable
          class="filter-select filter-status"
        >
          <el-option label="待接单" value="PENDING" />
          <el-option label="已修复" value="REPAIRED" />
        </el-select>
        <el-button @click="resetFilter">重置筛选</el-button>
      </div>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><component :is="icons.Plus" /></el-icon>
        新增送检单
      </el-button>
    </div>

    <div class="ledger-summary">
      共 {{ orderList.length }} 条送检记录，其中
      <el-tag type="warning" size="small">待接单 {{ pendingCount }}</el-tag>
      <el-tag type="success" size="small">已修复 {{ repairedCount }}</el-tag>
    </div>

    <el-table v-loading="loading" :data="filteredList" border stripe>
      <el-table-column prop="orderNo" label="送检单号" width="160" />
      <el-table-column prop="equipmentNo" label="器材编号" width="110" />
      <el-table-column prop="equipmentName" label="器材名称" width="150" />
      <el-table-column prop="coldResistanceSpec" label="耐寒规格" width="140" />
      <el-table-column prop="inspectionNote" label="送检说明" min-width="220" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'REPAIRED' ? 'success' : 'warning'">
            {{ row.status === 'REPAIRED' ? '已修复' : '待接单' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operator" label="建单馆务" width="100">
        <template #default="{ row }">{{ row.operator || '—' }}</template>
      </el-table-column>
      <el-table-column label="送检时间" width="160">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="修复时间" width="160">
        <template #default="{ row }">{{ formatTime(row.repairedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'PENDING'"
            size="small"
            type="success"
            :loading="repairingIds.has(row.id)"
            @click="markRepaired(row)"
          >标记已修复</el-button>
          <span v-else class="repaired-text">已完成</span>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="新增耐寒送检单" v-model="showCreateForm" width="560px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="送检器材" prop="equipmentId">
          <el-select
            v-model="formData.equipmentId"
            placeholder="请挑选送检器材"
            filterable
            class="form-select"
          >
            <el-option
              v-for="eq in equipmentList"
              :key="eq.id"
              :label="equipmentOptionLabel(eq)"
              :value="eq.id"
              :disabled="pendingEquipmentIds.has(eq.id)"
            />
          </el-select>
          <div class="form-tip">已有待接单的器材不可重复送检，待修复后才能再次建单。</div>
        </el-form-item>
        <el-form-item label="送检说明" prop="inspectionNote">
          <el-input
            v-model="formData.inspectionNote"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="写明耐寒规格偏差情况（如实测耐温与标称规格不符）及送检要求"
          />
        </el-form-item>
        <el-form-item label="建单馆务">
          <el-input v-model="formData.operator" placeholder="口头核对时便于联络（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateForm = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">提交送检单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getEquipmentList } from '../api/equipment'
import { getInspectionList, createInspection, markInspectionRepaired } from '../api/inspection'

const icons = { Plus }

const loading = ref(false)
const submitting = ref(false)
const orderList = ref([])
const equipmentList = ref([])
const repairingIds = ref(new Set())

// 筛选条件放在前端做：点“标记已修复”后该行状态可立即翻转，不必等待重新请求
const filterEquipmentId = ref(null)
const filterStatus = ref(null)

const showCreateForm = ref(false)
const formRef = ref(null)
const formData = ref({
  equipmentId: null,
  inspectionNote: '',
  operator: ''
})

const rules = {
  equipmentId: [{ required: true, message: '请挑选送检器材', trigger: 'change' }],
  inspectionNote: [{ required: true, message: '请填写送检说明', trigger: 'blur' }]
}

const pendingEquipmentIds = computed(() => {
  const ids = new Set()
  orderList.value.forEach((order) => {
    if (order.status === 'PENDING') {
      ids.add(order.equipmentId)
    }
  })
  return ids
})

const pendingCount = computed(() => orderList.value.filter((o) => o.status === 'PENDING').length)
const repairedCount = computed(() => orderList.value.filter((o) => o.status === 'REPAIRED').length)

// 器材筛选下拉：在册器材为主，并补上历史台账里在册清单之外的器材，保证每行都能被筛到
const equipmentFilterOptions = computed(() => {
  const options = [...equipmentList.value]
  const knownIds = new Set(options.map((eq) => eq.id))
  orderList.value.forEach((order) => {
    if (!knownIds.has(order.equipmentId)) {
      knownIds.add(order.equipmentId)
      options.push({
        id: order.equipmentId,
        equipmentNo: order.equipmentNo,
        name: order.equipmentName
      })
    }
  })
  return options
})

const filteredList = computed(() => {
  return orderList.value.filter((order) => {
    const matchEquipment = !filterEquipmentId.value || order.equipmentId === filterEquipmentId.value
    const matchStatus = !filterStatus.value || order.status === filterStatus.value
    return matchEquipment && matchStatus
  })
})

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getInspectionList({})
    if (res.success) {
      orderList.value = res.data
    } else {
      ElMessage.error(res.message || '送检台账加载失败')
    }
  } finally {
    loading.value = false
  }
}

const loadEquipment = async () => {
  const res = await getEquipmentList({})
  if (res.success) {
    equipmentList.value = res.data
  }
}

const resetFilter = () => {
  filterEquipmentId.value = null
  filterStatus.value = null
}

const equipmentOptionLabel = (eq) => {
  const suffix = pendingEquipmentIds.value.has(eq.id) ? '（已有待接单）' : ''
  return `${eq.equipmentNo} ${eq.name}${suffix}`
}

const openCreateDialog = () => {
  formData.value = { equipmentId: null, inspectionNote: '', operator: '' }
  showCreateForm.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const res = await createInspection(formData.value)
      if (res.success) {
        ElMessage.success('送检单已建立：待接单')
        showCreateForm.value = false
        orderList.value.unshift(res.data)
      } else {
        ElMessage.error(res.message || '建单失败')
      }
    } finally {
      submitting.value = false
    }
  })
}

// 乐观更新：点击后本行立即变为“已修复”；接口失败再回滚，避免界面假状态
const markRepaired = async (row) => {
  if (repairingIds.value.has(row.id)) return
  repairingIds.value.add(row.id)

  const previousStatus = row.status
  const previousRepairedAt = row.repairedAt
  row.status = 'REPAIRED'
  row.repairedAt = formatNow()

  try {
    const res = await markInspectionRepaired(row.id)
    if (res.success) {
      if (res.data && res.data.repairedAt) {
        row.repairedAt = res.data.repairedAt
      }
      ElMessage.success('已标记为已修复')
    } else {
      row.status = previousStatus
      row.repairedAt = previousRepairedAt
      ElMessage.error(res.message || '操作失败，请重试')
    }
  } catch (e) {
    row.status = previousStatus
    row.repairedAt = previousRepairedAt
    ElMessage.error('网络异常，操作未生效，请重试')
  } finally {
    repairingIds.value.delete(row.id)
  }
}

const pad = (n) => String(n).padStart(2, '0')

const formatNow = () => {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const formatTime = (time) => (time ? String(time).replace('T', ' ').slice(0, 16) : '—')

onMounted(() => {
  loadOrders()
  loadEquipment()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-select {
  width: 220px;
}

.filter-status {
  width: 150px;
}

.ledger-summary {
  margin-bottom: 12px;
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-select {
  width: 100%;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  margin-top: 4px;
}

.repaired-text {
  color: #67c23a;
  font-size: 13px;
}
</style>
