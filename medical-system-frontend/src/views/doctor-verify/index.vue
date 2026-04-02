<template>
  <div class="doctor-verify-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>医生审核</span>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="doctorCode" label="工号" width="120" />
        <el-table-column prop="deptId" label="科室ID" width="100" />
        <el-table-column prop="title" label="职称" width="120" />
        <el-table-column prop="specialty" label="专长" />
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="success" link @click="handleVerify(row, 1)">通过</el-button>
            <el-button type="danger" link @click="handleVerify(row, 2)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" layout="total, prev, pager, next" @current-change="loadData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getPendingDoctors, verifyDoctor } from '@/api/doctor'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({ pageNum: 1, pageSize: 10 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPendingDoctors(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

const handleVerify = async (row, status) => {
  const msg = status === 1 ? '确定通过该医生的审核?' : '确定拒绝该医生的申请?'
  await ElMessageBox.confirm(msg, '提示', { type: 'warning' })
  await verifyDoctor(row.id, status)
  ElMessage.success('操作成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
