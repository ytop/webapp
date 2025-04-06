<template>
  <el-card
    shadow="never"
    class="kpi-card"
    :body-style="{ padding: '15px', height: '160px' }"
  >
    <div class="kpi-card__title">
      {{ title }}
    </div>
    <div 
      class="kpi-card__score"
      :style="{ color: color }"
    >
      {{ score }}
    </div>
    <div class="kpi-card__description">
      {{ description }}
    </div>
    <el-button
      type="text"
      size="mini"
      class="kpi-card__action"
    >
      View Dashboard --
    </el-button>
  </el-card>
</template>

<script>
export default {
  name: 'KpiCard',
  props: {
    title: {
      type: String,
      required: true
    },
    score: {
      type: [String, Number],
      required: true
    },
    description: {
      type: String,
      default: ''
    },
    color: {
      type: String,
      default: '#303133'
    }
  }
}
</script>

<style lang="scss" scoped>
.kpi-card {
  height: 100%;

  &__title {
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 15px;
  }

  &__score {
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 10px;
  }

  &__description {
    font-size: 12px;
    color: #909399;
    margin-bottom: 10px;
    line-height: 1.4;
  }

  &__action {
    padding: 0;
    margin: 0;
    height: auto;
    line-height: 1;
  }
}
</style>
