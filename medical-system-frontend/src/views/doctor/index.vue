<template>
  <div class="doctor-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>医生管理</span>
          <el-button type="primary" @click="handleAdd">新增医生</el-button>
        </div>
      </template>
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="科室">
          <el-select v-model="query.deptId" placeholder="请选择" clearable>
            <el-option v-for="d in deptList" :key="d.id" :label="d.deptName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="doctorCode" label="工号" />
        <el-table-column prop="deptId" label="科室" />
        <el-table-column prop="title" label="职称" />
        <el-table-column prop="consultationFee" label="挂号费" />
        <el-table-column prop="isExpert" label="专家">
          <template #default="{ row }">
            <el-tag :type="row.isExpert === 1 ? 'success' : 'info'">
              {{ row.isExpert === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑医生' : '新增医生'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入医生姓名" />
        </el-form-item>
        <el-form-item label="工号" prop="doctorCode">
          <el-input v-model="form.doctorCode" />
        </el-form-item>
        <el-form-item label="科室" prop="deptId">
          <el-select v-model="form.deptId">
            <el-option v-for="d in deptList" :key="d.id" :label="d.deptName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="职称">
          <el-select v-model="form.title">
            <el-option label="主任医师" value="主任医师" />
            <el-option label="副主任医师" value="副主任医师" />
            <el-option label="主治医师" value="主治医师" />
            <el-option label="住院医师" value="住院医师" />
          </el-select>
        </el-form-item>
        <el-form-item label="挂号费">
          <el-input-number v-model="form.consultationFee" :min="0" />
        </el-form-item>
        <el-form-item label="是否专家">
          <el-switch v-model="form.isExpert" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="专长">
          <el-input v-model="form.specialty" type="textarea" />
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
import { getDoctorPage, createDoctor, updateDoctor, deleteDoctor } from '@/api/doctor'
import { getDepartmentList } from '@/api/department'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const deptList = ref([])

const query = reactive({ pageNum: 1, pageSize: 10, deptId: null })
const form = reactive({ id: null, realName: '', doctorCode: '', deptId: null, title: '', consultationFee: 0, isExpert: 0, specialty: '' })
const rules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  doctorCode: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择科室', trigger: 'change' }]
}

const loadDepts = async () => {
  const res = await getDepartmentList()
  deptList.value = res.data
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDoctorPage(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

const handleAdd = () => {
  Object.assign(form, { id: null, realName: '', doctorCode: '', deptId: null, title: '', consultationFee: 0, isExpert: 0, specialty: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => { Object.assign(form, row); dialogVisible.value = true }

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除?', '提示')
  await deleteDoctor(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  form.id ? await updateDoctor(form) : await createDoctor(form)
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

onMounted(() => { loadDepts(); loadData() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 15px; }
</style>
