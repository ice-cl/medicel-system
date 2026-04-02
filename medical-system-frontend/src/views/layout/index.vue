<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">医务管理系统</div>
      <el-menu :default-active="route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
        <!-- 所有角色可见 -->
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <!-- 仅管理员可见 -->
        <el-menu-item index="/user" v-if="isAdmin">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/department" v-if="isAdmin">
          <el-icon><OfficeBuilding /></el-icon>
          <span>科室管理</span>
        </el-menu-item>
        <el-menu-item index="/doctor" v-if="isAdmin">
          <el-icon><UserFilled /></el-icon>
          <span>医生管理</span>
        </el-menu-item>
        <el-menu-item index="/patient" v-if="isAdmin || isDoctor">
          <el-icon><Tickets /></el-icon>
          <span>患者管理</span>
        </el-menu-item>
        <el-menu-item index="/doctor-verify" v-if="isAdmin">
          <el-icon><Check /></el-icon>
          <span>医生审核</span>
        </el-menu-item>

        <!-- 管理员和医生可见 -->
        <el-menu-item index="/schedule" v-if="isAdmin || isDoctor">
          <el-icon><Calendar /></el-icon>
          <span>排班管理</span>
        </el-menu-item>
        <el-menu-item index="/medical-record" v-if="isAdmin || isDoctor">
          <el-icon><Notebook /></el-icon>
          <span>病历管理</span>
        </el-menu-item>
        <el-menu-item index="/prescription" v-if="isAdmin || isDoctor">
          <el-icon><List /></el-icon>
          <span>处方管理</span>
        </el-menu-item>

        <!-- 管理员和患者可见 -->
        <el-menu-item index="/registration" v-if="isAdmin || isPatient">
          <el-icon><Document /></el-icon>
          <span>挂号管理</span>
        </el-menu-item>
        <el-menu-item index="/health-data" v-if="isAdmin || isPatient">
          <el-icon><DataLine /></el-icon>
          <span>健康数据</span>
        </el-menu-item>

        <!-- 所有角色可见 -->
        <el-menu-item index="/consultation">
          <el-icon><ChatDotRound /></el-icon>
          <span>在线咨询</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-right">
          <span>{{ userStore.userInfo.realName }}</span>
          <el-button type="danger" link @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 角色判断
const isAdmin = computed(() => userStore.userInfo.role === 'ADMIN')
const isDoctor = computed(() => userStore.userInfo.role === 'DOCTOR')
const isPatient = computed(() => userStore.userInfo.role === 'PATIENT')

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container { height: 100vh; }
.aside { background-color: #304156; }
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; font-size: 18px; font-weight: bold; }
.header { background: #fff; display: flex; justify-content: flex-end; align-items: center; box-shadow: 0 1px 4px rgba(0,21,41,.08); }
.header-right { display: flex; align-items: center; gap: 15px; }
.main { background: #f0f2f5; padding: 20px; }
</style>
