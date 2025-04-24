<template>
  <el-card class="near-miss-bar-chart">
    <div id="barChart" class="bar-chart" />
  </el-card>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'NearMissBarChart',
  props: ['weekData'],
  mounted: function() {
    this.drawBar()
  },

  methods: {
    getLast7Days() {
      const last7Days = []
      const today = new Date()
      for (let i = 6; i >= 0; i--) {
        const date = new Date(today)
        date.setDate(date.getDate() - i)
        last7Days.push(date.toLocaleDateString('en-US'))
      }
      return last7Days
    },

    getX() {
      let d = this.getLast7Days()
      const last5Days = []
      for (let i = 0; i < 7; i++) {
        let td = new Date(Date.parse(d[i]))
        if (td.getDay() >= 1 && td.getDay() <= 5) {
          last5Days.push(d[i].substring(0, d[i].length - 5));
        }
      }
      return last5Days
    },


    getData() {
      const last7Days = this.getLast7Days()
      let val = [0, 0, 0, 0, 0, 0, 0]
      for (let i = 0; i < 7; i++) {
        for (let j = 0; j < this.weekData.length; j++) {
          let ttt = last7Days[i].split("/")
          let tttt = ttt[2] + "-" + ttt[0].padStart(2, '0') + "-" + ttt[1].padStart(2, '0')
          console.log("date reformatted" + tttt)
          if (this.weekData[j]['timestamp'].startsWith(tttt) === true) {
            if (this.weekData[j]['type'] !== '0' && this.weekData[j]['type'] !== '-') {
              val[i] += Number(this.weekData[j]['type'])
            }
          }
        }
      }

      const ret = [0, 0, 0, 0, 0]
      let count = 0
      for (let i = 0; i < 7; i++) {
        let d = new Date(Date.parse(last7Days[i]))
        if (d.getDay() >= 1 && d.getDay() <= 5) {
          ret[count] = val[i]
          count += 1;
        }
      }
      return ret
    },


    getColor() {
      return "rgb(136, 185, 249)"
    },
    drawBar() {
      this.myChart = echarts.init(document.getElementById('barChart'))
      this.myChart.setOption({
        title: {
          text: 'Weekly Near Miss Event Count',
          x: 'center',
          textStyle: {
            fontSize: 15
          }
        },
        
        xAxis: {
          data: this.getX(),
          axisLabel: {
                textStyle: {
                  fontWeight: 'bolder'
                }
              }
        },
        yAxis: {
          minInterval: 1,
          axisLabel: {
                textStyle: {
                  fontWeight: 'bolder'
                }
              }
        },
        tooltip: {textStyle: {
            fontWeight: 'bolder'
          }},
        series: [
          {
            type: 'bar',
            data: this.getData(),
            color: this.getColor(),
            itemStyle: {
              barBorderRadius: 15,
              borderWidth: 1,
              borderType: 'solid',
              shadowBlur: 9,
              shadowColor: '#5470c6'
            }
          }
        ]

      })
    }
  }}
</script>

<style scoped>

.near-miss-bar-chart {
  box-shadow: 4px 4px 10px rgba(0, 0, 0, 0.2);
  border-radius: 15px;
}

.bar-chart {
  width:100%;
  height:278px;
    justify-content: center;
  align-items: center;
  display: flex;
}

</style>
