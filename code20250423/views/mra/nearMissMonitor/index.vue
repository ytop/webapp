<template>
  <el-row :key="this.dataChanged">
    <el-collapse v-model="this.$store.getters.activeNames" style="width:100%">
      <el-collapse-item name="1" title="Detection Charts">
        <el-row>
          <el-col :span="18">
            <div class="section-class">
              <el-card style="border-radius: 15px; background-color:#c3c6c7">
                <el-row>
                  <div class="section-title">
                    Task
                  </div>
                </el-row>
                <el-row>
                  <NearMissMonitor v-if="isGetData" :timeline-data="timelineData" :week-data="weekData" :quarter-data="quarterData" />
                </el-row>
              </el-card>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="section-class">
              <el-card style="border-radius: 15px; background-color:#c3c6c7">
                <el-row>
                  <div class="section-title">
                    Trend
                  </div>
                </el-row>
                <el-row>
                  <el-row>
                    <el-col>
                      <div class="chart-div">
                        <NearMissBarChart v-if="isGetData" :week-data="weekData" />
                      </div>
                    </el-col>
                  </el-row>
                  <el-row>
                    <el-col>
                      <div class="chart-div">
                        <NearMissPieChart v-if="isGetData" :timeline-data="timelineData" />
                      </div>
                    </el-col>
                  </el-row>
                </el-row>
              </el-card>
            </div>
          </el-col>
        </el-row>
      </el-collapse-item>
    </el-collapse>
    <el-row>
      <NearMissTable v-if="this.$store.getters.tableName" :key="this.$store.getters.detection_id" :table-name="this.$store.getters.tableName" />
    </el-row>
  </el-row>
</template>

<script>

import NearMissMonitor from '@/views/mra/nearMissMonitor/NearMissMonitor.vue'
import NearMissPieChart from '@/views/mra/nearMissMonitor/NearMissPieChart.vue'
import NearMissBarChart from '@/views/mra/nearMissMonitor/NearMissBarChart.vue'
import NearMissTable from '@/views/mra/nearMissMonitor/NearMissTable.vue'
import { getWeeklyDetections, getQuarterlyDetections } from '@/api/mra'

export default {
  name: 'NearMiss',
  components: { NearMissBarChart, NearMissMonitor, NearMissPieChart, NearMissTable },
  data() {
    return {
      timelineData: [],
      weekData: [],
      quarterData: [],
      isGetData: false,
      dataChanged: true
    }
  },
  computed: {
    trigger() {
      return this.$store.getters.refresh
    }
  },
  watch: {
    trigger(newVal, oldVal) {
      this.fetchData()
    }
  },
  async created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
          try {
            console.log("re-execute")
            this.$store.commit('changeTableName', '')
            this.$store.commit('changeDetectionId', '')
            this.$store.commit('changeActiveNames', ["1"])
            const response = await getWeeklyDetections()
            // console.log("Full response data:", JSON.stringify(response, null, 2))
            const quarter_response = await getQuarterlyDetections()
            if (!response.code || response.code !== 200) {
              throw new Error('Invalid response code')
            }
            if (!response.data?.detections) {
              throw new Error('Response detections filed missing')
            }

            if (!quarter_response.code || quarter_response.code !== 200) {
              throw new Error('Invalid response code')
            }

            if (!quarter_response.data?.nearMissHistory) {
              throw new Error('Response detections filed missing')
            }

            console.log(response.data.detections)
            console.log(quarter_response.data.nearMissHistory)


            const today = new Date().toLocaleDateString('en-CA')
            this.timelineData = response.data.detections
              .filter(item => {
                if (!item.event_date) {
                  console.error('Date Error!')
                  return false
                }
                return item.event_date.startsWith(today)
              })
              .sort((a, b) => b.detection_id - a.detection_id)
              .map(item => {
                const requriedFields = ['detection_id', 'event_date', 'detection_status', 'near_miss_status', 'detection_dept', 'detection_desc']
                const missingFields = requriedFields.filter(field => !item[field])
                if (missingFields.length > 0) {
                  throw new Error(`Item missing fields: ${missingFields.join(', ')}`, item)
                }
                return {
                  title: `${item.detection_dept} ${item.detection_desc}, ID:${item.detection_id}`,
                  timestamp: item.event_date,
                  status: item.detection_status?.toLowerCase() || 'unknown',
                  type: item.near_miss_status,
                  department: item.detection_dept || 'unknown',
                  description: item.detection_desc || 'No description available',
                  detectionId: item.detecition_id
                }
              })

            this.weekData = response.data.detections
              .filter(item => {
                if (!item.event_date) {
                  console.error('Date error!')
                  return false
                }
                return item.event_date
              })
              .sort((a, b) => b.detection_id - a.detection_id)
              .map(item => {
                const requriedFields = ['detection_id', 'event_date', 'detection_status', 'near_miss_status', 'detection_dept', 'detection_desc', 'create_time']
                const missingFields = requriedFields.filter(field => !item[field])
                if (missingFields.length > 0) {
                  console.error(`Item missing fields: ${missingFields.join(', ')}`, item)
                }
                return {
                  title: `${item.detection_dept} ${item.detection_desc}, ID:${item.detection_id}`,
                  timestamp: item.event_date,
                  eventdetail: item.create_time,
                  status: item.detection_status?.toLowerCase() || 'unknown',
                  type: item.near_miss_status,
                  department: item.detection_dept || 'unknown',
                  description: item.detection_desc || 'No description available',
                  detectionId: item.detecition_id,
                  createtime: item.create_time !== undefined ? item.create_time : 'No create time'
                }
              })

            this.quarterData = quarter_response.data.nearMissHistory
            this.isGetData = true
            this.dataChanged =  !this.dataChanged
          } catch (error) {
            throw new Error('Failed to fetech detection data:', error)
          }
    }
  }
}
</script>

<style scoped>

.chart-div {
  height: 100%;
  padding: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: #0x52ba;
  border-radius: 10px;
  background-color:#c3c6c7;
  height: 10px;
}

.section-class {
  padding: 10px;
}
</style>
