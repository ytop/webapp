<template>
  <el-card class="near-miss-pie-chart">
    <div id="pieChart" class="pie-chart" />
    <div class="pie-line">
      <el-row>
        <el-col :span=8 style="background:#add3ca; border-radius:15px;" class="line-section"><a>Successful:{{this.getSuccessfulNum()}}</a></el-col>
        <el-col :span=8 style="background:#ea949e; border-radius:15px;" class="line-section"><a>Failed: {{this.getFailedNum()}}</a></el-col>
        <el-col :span=8 style="background:rgb(233, 233, 235); border-radius:15px" class="line-section"><a>Not Started: {{this.getNotStartedNum()}}</a></el-col>
      </el-row>
    </div>
  </el-card>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'NearMissPieChart',
  props: ['timelineData'],
  mounted: function() {
    this.drawPie()
  },
  methods: {
    getValue() {
      return [
        { name: 'Successful', value: this.getSuccessfulNum() },
        { name: 'Failed', value: this.getFailedNum() },
        { name: 'Not Started', value: this.getNotStartedNum() }
      ]
    },
    getSucccessfulColor() {
      return "#add3ca"
    },
    getFailedColor() {
      return "#ea949e"
    },
    getNotStartedColor() {
      return "rgb(233, 233, 235)"
    },
    getSuccessfulNum() {
      let ret = 0
      for (let i = 0; i < this.timelineData.length; i++) {
        if (this.timelineData[i]['status'] === 'successful') {
          ret += 1
        }
      }
      return ret
    },
    getFailedNum() {
      let ret = 0
      for (let i = 0; i < this.timelineData.length; i++) {
        if (this.timelineData[i]['status'] === 'failed') {
          ret += 1
        }
      }
      return ret
    },
    getNotStartedNum() {
      let ret = 0
      for (let i = 0; i < this.timelineData.length; i++) {
        if (this.timelineData[i]['status'] === 'not started') {
          ret += 1
        }
      }
      return ret
    },
    drawPie() {
      this.myChart = echarts.init(document.getElementById('pieChart'))
      this.myChart.setOption({
        title: {
          text: 'Daily Task Status',
          x: 'center',
          textStyle: {
            fontSize: 15
          }
        },
        tooltip: {
          tigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)',
          textStyle: {
            fontWeight: 'bolder'
          }
        },
        color: [this.getSucccessfulColor(), this.getFailedColor(), this.getNotStartedColor()],
        series: [
          {
            name: 'Daily Task Status',
            type: 'pie',
            radius: '57%',
            center: ['50%', '50%'],
            data: this.getValue(),
            radius: ["60%", "50%"],
            itemStyle: {
              barBorderRadius: 15,
              borderWidth: 1,
              borderType: 'solid',
              shadowBlur: 9,
              shadowColor: '#5470c6'
            },
            label: {
              fontWeight: 'bolder'
            }
          }
        ]

      })
    }
  }}
</script>

<style scoped>

.pie-chart {
  width: 100%;
  height: 258px;
  justify-content: center;
  align-items: center;
  display: flex;
}

.pie-line {
  height: 20px;
  
  border-radius: 15px;
  font-size: small;
  font-weight: bold;
}

.near-miss-pie-chart {
  box-shadow: 4px 4px 10px rgba(0, 0, 0, 0.2);
  border-radius: 15px;
}

.line-section {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
