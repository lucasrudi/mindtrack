<script setup lang="ts">
import { computed } from 'vue'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js'
import type { MoodTrend } from '@/stores/analytics'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
)

const props = defineProps<{
  data: MoodTrend[]
}>()

const chartData = computed(() => ({
  labels: props.data.map((d) => {
    const date = new Date(d.date + 'T00:00:00')
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
  }),
  datasets: [
    {
      label: 'Average Mood',
      data: props.data.map((d) => d.averageMood),
      borderColor: '#6366f1',
      backgroundColor: 'rgba(99, 102, 241, 0.1)',
      fill: true,
      tension: 0.3,
      pointRadius: 4,
      pointHoverRadius: 6,
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
          const point = props.data[context.dataIndex]
          return `${point.entryCount} ${point.entryCount === 1 ? 'entry' : 'entries'}`
        },
      },
    },
  },
  scales: {
    y: {
      min: 1,
      max: 10,
      ticks: { stepSize: 1 },
      title: { display: true, text: 'Mood' },
    },
  },
}
</script>

<template>
  <div class="chart-container">
    <Line v-if="data.length > 0" :data="chartData" :options="chartOptions" />
    <p v-else class="no-data">No mood data for this period</p>
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
