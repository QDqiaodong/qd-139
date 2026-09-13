<template>
  <div class="change-log-list">
    <div class="filter-bar">
      <el-select v-model="filterAgeGroup" placeholder="按年龄段筛选" clearable class="filter-select">
        <el-option label="儿童" value="CHILD" />
        <el-option label="成人" value="ADULT" />
        <el-option label="老年" value="ELDERLY" />
      </el-select>
      <el-button type="primary" @click="showAddForm = true">
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

    <el-dialog title="变更年龄段" v-model="showAddForm" width="500px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="场次ID" prop="sessionId">
          <el-input v-model.number="formData.sessionId" />
        </el-form-item>
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
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { changeAgeGroup, getLogsByAgeGroup } from '../api/changeLog'

const icons = { Plus }
const changeLogList = ref([])
const filterAgeGroup = ref(null)
const showAddForm = ref(false)
const formRef = ref(null)

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
    if (valid) {
      const res = await changeAgeGroup(formData.value)
      if (res.success) {
        ElMessage.success('变更成功')
        showAddForm.value = false
        loadData()
      } else {
        ElMessage.error(res.message)
      }
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
</style>