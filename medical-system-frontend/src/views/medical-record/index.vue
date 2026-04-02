<template>
  <div class="medical-record-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>病历管理</span>
          <el-button type="primary" @click="handleAdd">新增病历</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="recordNo" label="病历号" />
        <el-table-column label="患者">
          <template #default="{ row }">{{ getPatientName(row.patientId) }}</template>
        </el-table-column>
        <el-table-column label="医生">
          <template #default="{ row }">{{ getDoctorName(row.doctorId) }}</template>
        </el-table-column>
        <el-table-column prop="visitDate" label="就诊时间" />
        <el-table-column prop="diagnosis" label="诊断" show-overflow-tooltip />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" layout="total, prev, pager, next" @current-change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isView ? '查看病历' : (form.id ? '编辑' : '新增')" width="700px">
      <el-form :model="form" ref="formRef" label-width="80px" :disabled="isView">
        <el-form-item label="挂号记录" prop="registrationId" v-if="!form.id">
          <el-select v-model="form.registrationId" filterable placeholder="请选择就诊中的挂号" @change="onRegistrationChange">
            <el-option
              v-for="item in registrationList"
              :key="item.id"
              :label="`${item.regNo} / 患者${item.patientId} / 医生${item.doctorId}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="患者" prop="patientId">
              <el-select v-model="form.patientId" filterable>
                <el-option v-for="p in patientList" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="医生" prop="doctorId">
              <el-select v-model="form.doctorId" filterable placeholder="请选择医生">
                <el-option v-for="d in doctorList" :key="d.id" :label="d.realName" :value="d.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="主诉">
          <el-input v-model="form.chiefComplaint" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="现病史">
          <el-input v-model="form.presentIllness" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="诊断">
          <el-input v-model="form.diagnosis" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="治疗方案">
          <el-input v-model="form.treatmentPlan" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="医嘱">
          <el-input v-model="form.advice" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer v-if="!isView">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMedicalRecordPage, createMedicalRecord, updateMedicalRecord } from '@/api/medicalRecord'
import { getPatientList } from '@/api/patient'
import { getDoctorList } from '@/api/doctor'
import { getRegistrationPage } from '@/api/registration'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isView = ref(false)
const formRef = ref()
const patientList = ref([])
const doctorList = ref([])
const registrationList = ref([])

const query = reactive({ pageNum: 1, pageSize: 10 })
const form = reactive({
  id: null, registrationId: null, patientId: null, doctorId: null,
  chiefComplaint: '', presentIllness: '', diagnosis: '', treatmentPlan: '', advice: ''
})

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

const loadRegistrations = async () => {
  const res = await getRegistrationPage({ pageNum: 1, pageSize: 200, status: 1 })
  registrationList.value = res.data.records || []
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMedicalRecordPage(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

const handleAdd = () => {
  isView.value = false
  loadRegistrations()
  Object.assign(form, { id: null, registrationId: null, patientId: null, doctorId: null,
    chiefComplaint: '', presentIllness: '', diagnosis: '', treatmentPlan: '', advice: '' })
  dialogVisible.value = true
}

const onRegistrationChange = () => {
  const selected = registrationList.value.find(item => item.id === form.registrationId)
  if (selected) {
    form.patientId = selected.patientId
    form.doctorId = selected.doctorId
  }
}

const handleView = (row) => { isView.value = true; Object.assign(form, row); dialogVisible.value = true }
const handleEdit = (row) => { isView.value = false; Object.assign(form, row); dialogVisible.value = true }

const handleSubmit = async () => {
  form.id ? await updateMedicalRecord(form) : await createMedicalRecord(form)
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

onMounted(() => {
  loadPatientList()
  loadDoctorList()
  loadRegistrations()
  loadData()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
