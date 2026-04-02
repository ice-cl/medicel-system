<template>
  <div v-loading="loading" class="dashboard">
    <el-card class="welcome-card">
      <template #header>欢迎使用医务管理系统</template>
      <p>当前登录用户：{{ userStore.userInfo.realName }}</p>
      <p>角色：{{ roleMap[userStore.userInfo.role] }}</p>
    </el-card>

    <template v-if="isPatient">
      <el-row :gutter="20">
        <el-col :xs="24" :lg="8" class="section-col">
          <el-card class="patient-info-card">
            <template #header>当前患者信息</template>
            <div class="patient-profile">
              <div class="patient-profile__avatar">{{ patientNameInitial }}</div>
              <div class="patient-profile__content">
                <div class="patient-profile__name-row">
                  <span class="patient-profile__name">{{ patientName }}</span>
                  <el-tag effect="dark" round type="success">{{ patientGender }}</el-tag>
                </div>
                <div class="patient-profile__code">患者编号：{{ patientInfo?.patientCode || '--' }}</div>
                <div class="patient-profile__time">最近健康记录：{{ latestTrendTime }}</div>
              </div>
            </div>

            <div class="patient-summary-grid">
              <div v-for="item in patientSummary" :key="item.label" class="patient-summary-item">
                <div class="patient-summary-item__label">{{ item.label }}</div>
                <div class="patient-summary-item__value">{{ item.value }}</div>
              </div>
            </div>

            <div class="patient-detail-list">
              <div v-for="item in patientDetails" :key="item.label" class="patient-detail-item">
                <span class="patient-detail-item__label">{{ item.label }}</span>
                <span class="patient-detail-item__value">{{ item.value }}</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="16" class="section-col">
          <el-card class="patient-trend-card">
            <template #header>健康数据趋势</template>
            <div class="patient-trend-meta">
              <span>已接入当前患者真实健康记录</span>
              <span>{{ patientTrend.length }} 条数据 / {{ patientTrendTypeCount }} 项指标</span>
            </div>
            <div v-if="patientTrend.length" ref="patientTrendRef" class="patient-chart"></div>
            <el-empty v-else description="暂无健康数据" />
          </el-card>
        </el-col>
      </el-row>
    </template>

    <el-row v-else-if="dashboardCards.length" :gutter="20">
      <el-col
        v-for="card in dashboardCards"
        :key="card.key"
        :xs="24"
        :sm="12"
        :lg="6"
        class="section-col"
      >
        <el-card shadow="hover" class="chart-card" :style="getCardStyle(card.color)">
          <div class="chart-card__head">
            <div class="chart-card__title-group">
              <div class="chart-card__icon">
                <el-icon :size="18">
                  <component :is="card.icon" />
                </el-icon>
              </div>
              <div class="chart-card__info">
                <div class="stat-title">{{ card.title }}</div>
                <div class="stat-value">{{ formatStatValue(card.value) }}</div>
              </div>
            </div>
            <div class="chart-card__ratio">{{ formatPercent(card.ratio) }}</div>
          </div>
          <div class="chart-card__chart">
            <div :ref="el => setChartRef(el, card.key)" class="stat-chart"></div>
          </div>
          <div class="chart-card__footer">
            <span>当前看板占比</span>
            <span>{{ formatPercent(card.ratio) }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import {
  Document,
  OfficeBuilding,
  Tickets,
  UserFilled
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getHealthDataTrend } from '@/api/healthData'
import { getPatientByUserId } from '@/api/patient'
import { getStatistics } from '@/api/statistics'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const roleMap = { ADMIN: '管理员', DOCTOR: '医生', NURSE: '护士', PATIENT: '患者' }
const genderMap = { 1: '男', 2: '女' }
const healthTypeMap = {
  BLOOD_PRESSURE_HIGH: '收缩压',
  BLOOD_PRESSURE_LOW: '舒张压',
  BLOOD_SUGAR: '血糖',
  WEIGHT: '体重',
  HEART_RATE: '心率'
}

const loading = ref(false)
const statistics = ref({})
const patientInfo = ref(null)
const patientTrend = ref([])
const patientTrendRef = ref()
const chartRefs = new Map()
const chartInstances = new Map()
let patientTrendInstance = null

const isPatient = computed(() => userStore.userInfo.role === 'PATIENT')
const patientName = computed(() => patientInfo.value?.name || userStore.userInfo.realName || '--')
const patientNameInitial = computed(() => patientName.value?.slice(0, 1) || 'P')
const patientGender = computed(() => genderMap[patientInfo.value?.gender] || '--')
const latestTrendTime = computed(() => {
  if (!patientTrend.value.length) return '--'
  const latestRecord = [...patientTrend.value]
    .sort((a, b) => String(b.recordTime).localeCompare(String(a.recordTime)))[0]
  return formatRecordTime(latestRecord?.recordTime) || '--'
})
const patientTrendTypeCount = computed(() => new Set(patientTrend.value.map(item => item.type)).size)
const patientSummary = computed(() => [
  { label: '健康记录', value: `${patientTrend.value.length} 条` },
  { label: '指标类型', value: `${patientTrendTypeCount.value} 项` },
  { label: '联系电话', value: patientInfo.value?.phone || '--' },
  { label: '联系地址', value: patientInfo.value?.address || '--' }
])
const patientDetails = computed(() => [
  { label: '姓名', value: patientName.value },
  { label: '性别', value: patientGender.value },
  { label: '患者编号', value: patientInfo.value?.patientCode || '--' },
  { label: '最近健康记录', value: latestTrendTime.value }
])

const managementCards = [
  { key: 'todayRegistrations', title: '今日挂号', icon: Document, color: '#409EFF' },
  { key: 'totalRegistrations', title: '累计挂号', icon: Tickets, color: '#67C23A' },
  { key: 'totalDoctors', title: '在岗医生', icon: UserFilled, color: '#E6A23C' },
  { key: 'totalDepartments', title: '科室总数', icon: OfficeBuilding, color: '#F56C6C' }
]

const rawDashboardCards = computed(() => managementCards.map(card => ({
  ...card,
  value: Number(statistics.value[card.key] ?? 0)
})))

const dashboardMaxValue = computed(() => Math.max(...rawDashboardCards.value.map(card => card.value), 1))

const dashboardCards = computed(() => rawDashboardCards.value.map(card => ({
  ...card,
  ratio: card.value / dashboardMaxValue.value
})))

const loadManagementDashboard = async () => {
  loading.value = true
  try {
    const { data } = await getStatistics()
    statistics.value = data || {}
  } finally {
    loading.value = false
  }
}

const loadPatientDashboard = async () => {
  loading.value = true
  try {
    const patientRes = await getPatientByUserId(userStore.userInfo.userId)
    patientInfo.value = patientRes.data || null

    if (!patientInfo.value?.id) {
      patientTrend.value = []
      return
    }

    const trendRes = await getHealthDataTrend({ patientId: patientInfo.value.id })
    patientTrend.value = trendRes.data || []
  } finally {
    loading.value = false
  }
}

const formatStatValue = value => Number(value || 0)
const formatPercent = value => `${Math.round((Number(value) || 0) * 100)}%`

const formatRecordTime = value => {
  if (!value) return ''
  return String(value).replace('T', ' ')
}

const hexToRgba = (hex, alpha) => {
  const normalized = hex.replace('#', '')
  const fullHex = normalized.length === 3
    ? normalized.split('').map(char => char + char).join('')
    : normalized
  const red = Number.parseInt(fullHex.slice(0, 2), 16)
  const green = Number.parseInt(fullHex.slice(2, 4), 16)
  const blue = Number.parseInt(fullHex.slice(4, 6), 16)
  return `rgba(${red}, ${green}, ${blue}, ${alpha})`
}

const getCardStyle = color => ({
  '--card-accent': color,
  '--card-accent-soft': hexToRgba(color, 0.12),
  '--card-accent-fade': hexToRgba(color, 0.04),
  '--card-accent-light': hexToRgba(color, 0.55)
})

const setChartRef = (el, key) => {
  if (el) {
    chartRefs.set(key, el)
    return
  }

  chartRefs.delete(key)
  const instance = chartInstances.get(key)
  if (instance) {
    instance.dispose()
    chartInstances.delete(key)
  }
}

const createManagementChartOption = card => {
  const baseValue = Math.max(dashboardMaxValue.value, 1)
  const remainValue = Math.max(baseValue - card.value, 0)

  return {
    animationDuration: 500,
    animationEasing: 'cubicOut',
    series: [
      {
        type: 'pie',
        radius: ['70%', '88%'],
        center: ['50%', '50%'],
        startAngle: 90,
        silent: true,
        label: { show: false },
        data: [
          {
            value: card.value || 0.001,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                { offset: 0, color: card.color },
                { offset: 1, color: hexToRgba(card.color, 0.55) }
              ])
            }
          },
          {
            value: remainValue || 0.001,
            itemStyle: {
              color: hexToRgba(card.color, 0.12)
            }
          }
        ]
      }
    ],
    graphic: [
      {
        type: 'text',
        left: 'center',
        top: '40%',
        style: {
          text: formatPercent(card.ratio),
          fill: '#1f2937',
          fontSize: 18,
          fontWeight: 700
        }
      },
      {
        type: 'text',
        left: 'center',
        top: '56%',
        style: {
          text: '相对占比',
          fill: '#6b7280',
          fontSize: 11
        }
      }
    ]
  }
}

const createPatientTrendOption = () => {
  const sortedTrend = [...patientTrend.value].sort((a, b) => String(a.recordTime).localeCompare(String(b.recordTime)))
  const timeAxis = [...new Set(sortedTrend.map(item => formatRecordTime(item.recordTime)))]
  const seriesMap = new Map()

  sortedTrend.forEach(item => {
    const seriesName = healthTypeMap[item.type] || item.type
    if (!seriesMap.has(seriesName)) {
      seriesMap.set(seriesName, new Map())
    }
    seriesMap.get(seriesName).set(formatRecordTime(item.recordTime), Number(item.value))
  })

  return {
    color: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#8E44AD'],
    tooltip: { trigger: 'axis' },
    legend: {
      top: 0,
      textStyle: { color: '#4b5563' }
    },
    grid: {
      left: 40,
      right: 24,
      top: 48,
      bottom: 32
    },
    xAxis: {
      type: 'category',
      data: timeAxis,
      boundaryGap: false,
      axisLabel: {
        color: '#6b7280',
        formatter: value => String(value).slice(5, 16)
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#6b7280' },
      splitLine: {
        lineStyle: { color: '#e5e7eb' }
      }
    },
    series: [...seriesMap.entries()].map(([name, valueMap]) => ({
      name,
      type: 'line',
      smooth: true,
      showSymbol: true,
      showAllSymbol: 'auto',
      connectNulls: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        width: 3
      },
      areaStyle: {
        opacity: 0.08
      },
      emphasis: {
        focus: 'series'
      },
      data: timeAxis.map(time => valueMap.get(time) ?? null)
    }))
  }
}

const renderManagementCharts = async () => {
  await nextTick()
  const activeKeys = new Set(dashboardCards.value.map(card => card.key))

  chartInstances.forEach((instance, key) => {
    if (!activeKeys.has(key)) {
      instance.dispose()
      chartInstances.delete(key)
    }
  })

  dashboardCards.value.forEach(card => {
    const chartEl = chartRefs.get(card.key)
    if (!chartEl) return

    let instance = chartInstances.get(card.key)
    if (!instance) {
      instance = echarts.init(chartEl)
      chartInstances.set(card.key, instance)
    }

    instance.setOption(createManagementChartOption(card), true)
    instance.resize()
  })
}

const renderPatientTrendChart = async () => {
  await nextTick()

  if (!isPatient.value || !patientTrendRef.value || !patientTrend.value.length) {
    if (patientTrendInstance) {
      patientTrendInstance.dispose()
      patientTrendInstance = null
    }
    return
  }

  if (!patientTrendInstance) {
    patientTrendInstance = echarts.init(patientTrendRef.value)
  }

  patientTrendInstance.setOption(createPatientTrendOption(), true)
  patientTrendInstance.resize()
}

const handleResize = () => {
  chartInstances.forEach(instance => instance.resize())
  if (patientTrendInstance) {
    patientTrendInstance.resize()
  }
}

onMounted(() => {
  if (isPatient.value) {
    loadPatientDashboard()
  } else {
    loadManagementDashboard()
  }
  window.addEventListener('resize', handleResize)
})

watch(dashboardCards, () => {
  if (!isPatient.value) {
    renderManagementCharts()
  }
}, { deep: true })

watch(patientTrend, () => {
  if (isPatient.value) {
    renderPatientTrendChart()
  }
}, { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstances.forEach(instance => instance.dispose())
  chartInstances.clear()

  if (patientTrendInstance) {
    patientTrendInstance.dispose()
    patientTrendInstance = null
  }
})
</script>

<style scoped>
.dashboard {
  min-height: 100%;
}

.welcome-card {
  margin-bottom: 20px;
}

.section-col {
  margin-bottom: 20px;
}

.patient-info-card,
.patient-trend-card {
  height: 100%;
}

.patient-profile {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 20px;
  border-radius: 20px;
  background:
    radial-gradient(circle at top right, rgba(14, 165, 233, 0.16), transparent 34%),
    linear-gradient(135deg, #eff6ff, #f0fdfa);
}

.patient-profile__avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 20px;
  background: linear-gradient(135deg, #0284c7, #22c55e);
  color: #fff;
  font-size: 28px;
  font-weight: 700;
}

.patient-profile__content {
  min-width: 0;
  flex: 1;
}

.patient-profile__name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.patient-profile__name {
  color: #0f172a;
  font-size: 24px;
  font-weight: 700;
}

.patient-profile__code,
.patient-profile__time {
  margin-top: 8px;
  color: #475569;
  font-size: 13px;
}

.patient-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.patient-summary-item {
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #fff;
}

.patient-summary-item__label {
  color: #64748b;
  font-size: 12px;
}

.patient-summary-item__value {
  margin-top: 8px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 600;
  word-break: break-all;
}

.patient-detail-list {
  margin-top: 18px;
  padding: 8px 0 0;
}

.patient-detail-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;
  border-bottom: 1px solid #eef2f7;
}

.patient-detail-item:last-child {
  border-bottom: 0;
  padding-bottom: 0;
}

.patient-detail-item__label {
  color: #6b7280;
}

.patient-detail-item__value {
  max-width: 60%;
  color: #111827;
  text-align: right;
  word-break: break-all;
}

.patient-trend-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  color: #64748b;
  font-size: 13px;
  flex-wrap: wrap;
}

.patient-chart {
  height: 360px;
}

.chart-card {
  position: relative;
  overflow: hidden;
  border: 1px solid var(--card-accent-soft);
  background:
    radial-gradient(circle at top right, var(--card-accent-soft), transparent 36%),
    linear-gradient(180deg, #ffffff, var(--card-accent-fade));
}

.chart-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.chart-card__title-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chart-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  color: #fff;
  background: linear-gradient(135deg, var(--card-accent), var(--card-accent-light));
}

.chart-card__info {
  min-width: 0;
}

.chart-card__ratio {
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: var(--card-accent);
  background: var(--card-accent-soft);
}

.chart-card__chart {
  margin-top: 14px;
}

.stat-chart {
  height: 140px;
}

.chart-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  color: #6b7280;
  font-size: 12px;
}

.stat-title {
  color: #6b7280;
  font-size: 13px;
}

.stat-value {
  margin-top: 6px;
  font-size: 30px;
  font-weight: 700;
  color: #1f2937;
}

:deep(.chart-card .el-card__body) {
  padding: 18px;
}
</style>
