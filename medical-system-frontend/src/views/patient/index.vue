<template>
  <div class="patient-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>患者管理</span>
          <el-button v-if="!isDoctor" type="primary" @click="handleAdd">新增患者</el-button>
        </div>
      </template>
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="姓名">
          <el-input v-model="query.name" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="patientCode" label="患者编号" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="gender" label="性别">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="idCard" label="身份证" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="success" link @click="handleAnalyze(row)">AI分析</el-button>
            <el-button v-if="!isDoctor" type="primary" link @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" layout="total, prev, pager, next" @current-change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑' : '新增'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="身份证">
          <el-input v-model="form.idCard" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <AiHealthAnalysisDialog
      v-model="aiDialogVisible"
      :loading="aiLoading"
      :analysis="aiAnalysis"
      title="患者 AI 健康分析"
    />
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted } from 'vue'
import { getPatientPage, createPatient, updatePatient } from '@/api/patient'
import { analyzePatientHealth } from '@/api/aiHealth'
import AiHealthAnalysisDialog from '@/components/AiHealthAnalysisDialog.vue'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const isDoctor = computed(() => userStore.userInfo.role === 'DOCTOR')

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const aiDialogVisible = ref(false)
const aiLoading = ref(false)
const aiAnalysis = ref(null)
const formRef = ref()

const query = reactive({ pageNum: 1, pageSize: 10, name: '' })
const form = reactive({ id: null, name: '', gender: 1, phone: '', idCard: '', address: '' })
const rules = { name: [{ required: true, message: '请输入姓名', trigger: 'blur' }] }

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPatientPage(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

const handleAdd = () => {
  Object.assign(form, { id: null, name: '', gender: 1, phone: '', idCard: '', address: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => { Object.assign(form, row); dialogVisible.value = true }

const handleAnalyze = async (row) => {
  aiDialogVisible.value = true
  aiLoading.value = true
  aiAnalysis.value = null
  try {
    const res = await analyzePatientHealth(row.id)
    aiAnalysis.value = res.data
  } finally {
    aiLoading.value = false
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  form.id ? await updatePatient(form) : await createPatient(form)
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 15px; }
</style>
