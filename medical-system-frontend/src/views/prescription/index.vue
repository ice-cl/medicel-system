<template>
  <div class="prescription-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>处方管理</span>
          <el-button type="primary" @click="handleAdd">新增处方</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="药品名称">
          <el-input v-model="query.drugName" placeholder="请输入药品名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="recordId" label="病历ID" width="100" />
        <el-table-column prop="drugName" label="药品名称" />
        <el-table-column prop="dosage" label="剂量" width="100" />
        <el-table-column prop="frequency" label="频次" width="120" />
        <el-table-column prop="days" label="天数" width="80" />
        <el-table-column prop="usage" label="用法" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" layout="total, prev, pager, next" @current-change="loadData" style="margin-top: 15px;" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑处方' : '新增处方'" width="500px">
      <el-form :model="form" ref="formRef" label-width="80px" :rules="rules">
        <el-form-item label="病历ID" prop="recordId">
          <el-input v-model.number="form.recordId" />
        </el-form-item>
        <el-form-item label="药品名称" prop="drugName">
          <el-input v-model="form.drugName" />
        </el-form-item>
        <el-form-item label="剂量" prop="dosage">
          <el-input v-model="form.dosage" placeholder="如: 2片" />
        </el-form-item>
        <el-form-item label="频次" prop="frequency">
          <el-input v-model="form.frequency" placeholder="如: 一天三次" />
        </el-form-item>
        <el-form-item label="天数" prop="days">
          <el-input-number v-model="form.days" :min="1" />
        </el-form-item>
        <el-form-item label="用法">
          <el-input v-model="form.usage" placeholder="如: 饭后服用" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
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
import { ref, reactive, onMounted } from 'vue'
import { getPrescriptionPage, createPrescription, updatePrescription, deletePrescription } from '@/api/prescription'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()

const query = reactive({ pageNum: 1, pageSize: 10, drugName: '' })
const form = reactive({
  id: null, recordId: null, drugName: '', dosage: '', frequency: '', days: 7, usage: '', remark: ''
})

const rules = {
  recordId: [{ required: true, message: '请输入病历ID', trigger: 'blur' }],
  drugName: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
  dosage: [{ required: true, message: '请输入剂量', trigger: 'blur' }],
  frequency: [{ required: true, message: '请输入频次', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.drugName) params.drugName = query.drugName
    const res = await getPrescriptionPage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const resetQuery = () => {
  query.drugName = ''
  query.pageNum = 1
  loadData()
}

const handleAdd = () => {
  Object.assign(form, { id: null, recordId: null, drugName: '', dosage: '', frequency: '', days: 7, usage: '', remark: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该处方?', '提示', { type: 'warning' })
  await deletePrescription(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  form.id ? await updatePrescription(form) : await createPrescription(form)
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
