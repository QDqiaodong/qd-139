<template>
  <div class="session-list">
    <div class="filter-bar">
      <el-button type="primary" @click="openAddForm">
        <el-icon><component :is="icons.Plus" /></el-icon>
        新增场次
      </el-button>
    </div>

    <el-table :data="sessionList" border stripe>
      <el-table-column prop="sessionNo" label="场次编号" />
      <el-table-column prop="name" label="场次名称" />
      <el-table-column prop="startTime" label="开始时间" />
      <el-table-column prop="endTime" label="结束时间" />
      <el-table-column label="人群配比">
        <template #default="{ row }">
          <el-tag type="info" size="small">儿童 {{ row.childRatio }}%</el-tag>
          <el-tag type="success" size="small">成人 {{ row.adultRatio }}%</el-tag>
          <el-tag type="warning" size="small">老年 {{ row.elderlyRatio }}%</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="主年龄段">
        <template #default="{ row }">
          <el-tag :type="getAgeGroupTagType(getRowMainAgeGroup(row))" size="small">
            {{ getAgeGroupName(getRowMainAgeGroup(row)) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button size="small" @click="editSession(row)">编辑</el-button>
          <el-button size="small" @click="bindEquipment(row)">绑定器材</el-button>
          <el-button size="small" type="danger" @click="deleteSession(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      :title="isEdit ? '编辑场次' : '新增场次'"
      v-model="showAddForm"
      width="600px"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="场次编号" prop="sessionNo">
          <el-input v-model="formData.sessionNo" />
        </el-form-item>
        <el-form-item label="场次名称" prop="name">
          <el-input v-model="formData.name" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="formData.startTime" type="datetime" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="formData.endTime" type="datetime" />
        </el-form-item>
        <el-form-item label="人群配比">
          <div class="ratio-row">
            <el-input-number v-model="formData.childRatio" :min="0" :max="100" label="儿童" />
            <el-input-number v-model="formData.adultRatio" :min="0" :max="100" label="成人" />
            <el-input-number v-model="formData.elderlyRatio" :min="0" :max="100" label="老年" />
          </div>
          <div class="ratio-sum" :class="{ 'ratio-sum-error': ratioSum !== 100 }">
            当前合计：{{ ratioSum }}%，三项之和必须等于100%才能保存
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddForm = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="绑定器材" v-model="showBindForm" width="600px">
      <el-alert
        v-if="currentSession"
        :title="`本场次主年龄段：${getAgeGroupName(currentMainAgeGroup)}（儿童 ${currentSession.childRatio}% / 成人 ${currentSession.adultRatio}% / 老年 ${currentSession.elderlyRatio}%），仅可绑定适配【${getAgeGroupName(currentMainAgeGroup)}】的器材`"
        type="info"
        :closable="false"
        class="bind-tip"
      />
      <el-form :model="bindForm" label-width="100px">
        <el-form-item label="可选器材">
          <el-select v-model="bindForm.equipmentIds" multiple filterable style="width: 100%">
            <el-option
              v-for="item in allEquipment"
              :key="item.id"
              :label="`${item.name}（适配${getAgeGroupName(item.ageGroup)}）`"
              :value="item.id"
              :disabled="item.ageGroup !== currentMainAgeGroup"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBindForm = false">取消</el-button>
        <el-button type="primary" @click="submitBind">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getSessionList, createSession, updateSession, deleteSession as apiDeleteSession, bindEquipments } from '../api/session'
import { getEquipmentList } from '../api/equipment'

const icons = { Plus }
const sessionList = ref([])
const allEquipment = ref([])
const showAddForm = ref(false)
const showBindForm = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const currentSessionId = ref(null)
const currentSession = ref(null)

const formData = ref({
  id: null,
  sessionNo: '',
  name: '',
  startTime: '',
  endTime: '',
  childRatio: 0,
  adultRatio: 100,
  elderlyRatio: 0,
  status: 1
})

const bindForm = ref({
  equipmentIds: []
})

const rules = {
  sessionNo: [{ required: true, message: '请输入场次编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入场次名称', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

// 儿童、成人、老年三项人群配比之和，必须等于100才能保存
const ratioSum = computed(() => {
  const child = formData.value.childRatio || 0
  const adult = formData.value.adultRatio || 0
  const elderly = formData.value.elderlyRatio || 0
  return child + adult + elderly
})

// 主年龄段：占比最高的一类；并列时按儿童、成人、老年顺序取前者（与后端口径一致）
const resolveMainAgeGroup = (session) => {
  const child = session.childRatio || 0
  const adult = session.adultRatio || 0
  const elderly = session.elderlyRatio || 0
  if (child >= adult && child >= elderly) return 'CHILD'
  if (adult >= elderly) return 'ADULT'
  return 'ELDERLY'
}

const getRowMainAgeGroup = (row) => row.mainAgeGroup || resolveMainAgeGroup(row)

const currentMainAgeGroup = computed(() => {
  if (!currentSession.value) return null
  return getRowMainAgeGroup(currentSession.value)
})

const loadData = async () => {
  const res = await getSessionList()
  if (res.success) {
    sessionList.value = res.data
  }
}

const loadEquipment = async () => {
  const res = await getEquipmentList()
  if (res.success) {
    allEquipment.value = res.data
  }
}

const editSession = (row) => {
  isEdit.value = true
  formData.value = { ...row }
  showAddForm.value = true
}

const resetFormData = () => ({
  id: null,
  sessionNo: '',
  name: '',
  startTime: '',
  endTime: '',
  childRatio: 0,
  adultRatio: 100,
  elderlyRatio: 0,
  status: 1
})

const openAddForm = () => {
  isEdit.value = false
  formData.value = resetFormData()
  showAddForm.value = true
  formRef.value?.clearValidate?.()
}

const toMillis = (value) => {
  if (!value) return null
  const time = value instanceof Date ? value : new Date(value)
  return Number.isNaN(time.getTime()) ? null : time.getTime()
}

const formatDateTime = (value) => {
  const time = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(time.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${time.getFullYear()}-${pad(time.getMonth() + 1)}-${pad(time.getDate())} `
    + `${pad(time.getHours())}:${pad(time.getMinutes())}:${pad(time.getSeconds())}`
}

// 两个场次的时段是否重叠：半开区间 [开始, 结束)，首尾相接不算重叠
const isTimeOverlap = (startA, endA, startB, endB) =>
  startA < endB && endA > startB

// 保存前预检：与列表中其他正常场次的开始/结束时间只要叠在一起，就拦下本次保存并写明冲突对象与时段
const findOverlappingSession = () => {
  const start = toMillis(formData.value.startTime)
  const end = toMillis(formData.value.endTime)
  if (start === null || end === null) return null
  return sessionList.value.find((row) => {
    if (isEdit.value && row.id === formData.value.id) return false
    if (row.status !== 1) return false
    const otherStart = toMillis(row.startTime)
    const otherEnd = toMillis(row.endTime)
    return otherStart !== null && otherEnd !== null && isTimeOverlap(start, end, otherStart, otherEnd)
  }) || null
}

const buildOverlapMessage = (row) => {
  const start = toMillis(formData.value.startTime)
  const end = toMillis(formData.value.endTime)
  const otherStart = toMillis(row.startTime)
  const otherEnd = toMillis(row.endTime)
  const overlapStart = Math.max(start, otherStart)
  const overlapEnd = Math.min(end, otherEnd)
  return `保存失败：本场次时段与已有的「${row.name}」（场次编号：${row.sessionNo}，`
    + `${formatDateTime(row.startTime)} 至 ${formatDateTime(row.endTime)}）重叠，`
    + `冲突时段为 ${formatDateTime(overlapStart)} 至 ${formatDateTime(overlapEnd)}，请调整时间后再保存`
}

const bindEquipment = async (row) => {
  currentSessionId.value = row.id
  currentSession.value = row
  bindForm.value = { equipmentIds: [...(row.equipmentIds || [])] }
  await loadEquipment()
  showBindForm.value = true
}

const deleteSession = async (row) => {
  const confirm = await ElMessage.confirm('确定要删除该场次吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  if (confirm) {
    const res = await apiDeleteSession(row.id)
    if (res.success) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.message)
    }
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (ratioSum.value !== 100) {
      ElMessage.error(`儿童、成人、老年人群配比之和必须等于100%（当前为${ratioSum.value}%），不能保存`)
      return
    }
    const overlapping = findOverlappingSession()
    if (overlapping) {
      ElMessage.error(buildOverlapMessage(overlapping))
      return
    }
    const res = isEdit.value
      ? await updateSession(formData.value.id, formData.value)
      : await createSession(formData.value)
    if (res.success) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      showAddForm.value = false
      loadData()
    } else {
      ElMessage.error(res.message)
    }
  })
}

const submitBind = async () => {
  const res = await bindEquipments(currentSessionId.value, bindForm.value.equipmentIds)
  if (res.success) {
    ElMessage.success('绑定成功')
    showBindForm.value = false
    loadData()
  } else {
    ElMessage.error(res.message)
    // 绑定失败：恢复为服务端已绑名单，页面上的已绑名单保持不变
    bindForm.value.equipmentIds = [...(currentSession.value?.equipmentIds || [])]
  }
}

const getAgeGroupName = (ageGroup) => {
  const map = { CHILD: '儿童', ADULT: '成人', ELDERLY: '老年' }
  return map[ageGroup] || ageGroup
}

const getAgeGroupTagType = (ageGroup) => {
  const map = { CHILD: 'info', ADULT: 'success', ELDERLY: 'warning' }
  return map[ageGroup] || 'info'
}

onMounted(loadData)
</script>

<style scoped>
.filter-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.ratio-row {
  display: flex;
  gap: 20px;
}

.ratio-sum {
  margin-top: 8px;
  font-size: 12px;
  color: #67c23a;
}

.ratio-sum-error {
  color: #f56c6c;
}

.bind-tip {
  margin-bottom: 16px;
}
</style>
