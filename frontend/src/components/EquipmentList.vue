<template>
  <div class="equipment-list">
    <div class="filter-bar">
      <el-select v-model="filterAgeGroup" placeholder="按年龄段筛选" clearable class="filter-select">
        <el-option label="儿童" value="CHILD" />
        <el-option label="成人" value="ADULT" />
        <el-option label="老年" value="ELDERLY" />
      </el-select>
      <el-button type="primary" @click="showAddForm = true">
        <el-icon><component :is="icons.Plus" /></el-icon>
        新增器材
      </el-button>
    </div>

    <el-table :data="equipmentList" border stripe>
      <el-table-column prop="equipmentNo" label="器材编号" />
      <el-table-column prop="name" label="器材名称" />
      <el-table-column prop="coldResistanceSpec" label="耐寒规格" />
      <el-table-column prop="ageGroup" label="适配年龄段">
        <template #default="{ row }">
          <el-tag :type="getAgeGroupTagType(row.ageGroup)">
            {{ getAgeGroupName(row.ageGroup) }}
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
          <el-button size="small" @click="editEquipment(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteEquipment(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      :title="isEdit ? '编辑器材' : '新增器材'"
      v-model="showAddForm"
      width="500px"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="器材编号" prop="equipmentNo">
          <el-input v-model="formData.equipmentNo" />
        </el-form-item>
        <el-form-item label="器材名称" prop="name">
          <el-input v-model="formData.name" />
        </el-form-item>
        <el-form-item label="耐寒规格" prop="coldResistanceSpec">
          <el-input v-model="formData.coldResistanceSpec" />
        </el-form-item>
        <el-form-item label="适配年龄段" prop="ageGroup">
          <el-select v-model="formData.ageGroup" placeholder="请选择年龄段">
            <el-option label="儿童" value="CHILD" />
            <el-option label="成人" value="ADULT" />
            <el-option label="老年" value="ELDERLY" />
          </el-select>
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
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getEquipmentList, createEquipment, updateEquipment, deleteEquipment as apiDeleteEquipment } from '../api/equipment'

const icons = { Plus }
const equipmentList = ref([])
const filterAgeGroup = ref(null)
const showAddForm = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const formData = ref({
  id: null,
  equipmentNo: '',
  name: '',
  coldResistanceSpec: '',
  ageGroup: '',
  status: 1
})

const rules = {
  equipmentNo: [{ required: true, message: '请输入器材编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入器材名称', trigger: 'blur' }],
  coldResistanceSpec: [{ required: true, message: '请输入耐寒规格', trigger: 'blur' }],
  ageGroup: [{ required: true, message: '请选择年龄段', trigger: 'change' }]
}

const loadData = async () => {
  const params = filterAgeGroup.value ? { ageGroup: filterAgeGroup.value } : {}
  const res = await getEquipmentList(params)
  if (res.success) {
    equipmentList.value = res.data
  }
}

const editEquipment = (row) => {
  isEdit.value = true
  formData.value = { ...row }
  showAddForm.value = true
}

const deleteEquipment = async (row) => {
  const confirm = await ElMessage.confirm('确定要删除该器材吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  if (confirm) {
    const res = await apiDeleteEquipment(row.id)
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
    if (valid) {
      const res = isEdit.value
        ? await updateEquipment(formData.value.id, formData.value)
        : await createEquipment(formData.value)
      if (res.success) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
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