<template>
  <el-card
    shadow="never"
    class="health-score-gauge"
    :body-style="{ padding: '15px' }"
  >
    <div
      slot="header"
      class="health-score-gauge__header"
    >
      <span>{{ title }}</span>
    </div>
    <div
      ref="gaugeChart"
      class="health-score-gauge__chart"
      :style="{ height: '140px' }"
    />
  </el-card>
</template>

<script>
// Import only the necessary modules from echarts for better performance
import { use, init } from 'echarts/core';
import { GaugeChart } from 'echarts/charts';
import { CanvasRenderer } from 'echarts/renderers';
import debounce from 'lodash/debounce';

// Register the required components
use([GaugeChart, CanvasRenderer]);

export default {
  name: 'HealthScoreGauge',
  props: {
    title: {
      type: String,
      required: true
    },
    score: {
      type: Number,
      required: true,
      validator: value => value >= 0 && value <= 5
    },
    colors: {
      type: Array,
      default: () => [
        { value: 0, color: '#67e0e3' },
        { value: 2, color: '#37a2da' },
        { value: 4, color: '#fd666d' },
      ]
    },
    chartHeight: {
      type: Number,
      default: 180
    },
    animationDuration: {
      type: Number,
      default: 1000
    }
  },
  data() {
    return {
      chart: null,
      resizeHandler: null
    };
  },
  computed: {
    chartOptions() {
      return {
        series: [
          {
            type: 'gauge',
            min: 0,
            max: 5,
            splitNumber: 5,
            startAngle: 180,
            endAngle: 0,
            radius: '80%',  // Reduced to make room for the score on the right
            center: ['40%', '50%'],  // Shifted left to make room for score
            axisLine: {
              lineStyle: {
                width: 20,
                color: [
                  [0.25, '#FF4949'], // Strong Red (0-1.25)
                  [0.5, '#F56C6C'],  // Light Red (1.25-2.5)
                  [0.75, '#E6A23C'], // Yellow (2.5-3.75)
                  [1, '#67C23A']     // Green (3.75-5)
                ]
              }
            },
            pointer: {
              icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
              length: '12%',
              width: 4,
              offsetCenter: [0, '-60%'],
              itemStyle: {
                color: 'auto'
              }
            },
            axisTick: {
              show: true,
              length: 12,
              lineStyle: {
                color: 'auto',
                width: 1
              }
            },
            splitLine: {
              length: 20,
              lineStyle: {
                color: 'auto',
                width: 2
              }
            },
            axisLabel: {
              show: false
            },
            detail: {
              valueAnimation: true,
              formatter: '{value}',
              color: '#303133',
              fontSize: 36,
              fontWeight: 'bold',
              offsetCenter: ['100%', '0%'],  // Positioned at the right
              padding: [0, 0, 0, 20]  // Added right padding
            },
            data: [{ value: this.score }],
            animation: true,
            animationDuration: this.animationDuration,
            animationEasing: 'cubicInOut'
          }
        ]
      };
    }
  },
  watch: {
    score() {
      this.updateChart();
    },
    colors: {
      handler() {
        // If colors change, reinitialize the chart
        this.destroyChart();
        this.$nextTick(() => {
          this.initChart();
        });
      },
      deep: true
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initChart();
    });
  },
  beforeDestroy() {
    this.destroyChart();
  },
  methods: {
    initChart() {
      if (!this.$refs.gaugeChart) return;

      // Initialize the chart
      this.chart = init(this.$refs.gaugeChart);

      // Set initial options
      this.chart.setOption(this.chartOptions);

      // Create debounced resize handler
      this.resizeHandler = debounce(() => {
        if (this.chart) {
          this.chart.resize();
        }
      }, 200);

      // Add resize event listener
      window.addEventListener('resize', this.resizeHandler);
    },

    updateChart() {
      if (!this.chart) return;

      this.chart.setOption({
        series: [{
          data: [{ value: this.score }],
          animationDuration: this.animationDuration
        }]
      });
    },

    destroyChart() {
      // Remove event listener
      if (this.resizeHandler) {
        window.removeEventListener('resize', this.resizeHandler);
        this.resizeHandler = null;
      }

      // Dispose chart instance
      if (this.chart) {
        this.chart.dispose();
        this.chart = null;
      }
    },

    viewDashboard() {
      // Use Vue Router for navigation if available
      if (this.$router) {
        this.$router.push({ name: 'dashboard' });
      } else {
        this.$emit('dashboard-click');
        console.log('Navigate to Dashboard');
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.health-score-gauge {
  height: 100%;

  &__header {
    font-weight: 500;
  }

  &__chart {
    display: flex;
    justify-content: center;
    align-items: center;
    transition: height 0.3s ease;
  }
}
</style>
