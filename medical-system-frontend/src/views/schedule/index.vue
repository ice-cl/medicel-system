<template>
  <div class="schedule-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>排班管理</span>
          <el-button type="primary" @click="handleAdd">新增排班</el-button>
        </div>
      </template>
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="日期">
          <el-date-picker v-model="query.date" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="科室" v-if="!isDoctor">
          <el-select v-model="query.deptId" clearable>
            <el-option v-for="d in deptList" :key="d.id" :label="d.deptName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="医生">
          <template #default="{ row }">
            {{ getDoctorName(row.doctorId) }}
          </template>
        </el-table-column>
        <el-table-column prop="scheduleDate" label="日期" />
        <el-table-column prop="timePeriod" label="时段">
          <template #default="{ row }">
            {{ timePeriodMap[row.timePeriod] }}
          </template>
        </el-table-column>
        <el-table-column prop="maxPatients" label="最大人数" />
        <el-table-column prop="currentPatients" label="已挂号" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" layout="total, prev, pager, next" @current-change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑排班' : '新增排班'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="科室" prop="deptId">
          <el-select v-model="form.deptId" :disabled="isDoctor" @change="loadDoctorsByDept">
            <el-option v-for="d in deptList" :key="d.id" :label="d.deptName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="医生" prop="doctorId">
          <el-select v-model="form.doctorId" filterable :disabled="isDoctor">
            <el-option
              v-for="d in doctorList"
              :key="d.id"
              :label="`${d.realName || '未知医生'} (${d.doctorCode || d.id})`"
              :value="d.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="scheduleDate">
          <el-date-picker v-model="form.scheduleDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="时段" prop="timePeriod">
          <el-select v-model="form.timePeriod">
            <el-option label="上午" :value="1" />
            <el-option label="下午" :value="2" />
            <el-option label="晚上" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大人数">
          <el-input-number v-model="form.maxPatients" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getSchedulePage, createSchedule, updateSchedule, deleteSchedule } from '@/api/schedule'
import { getDepartmentList } from '@/api/department'
import { getDoctorList, getDoctorByUserId } from '@/api/doctor'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const isDoctor = computed(() => userStore.userInfo.role === 'DOCTOR')

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const deptList = ref([])
const doctorList = ref([])
const allDoctorList = ref([])
const currentDoctor = ref(null)
const timePeriodMap = { 1: '上午', 2: '下午', 3: '晚上' }

const query = reactive({ pageNum: 1, pageSize: 10, date: null, deptId: null })
const form = reactive({ id: null, doctorId: null, deptId: null, scheduleDate: '', timePeriod: 1, maxPatients: 30 })
const rules = {
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  deptId: [{ required: true, message: '请选择科室', trigger: 'change' }],
  scheduleDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  timePeriod: [{ required: true, message: '请选择时段', trigger: 'change' }]
}

const loadDepts = async () => { deptList.value = (await getDepartmentList()).data }
const loadDoctors = async () => {
  const res = await getDoctorList()
  allDoctorList.value = res.data || []
  doctorList.value = allDoctorList.value
}

const loadDoctorsByDept = async (deptId) => {
  if (!deptId) {
    doctorList.value = allDoctorList.value
    return
  }
  const res = await getDoctorList(deptId)
  doctorList.value = res.data || []
  if (!doctorList.value.find(item => item.id === form.doctorId)) {
    form.doctorId = null
  }
}

const getDoctorName = (doctorId) => {
  const doctor = allDoctorList.value.find(item => item.id === doctorId)
  if (!doctor) return doctorId
  return doctor.realName || doctor.doctorCode || doctorId
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...query }
    if (isDoctor.value && currentDoctor.value?.id) {
      params.doctorId = currentDoctor.value.id
      params.deptId = currentDoctor.value.deptId
    }
    const res = await getSchedulePage(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

const handleAdd = () => {
  Object.assign(form, { id: null, doctorId: null, deptId: null, scheduleDate: '', timePeriod: 1, maxPatients: 30 })
  if (isDoctor.value && currentDoctor.value) {
    form.doctorId = currentDoctor.value.id
    form.deptId = currentDoctor.value.deptId
    doctorList.value = [currentDoctor.value]
  } else {
    doctorList.value = allDoctorList.value
  }
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  Object.assign(form, row)
  if (isDoctor.value && currentDoctor.value) {
    form.doctorId = currentDoctor.value.id
    form.deptId = currentDoctor.value.deptId
    doctorList.value = [currentDoctor.value]
  } else {
    await loadDoctorsByDept(row.deptId)
  }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除?', '提示')
  await deleteSchedule(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  form.id ? await updateSchedule(form) : await createSchedule(form)
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

onMounted(async () => {
  if (isDoctor.value) {
    const res = await getDoctorByUserId(userStore.userInfo.userId)
    currentDoctor.value = res.data || null
  }
  await loadDepts()
  await loadDoctors()
  await loadData()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 15px; }
</style>
