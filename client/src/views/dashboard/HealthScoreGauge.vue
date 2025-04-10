<template>
  <el-card
    shadow="never"
    class="health-score-gauge"
  >
    <div
      slot="header"
      class="health-score-gauge__header"
    >
      <span>{{ title }}</span>
    </div>
    <div class="health-score-gauge__content">
      <div class="health-score-gauge__gauge">
        <el-progress
          type="dashboard"
          :percentage="gaugePercentage"
          :color="colorFunction"
          :stroke-width="10"
          :width="150"
        >
          <div class="health-score-gauge__score">
            {{ score }}
          </div>
        </el-progress>
      </div>
      <div class="health-score-gauge__legend">
        <div class="health-score-gauge__legend-item">
          <span
            class="health-score-gauge__legend-color"
            style="background-color: #5cb87a;"
          />
          <span class="health-score-gauge__legend-text">Good (4-5)</span>
        </div>
        <div class="health-score-gauge__legend-item">
          <span
            class="health-score-gauge__legend-color"
            style="background-color: #e6a23c;"
          />
          <span class="health-score-gauge__legend-text">Moderate (2-3.9)</span>
        </div>
        <div class="health-score-gauge__legend-item">
          <span
            class="health-score-gauge__legend-color"
            style="background-color: #f56c6c;"
          />
          <span class="health-score-gauge__legend-text">Poor (0-1.9)</span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'HealthScoreGauge',
  props: {
    title: {
      type: String,
      default: 'Health Score'
    },
    score: {
      type: Number,
      default: 0
    },
    colors: {
      type: Array,
      default: () => [
        {color: '#5cb87a', percentage: 20}, // Green
        {color: '#e6a23c', percentage: 40}, // Yellow
        {color: '#e6a23c', percentage: 60}, // Yellow (Moderate)
        {color: '#f56c6c', percentage: 80}, // Red
        {color: '#f56c6c', percentage: 100} // Red
      ]
    }
  },
  computed: {
    gaugePercentage() {
      // Convert score (0-5) to percentage (0-100)
      return Math.min(Math.max(this.score * 20, 0), 100);
    },
    colorFunction() {
      return (percentage) => {
        // Find the appropriate color based on the percentage
        for (const item of this.colors) {
          if (percentage <= item.percentage) {
            return item.color;
          }
        }
        return this.colors[this.colors.length - 1].color;
      };
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
  
  &__content {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
  }
  
  &__gauge {
    margin-bottom: 15px;
    transform: scale(0.75); /* Scale to 75% of original size */
  }
  
  &__score {
    font-size: 36px;
    font-weight: bold;
    color: #303133;
  }
  
  &__legend {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 5px;
  }
  
  &__legend-item {
    display: flex;
    align-items: center;
    font-size: 12px;
  }
  
  &__legend-color {
    display: inline-block;
    width: 12px;
    height: 12px;
    border-radius: 2px;
    margin-right: 5px;
  }
  
  &__legend-text {
    color: #606266;
  }
}
</style>
