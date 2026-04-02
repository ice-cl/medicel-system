import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue')
  },
  {
    path: '/',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'department',
        name: 'Department',
        component: () => import('@/views/department/index.vue'),
        meta: { title: '科室管理' }
      },
      {
        path: 'doctor',
        name: 'Doctor',
        component: () => import('@/views/doctor/index.vue'),
        meta: { title: '医生管理' }
      },
      {
        path: 'doctor-verify',
        name: 'DoctorVerify',
        component: () => import('@/views/doctor-verify/index.vue'),
        meta: { title: '医生审核' }
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: () => import('@/views/schedule/index.vue'),
        meta: { title: '排班管理' }
      },
      {
        path: 'patient',
        name: 'Patient',
        component: () => import('@/views/patient/index.vue'),
        meta: { title: '患者管理' }
      },
      {
        path: 'registration',
        name: 'Registration',
        component: () => import('@/views/registration/index.vue'),
        meta: { title: '挂号管理' }
      },
      {
        path: 'medical-record',
        name: 'MedicalRecord',
        component: () => import('@/views/medical-record/index.vue'),
        meta: { title: '病历管理' }
      },
      {
        path: 'prescription',
        name: 'Prescription',
        component: () => import('@/views/prescription/index.vue'),
        meta: { title: '处方管理' }
      },
      {
        path: 'health-data',
        name: 'HealthData',
        component: () => import('@/views/health-data/index.vue'),
        meta: { title: '健康数据' }
      },
      {
        path: 'consultation',
        name: 'Consultation',
        component: () => import('@/views/consultation/index.vue'),
        meta: { title: '在线咨询' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && to.path !== '/register' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
