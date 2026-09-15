<template>
  <div class="feedback-register">
    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>散场登记</span>
          <span class="header-tip">同一场次、同一位游客，当天只能记一条</span>
        </div>
      </template>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="90px">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="8">
            <el-form-item label="登记日期" prop="feedbackDate">
              <el-date-picker
                v-model="formData.feedbackDate"
                type="date"
                value-format="YYYY-MM-DD"
                :clearable="false"
                class="full-width"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="16">
            <el-form-item label="体验场次" prop="sessionId">
              <el-select
                v-model="formData.sessionId"
                placeholder="请选择刚散场的这一场"
                filterable
                class="full-width"
              >
                <el-option
                  v-for="s in sessionList"
                  :key="s.id"
                  :label="sessionOptionLabel(s)"
                  :value="s.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="8">
            <el-form-item label="游客称呼" prop="visitorName">
              <el-input
                v-model="formData.visitorName"
                maxlength="50"
                placeholder="如：张女士 / 带孩子的爸爸"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="登记人" prop="registrar">
              <el-input v-model="formData.registrar" maxlength="50" placeholder="填写你的名字" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="室温感觉" prop="tempFeeling">
              <el-radio-group v-model="formData.tempFeeling">
                <el-radio-button
                  v-for="o in TEMP_FEELING_OPTIONS"
                  :key="o.value"
                  :value="o.value"
                >{{ o.label }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="手脚发麻" prop="numbness">
              <el-radio-group v-model="formData.numbness">
                <el-radio-button
                  v-for="o in NUMBNESS_OPTIONS"
                  :key="o.value"
                  :value="o.value"
                >{{ o.label }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-alert
          v-if="duplicateEntry"
          type="warning"
          :closable="false"
          class="duplicate-alert"
          :title="`「${duplicateEntry.visitorName}」今天在该场次已登记过（登记人：${duplicateEntry.registrar}），同一场同一游客当天只记一条`"
        />
        <el-form-item class="submit-item">
          <el-button
            type="primary"
            :loading="submitting"
            :disabled="!!duplicateEntry"
            @click="submitForm"
          >登记</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>登记名单</span>
          <div class="list-filter">
            <el-date-picker
              v-model="filterDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="按日期筛选"
              clearable
              class="filter-date"
            />
            <el-select
              v-model="filterSessionId"
              placeholder="按场次筛选"
              clearable
              class="filter-session"
            >
              <el-option
                v-for="s in sessionFilterOptions"
                :key="s.id"
                :label="s.name"
                :value="s.id"
              />
            </el-select>
          </div>
        </div>
      </template>

      <div class="list-summary">
        共 {{ filteredList.length }} 条登记，其中手脚发麻
        <el-tag :type="numbCount > 0 ? 'danger' : 'success'" size="small">{{ numbCount }} 条</el-tag>
        <template v-if="numbCount > 0">（轻微 {{ mildCount }} / 明显 {{ obviousCount }}）</template>
      </div>

      <el-table v-loading="loading" :data="filteredList" border stripe>
        <el-table-column prop="feedbackDate" label="登记日期" width="110" />
        <el-table-column prop="sessionName" label="场次" min-width="150" show-overflow-tooltip />
        <el-table-column prop="visitorName" label="游客称呼" width="130" />
        <el-table-column label="室温感觉" width="100">
          <template #default="{ row }">
            <el-tag :type="tempFeelingTagType(row.tempFeeling)" size="small">
              {{ tempFeelingLabel(row.tempFeeling) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="手脚发麻" width="100">
          <template #default="{ row }">
            <el-tag :type="numbnessTagType(row.numbness)" size="small">
              {{ numbnessLabel(row.numbness) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registrar" label="登记人" width="100" />
        <el-table-column label="登记时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <template #empty>该条件下还没有登记记录</template>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSessionList } from '../api/session'
import { getFeedbackList, createFeedback } from '../api/feedback'

const TEMP_FEELING_OPTIONS = [
  { value: 'VERY_COLD', label: '很冷' },
  { value: 'COLD', label: '有点冷' },
  { value: 'COMFORTABLE', label: '适中' },
  { value: 'NOT_COLD', label: '不冷' }
]
const NUMBNESS_OPTIONS = [
  { value: 'NONE', label: '无' },
  { value: 'MILD', label: '轻微' },
  { value: 'OBVIOUS', label: '明显' }
]

// 登记人记在本机，连续登记下一位游客时不用重填
const REGISTRAR_STORAGE_KEY = 'experience_feedback_registrar'

const pad = (n) => String(n).padStart(2, '0')
const todayStr = () => {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const sessionList = ref([])
const feedbackList = ref([])
const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const formData = ref({
  feedbackDate: todayStr(),
  sessionId: null,
  visitorName: '',
  tempFeeling: '',
  numbness: '',
  registrar: localStorage.getItem(REGISTRAR_STORAGE_KEY) || ''
})

const rules = {
  feedbackDate: [{ required: true, message: '请选择登记日期', trigger: 'change' }],
  sessionId: [{ required: true, message: '请选择体验场次', trigger: 'change' }],
  visitorName: [{ required: true, message: '请填写游客称呼', trigger: 'blur' }],
  tempFeeling: [{ required: true, message: '请选择室温感觉', trigger: 'change' }],
  numbness: [{ required: true, message: '请选择手脚是否发麻', trigger: 'change' }],
  registrar: [{ required: true, message: '请填写登记人', trigger: 'blur' }]
}

// 筛选条件放在前端做：登记成功后新行可立即进名单，不必等待重新请求
const filterDate = ref(todayStr())
const filterSessionId = ref(null)

const normalizeVisitor = (name) => (name || '').trim().replace(/\s+/g, ' ')

// 前端拦截的一半：名单里已有 同场次+同日期+同游客 的记录时，提示并置灰提交按钮
const duplicateEntry = computed(() => {
  const visitor = normalizeVisitor(formData.value.visitorName)
  if (!visitor || !formData.value.sessionId || !formData.value.feedbackDate) return null
  return (
    feedbackList.value.find(
      (f) =>
        f.sessionId === formData.value.sessionId &&
        f.feedbackDate === formData.value.feedbackDate &&
        f.visitorName === visitor
    ) || null
  )
})

const filteredList = computed(() =>
  feedbackList.value.filter((f) => {
    const matchDate = !filterDate.value || f.feedbackDate === filterDate.value
    const matchSession = !filterSessionId.value || f.sessionId === filterSessionId.value
    return matchDate && matchSession
  })
)

// 场次筛选下拉：在用场次为主，并补上名单里出现过的历史场次，保证每行都能被筛到
const sessionFilterOptions = computed(() => {
  const options = sessionList.value.map((s) => ({ id: s.id, name: s.name }))
  const knownIds = new Set(options.map((o) => o.id))
  feedbackList.value.forEach((f) => {
    if (!knownIds.has(f.sessionId)) {
      knownIds.add(f.sessionId)
      options.push({ id: f.sessionId, name: f.sessionName })
    }
  })
  return options
})

const numbCount = computed(() => filteredList.value.filter((f) => f.numbness !== 'NONE').length)
const mildCount = computed(() => filteredList.value.filter((f) => f.numbness === 'MILD').length)
const obviousCount = computed(() => filteredList.value.filter((f) => f.numbness === 'OBVIOUS').length)

const tempFeelingLabel = (v) => (TEMP_FEELING_OPTIONS.find((o) => o.value === v) || {}).label || v
const numbnessLabel = (v) => (NUMBNESS_OPTIONS.find((o) => o.value === v) || {}).label || v

const tempFeelingTagType = (v) => {
  if (v === 'VERY_COLD') return 'danger'
  if (v === 'COLD') return 'warning'
  return 'success'
}
const numbnessTagType = (v) => {
  if (v === 'OBVIOUS') return 'danger'
  if (v === 'MILD') return 'warning'
  return 'success'
}

const sessionOptionLabel = (s) => `${s.name}（${formatSessionTime(s.startTime)}）`

const formatSessionTime = (time) => (time ? String(time).replace('T', ' ').slice(5, 16) : '')
const formatTime = (time) => (time ? String(time).replace('T', ' ').slice(0, 16) : '—')

const loadFeedback = async () => {
  loading.value = true
  try {
    const res = await getFeedbackList({})
    if (res.success) {
      feedbackList.value = res.data
    } else {
      ElMessage.error(res.message || '登记名单加载失败')
    }
  } finally {
    loading.value = false
  }
}

const loadSessions = async () => {
  const res = await getSessionList()
  if (res.success) {
    sessionList.value = res.data
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (duplicateEntry.value) return
    submitting.value = true
    try {
      const res = await createFeedback({
        ...formData.value,
        visitorName: normalizeVisitor(formData.value.visitorName),
        registrar: formData.value.registrar.trim()
      })
      if (res.success) {
        ElMessage.success('已登记')
        feedbackList.value.unshift(res.data)
        localStorage.setItem(REGISTRAR_STORAGE_KEY, formData.value.registrar.trim())
        // 日期、场次、登记人保留，方便散场时连续登记下一位游客
        formData.value.visitorName = ''
        formData.value.tempFeeling = ''
        formData.value.numbness = ''
        formRef.value.clearValidate()
      } else {
        ElMessage.error(res.message || '登记失败')
      }
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadFeedback()
  loadSessions()
})
</script>

<style scoped>
.form-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.header-tip {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.full-width {
  width: 100%;
}

.duplicate-alert {
  margin-bottom: 12px;
}

.submit-item {
  margin-bottom: 0;
}

.list-filter {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-date {
  width: 150px;
}

.filter-session {
  width: 220px;
}

.list-summary {
  margin-bottom: 12px;
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
