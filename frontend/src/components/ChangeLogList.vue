<template>
  <div class="change-log-list">
    <div class="filter-bar">
      <el-select v-model="filterAgeGroup" placeholder="按年龄段筛选" clearable class="filter-select">
        <el-option label="儿童" value="CHILD" />
        <el-option label="成人" value="ADULT" />
        <el-option label="老年" value="ELDERLY" />
      </el-select>
      <el-button type="primary" @click="openAddForm">
        <el-icon><component :is="icons.Plus" /></el-icon>
        变更年龄段
      </el-button>
    </div>

    <el-table :data="changeLogList" border stripe>
      <el-table-column prop="sessionId" label="场次ID" />
      <el-table-column prop="equipmentId" label="器材ID" />
      <el-table-column label="原年龄段">
        <template #default="{ row }">
          <el-tag :type="getAgeGroupTagType(row.oldAgeGroup)">
            {{ getAgeGroupName(row.oldAgeGroup) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="新年龄段">
        <template #default="{ row }">
          <el-tag :type="getAgeGroupTagType(row.newAgeGroup)" effect="dark">
            {{ getAgeGroupName(row.newAgeGroup) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="changeReason" label="变更原因" />
      <el-table-column prop="operator" label="操作人" />
      <el-table-column prop="createdAt" label="变更时间" />
    </el-table>

    <el-dialog title="变更年龄段" v-model="showAddForm" width="500px" @closed="resetForm">
      <el-alert
        title="已停用的场次按失效处理，不能再变更器材年龄段；同一器材两人同时变更时，后提交的一方将按冲突处理。"
        type="info"
        :closable="false"
        show-icon
        class="form-tip"
      />
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="场次ID" prop="sessionId">
          <el-input
            v-model.number="formData.sessionId"
            placeholder="请输入场次ID"
            @blur="checkSession"
          />
        </el-form-item>
        <el-form-item v-if="sessionChecking">
          <el-text type="info" size="small">正在校验场次状态…</el-text>
        </el-form-item>
        <el-alert
          v-if="sessionStatusError"
          :title="sessionStatusError"
          type="error"
          :closable="false"
          show-icon
          class="session-status-alert"
        />
        <el-alert
          v-else-if="sessionInfo"
          :title="`场次【${sessionInfo.sessionNo}】${sessionInfo.name}，状态：正常`"
          type="success"
          :closable="false"
          class="session-status-alert"
        />
        <el-form-item label="器材ID" prop="equipmentId">
          <el-input v-model.number="formData.equipmentId" />
        </el-form-item>
        <el-form-item label="新年龄段" prop="newAgeGroup">
          <el-select v-model="formData.newAgeGroup" placeholder="请选择年龄段">
            <el-option label="儿童" value="CHILD" />
            <el-option label="成人" value="ADULT" />
            <el-option label="老年" value="ELDERLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="变更原因">
          <el-input v-model="formData.changeReason" type="textarea" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="formData.operator" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddForm = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { changeAgeGroup, getLogsByAgeGroup } from '../api/changeLog'
import { getSessionById } from '../api/session'

const icons = { Plus }
const changeLogList = ref([])
const filterAgeGroup = ref(null)
const showAddForm = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const sessionChecking = ref(false)
const sessionInfo = ref(null)
const sessionStatusError = ref('')

const formData = ref({
  sessionId: null,
  equipmentId: null,
  newAgeGroup: '',
  changeReason: '',
  operator: ''
})

const rules = {
  sessionId: [{ required: true, message: '请输入场次ID', trigger: 'blur' }],
  equipmentId: [{ required: true, message: '请输入器材ID', trigger: 'blur' }],
  newAgeGroup: [{ required: true, message: '请选择年龄段', trigger: 'change' }]
}

// 输入场次ID后向服务端核实场次状态：已停用的场次直接拦截，不再允许提交变更
const checkSession = async () => {
  sessionInfo.value = null
  sessionStatusError.value = ''
  if (!formData.value.sessionId) return

  sessionChecking.value = true
  try {
    const res = await getSessionById(formData.value.sessionId)
    if (!res || res.success === false) {
      sessionStatusError.value = res?.message || '场次不存在，无法变更年龄段'
      return
    }
    const session = res.data
    if (session.status !== 1) {
      sessionStatusError.value = `场次【${session.sessionNo}】${session.name}已停用，按失效处理，不能再变更器材年龄段`
      return
    }
    sessionInfo.value = session
  } finally {
    sessionChecking.value = false
  }
}

const resetForm = () => {
  formData.value = {
    sessionId: null,
    equipmentId: null,
    newAgeGroup: '',
    changeReason: '',
    operator: ''
  }
  sessionInfo.value = null
  sessionStatusError.value = ''
  sessionChecking.value = false
  formRef.value?.clearValidate()
}

const openAddForm = () => {
  resetForm()
  showAddForm.value = true
}

const loadData = async () => {
  if (filterAgeGroup.value) {
    const res = await getLogsByAgeGroup(filterAgeGroup.value)
    if (res.success) {
      changeLogList.value = res.data
    }
  } else {
    changeLogList.value = []
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    // 提交前再查一次最新状态，防止打开弹窗后场次被停用
    await checkSession()
    if (sessionStatusError.value) {
      ElMessage.error(sessionStatusError.value)
      return
    }
    submitting.value = true
    try {
      const res = await changeAgeGroup(formData.value)
      if (res.success) {
        ElMessage.success('变更成功')
        showAddForm.value = false
        loadData()
      } else if (res.conflict) {
        ElMessage.error(res.message || '操作冲突，请刷新后重试')
      } else {
        ElMessage.error(res.message)
      }
    } finally {
      submitting.value = false
    }
  })
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
watch(filterAgeGroup, loadData)
</script>

<style scoped>
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-select {
  width: 200px;
}

.form-tip {
  margin-bottom: 16px;
}

.session-status-alert {
  margin: 0 0 18px 100px;
}
</style>
