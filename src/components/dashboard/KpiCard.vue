<template>
  <el-card
    shadow="never"
    class="kpi-card"
  >
    <div class="kpi-card__title">
      {{ title }}
    </div>
    <div class="kpi-card__score">
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
    }
  }
}
</script>

<style scoped>
.kpi-card {
  height: 100%;
}

.el-card__body {
  padding: 15px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.kpi-card__title {
  font-size: 12px;
  color: grey;
  margin-bottom: 5px;
}

.kpi-card__score {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
}

.kpi-card__description {
  font-size: 11px;
  color: grey;
  margin-bottom: 10px;
  height: 60px;
  overflow: hidden;
  flex-grow: 1;
}

.kpi-card__action {
  margin-top: auto;
  padding-left: 0;
}
</style>
