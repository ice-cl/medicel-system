<template>
  <div class="register-container">
    <div class="register-box">
      <h2>用户注册</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="注册类型" prop="registerType">
          <el-radio-group v-model="form.registerType">
            <el-radio label="PATIENT">患者</el-radio>
            <el-radio label="DOCTOR">医生</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>

        <!-- 医生额外信息 -->
        <template v-if="form.registerType === 'DOCTOR'">
          <el-divider>医生信息</el-divider>
          <el-form-item label="科室" prop="deptId">
            <el-select v-model="form.deptId" placeholder="请选择科室">
              <el-option v-for="d in deptList" :key="d.id" :label="d.deptName" :value="d.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="职称">
            <el-input v-model="form.title" placeholder="如: 主任医师" />
          </el-form-item>
          <el-form-item label="专长">
            <el-input v-model="form.specialty" placeholder="请输入专长" />
          </el-form-item>
          <el-form-item label="简介">
            <el-input v-model="form.introduction" type="textarea" :rows="2" />
          </el-form-item>
        </template>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRegister" style="width: 100%">
            注册
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-link type="primary" @click="$router.push('/login')">已有账号？去登录</el-link>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/user'
import { getDepartmentList } from '@/api/department'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const deptList = ref([])

const form = reactive({
  registerType: 'PATIENT',
  username: '', password: '', realName: '', phone: '', email: '', gender: 1,
  deptId: null, title: '', specialty: '', introduction: ''
})

const rules = {
  registerType: [{ required: true, message: '请选择注册类型', trigger: 'change' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

const loadDepts = async () => {
  const res = await getDepartmentList()
  deptList.value = res.data || []
}

const handleRegister = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await register(form)
    ElMessage.success(form.registerType === 'DOCTOR' ? '注册成功，请等待审核' : '注册成功')
    router.push('/login')
  } finally { loading.value = false }
}

onMounted(loadDepts)
</script>

<style scoped>
.register-container {
  min-height: 100vh; display: flex; justify-content: center; align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 20px;
}
.register-box {
  width: 500px; padding: 30px; background: #fff; border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
.register-box h2 { text-align: center; margin-bottom: 20px; color: #333; }
</style>
