<template>
  <div class="validate-trigger">
    <el-row v-for="num in [0, 1]" :key="`row-${num}`" class="monitor-row">
      <el-col v-for="index in [0, 1, 2]" :key="`col-${num}-${index}`" :span="8">
        <div
          class = "monitor-card"
          :class="getCardClass(tags[num * 3 + index] && tags[num * 3 + index].status)"
        >
          <div class="card-header">
            <h3 class="card-title">
               {{ (tags[num * 3 + index] && tags[num * 3 + index].title) || 'No Title' }}
            </h3>
          </div>
          <div class = "card-actions">
            <el-button 
              class="action-button trigger-button"
              icon="el-icon-video-play"
              :loading="loadingStates[tags[num * 3 + index] && tags[num * 3 + index].title]"
              @click="executeDetection(tags[num * 3 + index])"
            >
              Scan Now
            </el-button>
          </div>
          <div class="addional-info">
            {{ (tags[num * 3 + index] && tags[num * 3 + index].timestamp) || 'N/A' }} - {{ (tags[num * 3 + index] && tags[num * 3 + index].status) || 'N/A' }} - {{ (tags[num * 3 + index] && tags[num * 3 + index].type) || 'N/A' }} - 
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { executeDetectionQuery, getWeeklyDetections } from '@/api/mra'

export default {
  name: 'ValidateTrigger',
  data() {
    return {
      timelineData: [],
      loadingStates: {},
      selectedItem: null,
      detectionResults: null,
      showResults: false
    }
  },
  created() {
   this.fetchDetections()
  },
  computed: {
    tags() {
      return this.timelineData
    }
  },
  methods: {
    getCardClass(status) {
      return {
        'successful': 'status-success',
        'failed': 'status-danger',
        'not started': 'status-info',
      }[status] || 'status-default'
    },
    getStatusClass(status) {
      return {
        'successful': 'status-success',
        'failed': 'status-danger',
        'not started': 'status-info',
      }[status] || 'status-default'
    },
    async fetchDetections() {
     try {
      const response = await getWeeklyDetections()
      if (!response.code || response.code !== 200) {
        console.error('Invalid response code:', response)
        throw new Error('Invalid response code')
      }

      if (!response.data?.detections) {
        console.error('Response detections field not found:', response)
        throw new Error('Response detections field missing')
      }

      const latestDetections = {}
      response.data.detections
       .filter(item => item.event_date.startsWith(new Date().toLocaleDateString('en-CA')))
       .forEach(item => {
        if (!latestDetections[item.detection_id] || new Date(item.create_time) > new Date(latestDetections[item.detection_id].create_time)) {
          latestDetections[item.detection_id] = item;
        }
       });

      // Ensure each detection_id only has one row in timelineData
      this.timelineData = Object.values(latestDetections)
        .sort((a, b) => b.detection_id - a.detection_id)
        .map(item => ({
            title: `${item.detection_dept} ${item.detection_desc}, ID:${item.detection_id}`,
            timestamp: item.event_date,
            status: item.detection_status?.toLowerCase() || 'unknown',
            type: item.near_miss_status,
            detectionId: item.detection_id
        }));
      console.log('Processed timelineData:', this.timelineData)
    } catch (error) {
      this.$message({type: 'error', message: 'Failed to fetch detection data'})
      console.error('Failed to fetch detection data:', error)
    }
   },

    async executeDetection(item) {
      if (!item) return

      this.$set(this.loadingStates, item.title, true)

      try {
        // const detectionId = this.getDetectionId(item)
        const detectionId = item.detectionId;
        const response = await executeDetectionQuery(detectionId)

        this.detectionResults = response.data
        this.selectedItem = item
        this.showResults = true

        this.$message({
          type: 'success',
          message: `Done to detect for ${item.title}`
        })

        await this.fetchDetections()
      } catch (error) {
        this.$message({
          type: 'error',
          message: `Failed to execute detection: ${error.message}`
        })
      } finally {
        this.$set(this.loadingStates, item.title, false)
      }
    },
  }
}
</script>

<style scoped>
.validate-trigger {
  height: 100%;
  padding: 24px;
  background-color: #f8f9fd;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.monitor-card {
  height: 160px;
  margin: 0 10px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.monitor-card:hover {
  transform: translateY(-4px);
   box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}


.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  margin: 0;
  font-size: 11px;
  font-weight: 600;
  color: #2c3e50;
  while-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 80%;
}

.status-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.card-actions {
  display: flex;
  gap: 12px;
}

.action-button {
  flex: 1;
  border-radius: 8px;
  font-weight: 500;
  padding: 10px 0;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.trigger-button {
  background-color: #8dc0be;
  color: white;
  border: none;
}

.trigger-button:hover {
  background-color: #1e89a1;
}

.status-success {
  background-color: #f0fff4;
  border-left: 4px solid #48bb78;
}

.status-danger {
  background-color: #fff5f5;
  border-left: 4px solid #f56565;
}

.status-info {
  background-color: #ebf8ff;
  border-left: 4px solid #4299e1;
}

.status-default {
  background-color: #fff;
  border-left: 4px solid #a0aec0;
}

.indicator-success {
  background-color: #48bb78;
}

.indicator-danger {
  background-color: #f56565;
}

.indicator-info {
  background-color: #4299e1;
}

.indicator-default {
  background-color: #a0a3c0;
}

@media (max-width: 768px) {
  .monitor-card {
    height: auto;
    min-height: 160px;
  }

  .card-actions {
    flex-direction: column;
  }
}
</style>
