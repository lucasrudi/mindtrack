<script setup lang="ts">
import { computed } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js'
import type { ActivityStat } from '@/stores/analytics'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const props = defineProps<{
  data: ActivityStat[]
}>()

function formatType(type: string): string {
  return type
    .replaceAll(/_/g, ' ')
    .toLowerCase()
    .replaceAll(/\b\w/g, (c) => c.toUpperCase())
}

const chartData = computed(() => ({
  labels: props.data.map((d) => formatType(d.activityType)),
  datasets: [
    {
      label: 'Completion Rate (%)',
      data: props.data.map((d) => d.completionRate),
      backgroundColor: '#10b981',
      borderRadius: 4,
    },
  ],
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        afterLabel: (context: { dataIndex: number }) => {
          const stat = props.data[context.dataIndex]
          return `${stat.completedLogs}/${stat.totalLogs} completed`
        },
      },
    },
  },
  scales: {
    y: {
      min: 0,
      max: 100,
      ticks: { callback: (value: string | number) => `${value}%` },
      title: { display: true, text: 'Completion Rate' },
    },
  },
}
</script>

<template>
  <div class="chart-container">
    <Bar v-if="data.length > 0" :data="chartData" :options="chartOptions" />
    <p v-else class="no-data">No activity data for this period</p>
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
