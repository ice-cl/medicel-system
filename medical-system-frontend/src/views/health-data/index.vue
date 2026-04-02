<template>
  <div class="health-data-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>健康数据管理</span>
          <div class="header-actions">
            <el-button v-if="isPatient" type="success" @click="handleAiAnalyze">AI健康分析</el-button>
            <el-button type="primary" @click="handleAdd">录入数据</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="患者" v-if="!isPatient">
          <el-select v-model="query.patientId" placeholder="请选择患者" clearable filterable>
            <el-option v-for="p in patientList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据类型">
          <el-select v-model="query.type" placeholder="请选择" clearable>
            <el-option label="收缩压" value="BLOOD_PRESSURE_HIGH" />
            <el-option label="舒张压" value="BLOOD_PRESSURE_LOW" />
            <el-option label="血糖" value="BLOOD_SUGAR" />
            <el-option label="体重" value="WEIGHT" />
            <el-option label="心率" value="HEART_RATE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="showTrend">查看趋势</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="患者" width="120">
          <template #default="{ row }">{{ getPatientName(row.patientId) }}</template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">{{ typeMap[row.type] || row.type }}</template>
        </el-table-column>
        <el-table-column prop="value" label="数值" width="100" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="recordTime" label="记录时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" layout="total, prev, pager, next" @current-change="loadData" />
    </el-card>

    <!-- 录入对话框 -->
    <el-dialog v-model="dialogVisible" title="录入健康数据" width="450px">
      <el-form :model="form" ref="formRef" label-width="80px" :rules="rules">
        <el-form-item label="患者" prop="patientId">
          <el-select v-if="!isPatient" v-model="form.patientId" placeholder="请选择患者" filterable style="width: 100%">
            <el-option v-for="p in patientList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
          <span v-else>{{ getPatientName(form.patientId) }}</span>
        </el-form-item>
        <el-form-item label="数据类型" prop="type">
          <el-select v-model="form.type" @change="onTypeChange">
            <el-option label="收缩压" value="BLOOD_PRESSURE_HIGH" />
            <el-option label="舒张压" value="BLOOD_PRESSURE_LOW" />
            <el-option label="血糖" value="BLOOD_SUGAR" />
            <el-option label="体重" value="WEIGHT" />
            <el-option label="心率" value="HEART_RATE" />
          </el-select>
        </el-form-item>
        <el-form-item label="数值" prop="value">
          <el-input-number v-model="form.value" :precision="2" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" disabled />
        </el-form-item>
        <el-form-item label="记录时间" prop="recordTime">
          <el-date-picker v-model="form.recordTime" type="datetime" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 趋势图对话框 -->
    <el-dialog v-model="trendVisible" title="健康数据趋势" width="700px">
      <div ref="chartRef" style="height: 400px;"></div>
    </el-dialog>

    <AiHealthAnalysisDialog
      v-model="aiDialogVisible"
      :loading="aiLoading"
      :analysis="aiAnalysis"
      title="AI 健康分析"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { getHealthDataPage, getHealthDataTrend, createHealthData, deleteHealthData } from '@/api/healthData'
import { analyzeMyHealth } from '@/api/aiHealth'
import { getPatientList, getPatientByUserId } from '@/api/patient'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import AiHealthAnalysisDialog from '@/components/AiHealthAnalysisDialog.vue'

const userStore = useUserStore()
const isPatient = computed(() => userStore.userInfo.role === 'PATIENT')

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const trendVisible = ref(false)
const aiDialogVisible = ref(false)
const aiLoading = ref(false)
const aiAnalysis = ref(null)
const formRef = ref()
const chartRef = ref()
const patientList = ref([])
const currentPatientId = ref(null)
let chartInstance = null

const typeMap = {
  BLOOD_PRESSURE_HIGH: '收缩压', BLOOD_PRESSURE_LOW: '舒张压',
  BLOOD_SUGAR: '血糖', WEIGHT: '体重', HEART_RATE: '心率'
}
const unitMap = {
  BLOOD_PRESSURE_HIGH: 'mmHg', BLOOD_PRESSURE_LOW: 'mmHg',
  BLOOD_SUGAR: 'mmol/L', WEIGHT: 'kg', HEART_RATE: 'bpm'
}

const query = reactive({ pageNum: 1, pageSize: 10, patientId: null, type: null })
const form = reactive({ patientId: null, type: '', value: 0, unit: '', recordTime: new Date() })

const rules = {
  patientId: [{ required: true, message: '请选择患者', trigger: 'change' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  value: [{ required: true, message: '请输入数值', trigger: 'blur' }]
}

const getPatientName = (id) => {
  const p = patientList.value.find(item => item.id === id)
  return p ? p.name : id
}

const loadPatientList = async () => {
  const res = await getPatientList()
  patientList.value = res.data || []
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...query }
    // 患者只能查看自己的健康数据
    if (isPatient.value && currentPatientId.value) {
      params.patientId = currentPatientId.value
    }
    const res = await getHealthDataPage(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

const handleAdd = () => {
  Object.assign(form, { patientId: null, type: '', value: 0, unit: '', recordTime: new Date() })
  // 患者只能为自己录入数据
  if (isPatient.value && currentPatientId.value) {
    form.patientId = currentPatientId.value
  }
  dialogVisible.value = true
}

const handleAiAnalyze = async () => {
  aiDialogVisible.value = true
  aiLoading.value = true
  aiAnalysis.value = null
  try {
    const res = await analyzeMyHealth()
    aiAnalysis.value = res.data
  } finally {
    aiLoading.value = false
  }
}

const onTypeChange = (val) => { form.unit = unitMap[val] || '' }

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除?', '提示', { type: 'warning' })
  await deleteHealthData(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  await createHealthData(form)
  ElMessage.success('录入成功')
  dialogVisible.value = false
  loadData()
}

const showTrend = async () => {
  if (!query.patientId) { ElMessage.warning('请选择患者'); return }
  trendVisible.value = true
  await nextTick()
  const res = await getHealthDataTrend({ patientId: query.patientId, type: query.type })
  const data = res.data || []
  if (!chartInstance) chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(d => d.recordTime) },
    yAxis: { type: 'value' },
    series: [{ data: data.map(d => d.value), type: 'line', smooth: true }]
  })
}

onMounted(async () => {
  // 如果是患者，先获取当前患者ID
  if (isPatient.value) {
    const res = await getPatientByUserId(userStore.userInfo.userId)
    if (res.data) {
      currentPatientId.value = res.data.id
      query.patientId = res.data.id
    }
  }
  loadPatientList()
  loadData()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 12px; }
.search-form { margin-bottom: 15px; }
</style>
