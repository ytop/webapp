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
      :style="{ height: chartHeight + 'px' }"
    />
    <div
      class="health-score-gauge__label"
    >
      {{ label }}
    </div>
    <div
      v-if="sublabel"
      class="health-score-gauge__sublabel"
    >
      <a
        href="#"
        @click.prevent="viewDashboard"
      >{{ sublabel }}</a>
    </div>
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
    label: {
      type: String,
      required: true
    },
    sublabel: {
      type: String,
      default: ''
    },
    colors: {
      type: Array,
      default: () => [
        { value: 0, color: '#67e0e3' }, // Green
        { value: 2, color: '#37a2da' }, // Light Blue
        { value: 4, color: '#fd666d' }, // Red
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
            radius: '80%',
            axisLine: {
              lineStyle: {
                width: 20,
                color: this.colors.map(color => [
                  color.value / 5,
                  color.color
                ])
              }
            },
            axisTick: { show: false },
            axisLabel: { show: false },
            splitLine: { show: false },
            pointer: {
              itemStyle: {
                color: 'auto'
              }
            },
            detail: {
              valueAnimation: true,
              formatter: '{value}',
              fontSize: 20,
              offsetCenter: [0, '70%']
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
  transition: all 0.3s ease;

  &__header {
    font-weight: 500;
  }

  &__chart {
    margin: 0 auto;
    display: flex;
    justify-content: center;
    align-items: center;
    transition: height 0.3s ease;
  }

  &__label {
    text-align: center;
    margin-top: 10px;
    font-weight: bold;
    font-size: 16px;
  }

  &__sublabel {
    text-align: center;
    margin-top: 8px;
    color: #909399;
    font-size: 14px;

    a {
      color: #909399;
      text-decoration: none;
      transition: color 0.2s ease;

      &:hover {
        color: #409EFF;
        text-decoration: underline;
      }
    }
  }
}

@media screen and (max-width: 768px) {
  .health-score-gauge {
    &__chart {
      height: 150px !important;
    }

    &__label {
      font-size: 14px;
    }

    &__sublabel {
      font-size: 12px;
    }
  }
}
</style>
