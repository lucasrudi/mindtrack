<script setup lang="ts">
import { computed } from 'vue'
import { Doughnut } from 'vue-chartjs'
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js'
import type { GoalProgress } from '@/stores/analytics'

ChartJS.register(ArcElement, Tooltip, Legend)

const props = defineProps<{
  data: GoalProgress[]
}>()

const statusColors: Record<string, string> = {
  NOT_STARTED: '#94a3b8',
  IN_PROGRESS: '#6366f1',
  COMPLETED: '#10b981',
  PAUSED: '#f59e0b',
  CANCELLED: '#ef4444',
}

const statusLabels: Record<string, string> = {
  NOT_STARTED: 'Not Started',
  IN_PROGRESS: 'In Progress',
  COMPLETED: 'Completed',
  PAUSED: 'Paused',
  CANCELLED: 'Cancelled',
}

const chartData = computed(() => ({
  labels: props.data.map((d) => statusLabels[d.status] || d.status),
  datasets: [
    {
      data: props.data.map((d) => d.count),
      backgroundColor: props.data.map((d) => statusColors[d.status] || '#94a3b8'),
      borderWidth: 0,
    },
  ],
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom' as const,
      labels: {
        padding: 16,
        usePointStyle: true,
        pointStyleWidth: 12,
      },
    },
  },
  cutout: '60%',
}
</script>

<template>
  <div class="chart-container">
    <Doughnut v-if="data.length > 0" :data="chartData" :options="chartOptions" />
    <p v-else class="no-data">No goals yet</p>
  </div>
</template>

<style scoped>
.chart-container {
  position: relative;
  height: 300px;
}

.no-data {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--color-gray-500);
  font-style: italic;
}
</style>
