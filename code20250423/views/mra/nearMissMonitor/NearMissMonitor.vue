<template>
  <div class="near-miss-monitor">
    <el-row v-for="num in [0, 1]" :key="`row-${num}`" class="">
      <el-col v-for="index in [0, 1, 2]" :key="`col-${num}-${index}`" :span="8">
        <div class="data-div">
            <el-card :class="[getCardClass(tags[num * 3 + index]), 'custom-card']">
              
                <el-row class="tag-name">
                  <el-row style="height:50px;">
                    <el-col :span=20>
                      <el-row>
                        Dept: {{ (tags[num * 3 + index] && parseDept(num * 3 + index)) || 'No Title' }}
                      </el-row>
                      <el-row>
                        Desc: {{ (tags[num * 3 + index] && parseTitle(num * 3 + index)) || 'No Title' }}
                      </el-row>
                    </el-col>

                    <el-col :span=4>
                      <el-row v-if="parseDept(num * 3 + index)==='OSD' && (deptName === 'OSD' || userRole.includes('Admin') === true)">
                        <near-miss-upload :task-indicator="parseID(num * 3 + index)" />
                      </el-row>
                    </el-col>
                  </el-row>

                  <el-divider></el-divider>

                  <el-row style="background:#BEBEBE">
                      <el-col :span=8 style="font-size:12px">
                        <u @click="setDetection(parseID(num * 3 + index))" class="detection-link">Latest Detection:</u>
                      </el-col>

                      <el-col :span=5 style="font-size:12px">
                        {{ getLastestDetectionDate(num * 3 + index) }}
                      </el-col>
                      <el-col :span=3 style="font-size:12px">
                        {{ getLastestDetectiontime(num * 3 + index) }}
                      </el-col>
                      <el-col :span=6 :class="[getStatusClass(getLastestDetectionStatus(num * 3 + index))]" style="font-size:12px">
                        {{ getLastestDetectionStatus(num * 3 + index) }}
                      </el-col>
                      <el-col :span=2 style="font-size:12px">
                        {{ getLastestDetectionNum(num * 3 + index) }}
                      </el-col>
                      
                  </el-row>

                  <el-divider></el-divider>

                 
                  <el-row>
                    <el-card style="border-radius: 15px;box-shadow: 4px 4px 10px rgba(0, 0, 0, 0.2); height:190px;">
                      <div :id="'quarter' + getId(num * 3 + index)" class="quarter-chart" />
                    </el-card>
                  </el-row>

                </el-row>
              
            </el-card>
        </div>
      </el-col>
    </el-row>
  </div>
</template>
<script>
import * as echarts from 'echarts'
import NearMissUpload from './NearMissUpload.vue'

export default {
  name: 'NearMissMonitor',
  props: ['timelineData', 'weekData', 'quarterData'],
  components: { NearMissUpload },
  data() {
    return {
      loadingStates: {},
      selectedItem: null,
      detectionResults: null
    }
  },
  
  computed: {

    userRole() {
      return this.$store.getters.roles[0]
    },

    deptName() {
      return this.$store.getters.deptId
    },

    tags() {
      return this.timelineData
    },

    getId(){
      return function(index) {
        return index.toString();
      }
    },

    parseDept() {
      return function(index) {
        return this.timelineData[index]['title'].substring(0, 3);
      }
    },
    
    parseTitle() {
      return function(index) {
        const title_length = this.timelineData[index].title.length
        return this.timelineData[index]['title'].substring(4, title_length - 8)
      }
    },

    parseID() {
      return function(index) {
        const title_length = this.timelineData[index].title.length
        return this.timelineData[index]['title'].substring(title_length - 6, title_length)
      }
    },

    getLastestDetectionDate() {
      return function(index) {
        const title = this.timelineData[index].title
        const detections = []
        for (let i = 0; i  < this.weekData.length; i++) {
          if (this.weekData[i].title === title && this.weekData[i].status !== 'not started') {
            detections.push(this.weekData[i])
          }
        }
        detections.sort((a, b) => {
          let timeA = a.createtime;
          let timeB = b.createtime;
          if (timeA === 'No create time') {
              timeA = '1900-01-01T00:00:00.000+00:00'
          }
          if (timeB === 'No create time') {
            timeB = '1900-01-01T00:00:00.000+00:00'
          }
          if (timeA < timeB) {
            return -1;
          }
          if (timeA > timeB) {
            return 1;
          }
          return 0;
        })
        return detections[detections.length - 1].timestamp

      }
    },

    getLastestDetectionNum() {
      return function(index) {
        const title = this.timelineData[index].title
        let detections = []
        for (let i = 0; i  < this.weekData.length; i++) {
          if (this.weekData[i].title === title && this.weekData[i].status !== 'not started') {
            detections.push(this.weekData[i])
          }
        }
        detections.sort((a, b) => {
          let timeA = a.createtime;
          if (timeA === 'No create time') {
              timeA = '1900-01-01T00:00:00.000+00:00'
          }
          let timeB = b.createtime;
          if (timeB === 'No create time') {
            timeB = '1900-01-01T00:00:00.000+00:00'
          }
          if (timeA < timeB) {
            return -1;
          }
          if (timeA > timeB) {
            return 1;
          }
          return 0;
        })
        return detections[detections.length - 1].type

      }
    },


    getLastestDetectionStatus() {
      return function(index) {
        const title = this.timelineData[index].title
        let detections = []
        for (let i = 0; i  < this.weekData.length; i++) {
          if (this.weekData[i].title === title && this.weekData[i].status !== 'not started') {
            detections.push(this.weekData[i])
          }
        }
        detections.sort((a, b) => {
          let timeA = a.createtime;
          if (timeA === 'No create time') {
              timeA = '1900-01-01T00:00:00.000+00:00'
          }
          let timeB = b.createtime;
          if (timeB === 'No create time') {
            timeB = '1900-01-01T00:00:00.000+00:00'
          }
          if (timeA < timeB) {
            return -1;
          }
          if (timeA > timeB) {
            return 1;
          }
          return 0;
        })
        return detections[detections.length - 1].status

      }
    },

    getLastestDetectiontime() {
      return function(index) {
        const title = this.timelineData[index].title
        let detections = []
        for (let i = 0; i  < this.weekData.length; i++) {
          if (this.weekData[i].title === title && this.weekData[i].status !== 'not started') {
            detections.push(this.weekData[i])
          }
        }
        detections.sort((a, b) => {
            let timeA = a.createtime;
            if (timeA === 'No create time') {
              timeA = '1900-01-01T00:00:00.000+00:00'
            }
            let timeB = b.createtime;
            if (timeB === 'No create time') {
              timeB = '1900-01-01T00:00:00.000+00:00'
            }
            if (timeA < timeB) {
              return -1;
            }
            if (timeA > timeB) {
              return 1;
            }
            return 0;
          })
        return detections[detections.length - 1].createtime.substring(11, 16)

      }
    }
  },

  mounted: function() {
      this.drawBar()
  },

  methods: {

    setDetection(id) {
      this.$store.commit('changeTableName', 'IMP_DETECTION')
      this.$store.commit('changeDetectionId', id.substring(3, id.length))
      this.$store.commit('changeActiveNames', [])
    },
  
    getCurrentQuarter() {
      const now = new Date();
      const month = now.getMonth();
      const quarter = Math.floor(month / 3) + 1;
      return "Q" + quarter.toString();
    },

    getPrevious1Quarter() {
      const now = new Date();
      const month = now.getMonth();
      let quarter = Math.floor(month / 3) + 1;
      quarter = quarter - 1
      if (quarter <= 0) {
        quarter = quarter + 4
      }
      return "Q" + quarter.toString();
    },

    getPrevious2Quarter() {
      const now = new Date();
      const month = now.getMonth();
      let quarter = Math.floor(month / 3) + 1;
      quarter = quarter - 2
      if (quarter <= 0) {
        quarter = quarter + 4
      }
      return "Q" + quarter.toString()
    },

    getPrevious3Quarter() {
      const now = new Date();
      const month = now.getMonth();
      let quarter = Math.floor(month / 3) + 1;
      quarter = quarter - 3
      if (quarter <= 0) {
        quarter = quarter + 4
      }
      return "Q" + quarter.toString();
    },

    getPrevious4Quarter() {
      const now = new Date();
      const month = now.getMonth();
      let quarter = Math.floor(month / 3) + 1;
      quarter = quarter - 4
      if (quarter <= 0) {
        quarter = quarter + 4
      }
      return "Q" + quarter.toString()
    },

    getCurrentMonth() {
      const now = new Date();
      let month = now.getMonth();
      return month.toString()
    },

    getCurrentMonth() {
      const now = new Date();
      let month = now.getMonth() + 1;
      return month.toString()
    },

    getPrev1Month() {
      const now = new Date();
      let month = now.getMonth() + 1- 1;
      if (month <= 0) {
        month = month + 12
      }
      return month.toString()
    },

    getPrev2Month() {
      const now = new Date();
      let month = now.getMonth() + 1 - 2;
      if (month <= 0) {
        month = month + 12
      }
      return month.toString()
    },

    getPrev3Month() {
      const now = new Date();
      let month = now.getMonth() + 1 - 3;
      if (month <= 0) {
        month = month + 12
      }
      return month.toString()
    },

    getPrev4Month() {
      const now = new Date();
      let month = now.getMonth() + 1 - 4;
      if (month <= 0) {
        month = month + 12
      }
      return month.toString()
    },

    getPrev5Month() {
      const now = new Date();
      let month = now.getMonth() + 1 - 5;
      if (month <= 0) {
        month = month + 12
      }
      return month.toString()
    },

    
    drawBar() {
      for (let num of [0, 1]) {
        for (let index of [0, 1, 2]) {
          let id = num * 3 + index;
          this.myChart = echarts.init(document.getElementById('quarter' + id.toString()))
          let current = this.timelineData[id]
          let current_id = current.title.substring(current.title.length - 3, current.title.length)
          // console.log(this.quarterData[current_id])
          let x_axis_label = []
          if (this.quarterData[current_id].length !== 6) {
            x_axis_label.push(this.getPrevious4Quarter(), this.getPrevious3Quarter(), this.getPrevious2Quarter(), this.getPrevious1Quarter(), this.getCurrentQuarter())
            
            this.myChart.setOption({
            backgroundColor: "rgb(255, 255, 255)",
            title: {
              left: 'center',
              text: 'Quarterly Historical Data',
              textStyle: {
                fontSize: 13
              }
            },
            xAxis: {
              type: 'category',
              data: x_axis_label,
              axisLabel: {
                textStyle: {
                  fontWeight: 'bolder'
                }
              }
            },
            yAxis: {
              minInterval: 1,
              type: 'value',
              axisLabel: {
                show: true,
                textStyle: {
                  fontWeight: 'bolder'
                }
              }
            },
            tooltip: {},
            series: [
              {
                type: 'line',
                data: this.quarterData[current_id],
                smooth: true,
                lineStyle: {
                  width: 5,
                },
                symbolSize: 10,
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
          } else {
            x_axis_label = [this.getPrev5Month(), this.getPrev4Month(), this.getPrev3Month(), this.getPrev2Month(), this.getPrev1Month(), this.getCurrentMonth()]
            this.myChart.setOption({
            backgroundColor: "rgb(255, 255, 255)",
            title: {
              left: 'center',
              text: 'Monthly Historical Data',
              textStyle: {
                fontSize: 13
              }
            },
            xAxis: {
              type: 'category',
              data: x_axis_label,
              axisLabel: {
                textStyle: {
                  fontWeight: 'bolder'
                }
              }
            },
            yAxis: {
              minInterval: 1,
              type: 'value',
              axisLabel: {
                show: true,
                textStyle: {
                  fontWeight: 'bolder'
                }
              }
            },
            tooltip: {},
            series: [
              {
                type: 'line',
                data: this.quarterData[current_id],
                smooth: true,
                lineStyle: {
                  width: 5,
                },
                symbolSize: 10,
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

          
        }
      }
    },

    getStatusClass(item) {
      return item === 'successful' ? 'successStatus' :
            item === 'failed' ? 'dangerStatus' : 'dangerStatus';
    },


    getCardClass(item) {
      return item.status === 'successful' ? 'success':
            item.status === 'failed' ? 'danger':
            item.status === 'not started' ? 'default' : 'default';
    },

    getTagType(status) {
      if (!status) return 'default'
      return {
        successful: 'success',
        failed: 'danger',
        not_started: 'info'
      }[status] || 'default'
    }
  }
}
</script>

<style scoped>

.custom-card {
  white-space: normal;
  word-break: break-word;
  box-shadow: 4px 4px 10px rgba(0, 0, 0, 0.2);
}

.successStatus {
  color : rgba(26, 121, 68, 0.8);
}

.dangerStatus {
  color: #f56c6c;
}


.success {
  background: linear-gradient(#add3ca, transparent);
  border-radius: 15px;
}

.danger {
  background: linear-gradient(#ea949e, transparent);
  border-radius: 15px;
}

.default {
  background-color: rgb(233, 233, 235);
  border-radius: 15px;
}

.data-div {
  height: 100%;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.near-miss-monitor-row {
  display: flex;
  flex-wrap: wrap;

}

.tag-name {
  font-size: small;
  font-weight: bold;
  height: 278px;
  border-color: transparent;
}

.quarter-chart {
  width: 100%;
  height: 180px;
  padding: 5px;

  justify-content: center;
  align-items: center;
  display: flex;
  border-radius: 15px;
  background-color: #ffffff;
}

.el-divider--horizontal {
  margin: 8px 0;
  background: 0 0;
  border-top: 1px dashed #000000;
}

.detection-link {
  color: blue;
  text-decoration:underine;
  cursor: pointer;
}

.detection-link:hover {
  color: white;
  text-decoration: none;
}
</style>
