<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    width="760px"
    destroy-on-close
    @close="emit('update:modelValue', false)"
  >
    <div v-loading="loading" class="analysis-dialog">
      <template v-if="analysis">
        <el-descriptions :column="2" border class="analysis-meta">
          <el-descriptions-item label="患者姓名">
            {{ analysis.patientName || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="分析视角">
            {{ audienceLabel }}
          </el-descriptions-item>
          <el-descriptions-item label="指标条数">
            {{ analysis.recordCount ?? 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="最新记录时间">
            {{ analysis.latestRecordTime || '--' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-alert
          :closable="false"
          type="warning"
          show-icon
          :title="analysis.disclaimer || 'AI 分析仅供参考'"
        />

        <div class="analysis-content">{{ analysis.analysis }}</div>
      </template>

      <el-empty v-else description="暂无分析结果" />
    </div>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: 'AI 健康分析'
  },
  analysis: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue'])

const audienceLabel = computed(() => {
  if (props.analysis?.audience === 'CLINICIAN') {
    return '医生视角'
  }
  if (props.analysis?.audience === 'PATIENT') {
    return '患者视角'
  }
  return '--'
})
</script>

<style scoped>
.analysis-dialog {
  min-height: 240px;
}

.analysis-meta {
  margin-bottom: 16px;
}

.analysis-content {
  margin-top: 16px;
  padding: 16px;
  border-radius: 12px;
  background: #f7fafc;
  color: #1f2937;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
