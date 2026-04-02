<template>
  <div class="consultation-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>在线咨询</span>
          <el-button type="primary" @click="handleAdd">发起咨询</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="患者" v-if="!isPatient">
          <el-select v-model="query.patientId" placeholder="请选择患者" clearable filterable>
            <el-option v-for="p in patientList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="医生" v-if="!isDoctor">
          <el-select v-model="query.doctorId" placeholder="请选择医生" clearable filterable>
            <el-option v-for="d in doctorList" :key="d.id" :label="d.realName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="请选择" clearable>
            <el-option label="进行中" :value="0" />
            <el-option label="已结束" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="患者" width="120">
          <template #default="{ row }">{{ getPatientName(row.patientId) }}</template>
        </el-table-column>
        <el-table-column label="医生" width="120">
          <template #default="{ row }">{{ getDoctorName(row.doctorId) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'info'">
              {{ row.status === 0 ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="openChat(row)">查看消息</el-button>
            <el-button v-if="row.status === 0" type="warning" link @click="handleEnd(row)">结束</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" layout="total, prev, pager, next" @current-change="loadData" />
    </el-card>

    <!-- 发起咨询对话框 -->
    <el-dialog v-model="dialogVisible" title="发起咨询" width="400px">
      <el-form :model="form" ref="formRef" label-width="80px" :rules="rules">
        <el-form-item label="患者" prop="patientId">
          <el-select v-if="!isPatient" v-model="form.patientId" placeholder="请选择患者" filterable style="width: 100%">
            <el-option v-for="p in patientList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
          <span v-else>{{ getPatientName(form.patientId) }}</span>
        </el-form-item>
        <el-form-item label="医生" prop="doctorId">
          <el-select v-if="!isDoctor" v-model="form.doctorId" placeholder="请选择医生" filterable style="width: 100%">
            <el-option v-for="d in doctorList" :key="d.id" :label="d.realName" :value="d.id" />
          </el-select>
          <span v-else>{{ getDoctorName(form.doctorId) }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 聊天对话框 -->
    <el-dialog v-model="chatVisible" title="咨询消息" width="600px">
      <div class="chat-container" ref="chatContainer">
        <div v-for="msg in messages" :key="msg.id"
          :class="['message', isMySelf(msg) ? 'right' : 'left']">
          <div class="role">{{ msg.senderRole === 'PATIENT' ? '患者' : '医生' }}</div>
          <div class="content">{{ msg.content }}</div>
          <div class="time">{{ msg.createTime }}</div>
        </div>
      </div>
      <div class="chat-input" v-if="currentConsultation?.status === 0">
        <el-input v-model="newMessage" placeholder="输入消息" @keyup.enter="sendMsg" />
        <el-button type="primary" @click="sendMsg">发送</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { getConsultationPage, createConsultation, endConsultation,
  getMessagesByConsultation, sendMessage } from '@/api/consultation'
import { getPatientList, getPatientByUserId } from '@/api/patient'
import { getDoctorList, getDoctorByUserId } from '@/api/doctor'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const isPatient = computed(() => userStore.userInfo.role === 'PATIENT')
const isDoctor = computed(() => userStore.userInfo.role === 'DOCTOR')
const isAdmin = computed(() => userStore.userInfo.role === 'ADMIN')

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const chatVisible = ref(false)
const formRef = ref()
const chatContainer = ref()
const messages = ref([])
const newMessage = ref('')
const currentConsultation = ref(null)
const patientList = ref([])
const doctorList = ref([])
const currentPatientId = ref(null)
const currentDoctorId = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10, status: null, patientId: null, doctorId: null })
const form = reactive({ patientId: null, doctorId: null })

const rules = {
  patientId: [{ required: true, message: '请选择患者', trigger: 'change' }],
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }]
}

const getPatientName = (id) => {
  const p = patientList.value.find(item => item.id === id)
  return p ? p.name : id
}

const getDoctorName = (id) => {
  const d = doctorList.value.find(item => item.id === id)
  return d ? d.realName : id
}

// 判断消息是否是当前用户发送的
const isMySelf = (msg) => {
  if (isPatient.value && msg.senderRole === 'PATIENT') {
    return true
  }
  if (isDoctor.value && msg.senderRole === 'DOCTOR') {
    return true
  }
  return false
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
    // 患者只能查看自己的咨询
    if (isPatient.value && currentPatientId.value) {
      params.patientId = currentPatientId.value
    }
    // 医生只能查看自己的咨询
    if (isDoctor.value && currentDoctorId.value) {
      params.doctorId = currentDoctorId.value
    }
    const res = await getConsultationPage(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

const handleAdd = () => {
  Object.assign(form, { patientId: null, doctorId: null })
  // 患者发起咨询自动设置患者ID
  if (isPatient.value && currentPatientId.value) {
    form.patientId = currentPatientId.value
  }
  // 医生发起咨询自动设置医生ID
  if (isDoctor.value && currentDoctorId.value) {
    form.doctorId = currentDoctorId.value
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  await createConsultation(form)
  ElMessage.success('创建成功')
  dialogVisible.value = false
  loadData()
}

const handleEnd = async (row) => {
  await ElMessageBox.confirm('确定结束该咨询?', '提示', { type: 'warning' })
  await endConsultation(row.id)
  ElMessage.success('已结束')
  loadData()
}

const openChat = async (row) => {
  currentConsultation.value = row
  chatVisible.value = true
  const res = await getMessagesByConsultation(row.id)
  messages.value = res.data || []
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

const sendMsg = async () => {
  if (!newMessage.value.trim()) return
  const msgData = {
    consultationId: currentConsultation.value.id,
    content: newMessage.value
  }
  // 根据角色设置发送者
  if (isPatient.value) {
    msgData.senderId = currentPatientId.value
    msgData.senderRole = 'PATIENT'
  } else {
    msgData.senderId = currentDoctorId.value
    msgData.senderRole = 'DOCTOR'
  }
  await sendMessage(msgData)
  newMessage.value = ''
  openChat(currentConsultation.value)
}

onMounted(async () => {
  // 如果是患者，获取当前患者ID
  if (isPatient.value) {
    const res = await getPatientByUserId(userStore.userInfo.userId)
    if (res.data) {
      currentPatientId.value = res.data.id
    }
  }
  // 如果是医生，获取当前医生ID
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
.search-form { margin-bottom: 15px; }
.chat-container { height: 350px; overflow-y: auto; padding: 10px; background: #f5f5f5; }
.message { margin-bottom: 15px; max-width: 70%; }
.message.left { text-align: left; }
.message.right { text-align: right; margin-left: auto; }
.message .role { font-size: 12px; color: #999; }
.message .content { background: #fff; padding: 8px 12px; border-radius: 8px; display: inline-block; }
.message.right .content { background: #409eff; color: #fff; }
.message .time { font-size: 12px; color: #999; margin-top: 4px; }
.chat-input { display: flex; gap: 10px; margin-top: 15px; }
.chat-input .el-input { flex: 1; }
</style>
