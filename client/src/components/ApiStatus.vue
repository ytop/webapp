<template>
  <div class="api-status">
    <el-card class="status-card">
      <div
        slot="header"
        class="clearfix"
      >
        <span>API Connection Status</span>
        <el-button
          style="float: right; padding: 3px 0"
          type="text"
          @click="testConnection"
        >
          Test Connection
        </el-button>
      </div>
      
      <div
        v-if="loading"
        class="status-loading"
      >
        <el-progress
          type="circle"
          :percentage="50"
          status="exception"
        />
        <p>Testing API connection...</p>
      </div>
      
      <div
        v-else-if="error"
        class="status-error"
      >
        <el-alert
          title="Connection Error"
          type="error"
          :description="error"
          show-icon
        />
        <div class="error-details">
          <p><strong>Details:</strong></p>
          <pre>{{ errorDetails }}</pre>
        </div>
      </div>
      
      <div
        v-else-if="status"
        class="status-success"
      >
        <el-alert
          title="Connection Successful"
          type="success"
          description="API connection is working properly"
          show-icon
        />
        
        <div class="status-details">
          <h4>Server Health:</h4>
          <pre>{{ JSON.stringify(status.health, null, 2) }}</pre>
          
          <h4>KRIs Available:</h4>
          <p>{{ status.kris ? status.kris.length : 0 }} KRIs found</p>
          
          <h4>Tasks Available:</h4>
          <p>{{ status.tasks ? status.tasks.length : 0 }} tasks found</p>
        </div>
      </div>
      
      <div
        v-else
        class="status-unknown"
      >
        <el-alert
          title="Connection Status Unknown"
          type="info"
          description="Click 'Test Connection' to check API status"
          show-icon
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { testApiConnection } from '@/utils/testApi'

export default {
  name: 'ApiStatus',
  data() {
    return {
      loading: false,
      status: null,
      error: null,
      errorDetails: null
    }
  },
  mounted() {
    // Test connection when component is mounted
    this.testConnection()
  },
  methods: {
    async testConnection() {
      this.loading = true
      this.error = null
      this.errorDetails = null
      
      try {
        const result = await testApiConnection()
        
        if (result.success) {
          this.status = result
          this.error = null
        } else {
          this.status = null
          this.error = 'Failed to connect to API'
          this.errorDetails = JSON.stringify(result, null, 2)
        }
      } catch (err) {
        this.status = null
        this.error = err.message || 'Unknown error'
        this.errorDetails = JSON.stringify(err, null, 2)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.api-status {
  margin: 20px 0;
}

.status-card {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.status-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.status-details {
  margin-top: 20px;
  padding: 10px;
  background-color: #f8f8f8;
  border-radius: 4px;
}

.error-details {
  margin-top: 20px;
}

pre {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
}
</style>
