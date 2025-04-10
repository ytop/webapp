<template>
  <el-card
    shadow="never"
    class="kpi-card"
    :body-style="bodyStyle"
  >
    <div class="kpi-card__content">
      <div class="kpi-card__header">
        <div class="kpi-card__title">
          {{ title }}
        </div>
      </div>
      <div class="kpi-card__score-container">
        <div
          class="kpi-card__score"
          :style="{ color }"
        >
          {{ score }}
        </div>
      </div>
      <div class="kpi-card__description">
        {{ description }}
      </div>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'KpiCard',
  props: {
    title: {
      type: String,
      default: ''
    },
    score: {
      type: Number,
      default: 0
    },
    description: {
      type: String,
      default: ''
    },
    color: {
      type: String,
      default: '#67C23A'
    },
    bodyStyle: {
      type: Object,
      default: () => ({})
    }
  }
};
</script>

<style lang="scss" scoped>
.kpi-card {
  height: 100%;
  transform: scale(0.75); /* Scale to 75% of original size */
  transform-origin: top left;
  
  &__content {
    display: flex;
    flex-direction: column;
    height: 100%;
  }
  
  &__header {
    margin-bottom: 10px;
  }
  
  &__title {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
    line-height: 1.2;
  }
  
  &__score-container {
    display: flex;
    justify-content: center;
    align-items: center;
    margin: 10px 0;
  }
  
  &__score {
    font-size: 36px;
    font-weight: bold;
  }
  
  &__description {
    font-size: 12px;
    color: #606266;
    line-height: 1.4;
    flex-grow: 1;
    margin-top: auto;
  }
}
</style>
