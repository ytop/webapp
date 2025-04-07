<template>
  <el-card
    shadow="never"
    class="health-score-gauge"
    :body-style="{ padding: '11px' }"
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
      :style="{ height: '180px' }"
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
      default: 135
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
            radius: '56%',  // Reduced to 75% of original 75%
            center: ['50%', '45%'],  // Moved up slightly to make room for value
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
              length: '9%',
              width: 3,
              offsetCenter: [0, '-60%'],
              itemStyle: {
                color: 'auto'
              }
            },
            axisTick: {
              show: true,
              length: 9,
              lineStyle: {
                color: 'auto',
                width: 1
              }
            },
            splitLine: {
              length: 15,
              lineStyle: {
                color: 'auto',
                width: 2
              }
            },
            axisLabel: {
              show: false
            },
            title: {
              show: false
            },
            detail: {
              valueAnimation: true,
              formatter: '{value}',
              color: '#303133',
              fontSize: 27,
              fontWeight: 'bold',
              offsetCenter: [0, '85%'],  // Adjusted position for value
              padding: [15, 0, 0, 0]
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
    // Use a small delay to ensure DOM is fully rendered
    setTimeout(() => {
      this.initChart();
    }, 100);
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

      // Force a resize after initialization to ensure proper rendering
      this.$nextTick(() => {
        if (this.chart) {
          this.chart.resize();
        }
      });
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

    // Method to manually refresh the chart if needed
    refreshChart() {
      if (this.chart) {
        this.chart.resize();
        this.updateChart();
      } else {
        this.initChart();
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
  transform: scale(0.75);
  transform-origin: center center;

  &__header {
    font-weight: 500;
    font-size: 0.95rem;
  }

  &__chart {
    display: flex;
    justify-content: center;
    align-items: center;
    transition: height 0.3s ease;
    margin-top: -15px;  // Reduced negative margin to adjust spacing
  }
}
</style>
