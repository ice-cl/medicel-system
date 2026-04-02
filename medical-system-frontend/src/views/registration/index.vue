<template>
  <div class="registration-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>挂号管理</span>
          <el-button type="primary" @click="handleAdd">新增挂号</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="regNo" label="挂号单号" />
        <el-table-column label="患者">
          <template #default="{ row }">{{ getPatientName(row.patientId) }}</template>
        </el-table-column>
        <el-table-column label="医生">
          <template #default="{ row }">{{ getDoctorName(row.doctorId) }}</template>
        </el-table-column>
        <el-table-column prop="regDate" label="日期" />
        <el-table-column prop="timePeriod" label="时段">
          <template #default="{ row }">
            {{ { 1: '上午', 2: '下午', 3: '晚上' }[row.timePeriod] }}
          </template>
        </el-table-column>
        <el-table-column prop="queueNo" label="排队号" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="statusType[row.status]">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button v-if="isDoctor && row.status === 0" type="primary" link @click="handleStartVisit(row)">开始接诊</el-button>
            <el-button v-if="isDoctor && row.status === 1" type="success" link @click="handleFinishVisit(row)">完成就诊</el-button>
            <el-button v-if="row.status === 0" type="danger" link @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" layout="total, prev, pager, next" @current-change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增挂号" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="患者" prop="patientId">
          <el-select v-if="!isPatient" v-model="form.patientId" filterable>
            <el-option v-for="p in patientList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
          <span v-else>{{ getPatientName(form.patientId) }}</span>
        </el-form-item>
        <el-form-item label="科室" prop="deptId">
          <el-select v-model="form.deptId" @change="loadSchedules">
            <el-option v-for="d in deptList" :key="d.id" :label="d.deptName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD" @change="loadSchedules" />
        </el-form-item>
        <el-form-item label="排班" prop="scheduleId">
          <el-select v-model="form.scheduleId" @change="selectSchedule">
            <el-option v-for="s in scheduleList" :key="s.id"
              :label="`${getDoctorName(s.doctorId)} - ${{ 1: '上午', 2: '下午', 3: '晚上' }[s.timePeriod]}`" :value="s.id" />
          </el-select>
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
import { getRegistrationPage, createRegistration, cancelRegistration, updateRegistrationStatus } from '@/api/registration'
import { getDepartmentList } from '@/api/department'
import { getPatientPage, getPatientList, getPatientByUserId } from '@/api/patient'
import { getAvailableSchedules } from '@/api/schedule'
import { getDoctorList, getDoctorByUserId } from '@/api/doctor'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const isPatient = computed(() => userStore.userInfo.role === 'PATIENT')
const isDoctor = computed(() => userStore.userInfo.role === 'DOCTOR')

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const deptList = ref([])
const patientList = ref([])
const doctorList = ref([])
const scheduleList = ref([])
const currentPatientId = ref(null)
const currentDoctorId = ref(null)

const statusMap = { 0: '待就诊', 1: '就诊中', 2: '已完成', 3: '已取消' }
const statusType = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }

const query = reactive({ pageNum: 1, pageSize: 10 })
const form = reactive({ patientId: null, deptId: null, date: '', scheduleId: null, doctorId: null })
const rules = {
  patientId: [{ required: true, message: '请选择患者', trigger: 'change' }],
  scheduleId: [{ required: true, message: '请选择排班', trigger: 'change' }]
}

const getPatientName = (id) => {
  const p = patientList.value.find(item => item.id === id)
  return p ? p.name : id
}

const getDoctorName = (id) => {
  const d = doctorList.value.find(item => item.id === id)
  return d ? d.realName : id
}

const loadPatientList = async () => {
  const res = await getPatientList()
  patientList.value = res.data || []
}

const loadDoctorList = async () => {
  const res = await getDoctorList()
  doctorList.value = res.data || []
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...query }
    // 患者只能查看自己的挂号记录
    if (isPatient.value && currentPatientId.value) {
      params.patientId = currentPatientId.value
    }
    if (isDoctor.value && currentDoctorId.value) {
      params.doctorId = currentDoctorId.value
    }
    const res = await getRegistrationPage(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

const loadSchedules = async () => {
  if (form.deptId && form.date) {
    const res = await getAvailableSchedules({ deptId: form.deptId, date: form.date })
    scheduleList.value = res.data
  }
}

const selectSchedule = () => {
  const s = scheduleList.value.find(x => x.id === form.scheduleId)
  if (s) form.doctorId = s.doctorId
}

const handleAdd = async () => {
  Object.assign(form, { patientId: null, deptId: null, date: '', scheduleId: null, doctorId: null })
  deptList.value = (await getDepartmentList()).data
  // 患者只能为自己挂号
  if (isPatient.value && currentPatientId.value) {
    form.patientId = currentPatientId.value
  } else {
    patientList.value = (await getPatientPage({ pageNum: 1, pageSize: 100 })).data.records
  }
  dialogVisible.value = true
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm('确定取消?', '提示')
  await cancelRegistration(row.id)
  ElMessage.success('取消成功')
  loadData()
}

const handleStartVisit = async (row) => {
  await updateRegistrationStatus(row.id, 1)
  ElMessage.success('已开始接诊')
  loadData()
}

const handleFinishVisit = async (row) => {
  await updateRegistrationStatus(row.id, 2)
  ElMessage.success('已完成就诊')
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  await createRegistration(form)
  ElMessage.success('挂号成功')
  dialogVisible.value = false
  loadData()
}

onMounted(async () => {
  // 如果是患者，先获取当前患者ID
  if (isPatient.value) {
    const res = await getPatientByUserId(userStore.userInfo.userId)
    if (res.data) {
      currentPatientId.value = res.data.id
    }
  }
  if (isDoctor.value) {
    const res = await getDoctorByUserId(userStore.userInfo.userId)
    if (res.data) {
      currentDoctorId.value = res.data.id
    }
  }
  loadPatientList()
  loadDoctorList()
  loadData()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
