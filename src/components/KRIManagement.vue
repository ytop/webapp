<template>
  <div class="kri-management-container">
    <h1 class="page-title">Key Risk Index Management System</h1>

    <el-row :gutter="20" class="dashboard-cards">
      <el-col :span="8">
        <el-card class="dashboard-card" shadow="hover" @click.native="navigateTo('/inventory')">
          <div slot="header" class="card-header">
            <span>KRI Inventory</span>
          </div>
          <div class="card-content">
            <i class="el-icon-document"></i>
            <p>Manage your Key Risk Index inventory</p>
            <p class="card-stat">{{ kriList.length }} KRIs defined</p>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="dashboard-card" shadow="hover" @click.native="navigateTo('/data-collection')">
          <div slot="header" class="card-header">
            <span>KRI Data Collection</span>
          </div>
          <div class="card-content">
            <i class="el-icon-edit-outline"></i>
            <p>Submit monthly KRI data</p>
            <p class="card-stat">{{ currentPeriod }} is current period</p>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="dashboard-card" shadow="hover" @click.native="navigateTo('/reporting')">
          <div slot="header" class="card-header">
            <span>KRI Reporting</span>
          </div>
          <div class="card-content">
            <i class="el-icon-data-analysis"></i>
            <p>View KRI reports and trends</p>
            <p class="card-stat">{{ alertCount }} alerts this month</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-divider content-position="center">Recent KRI Status</el-divider>

    <el-table :data="recentKRIData" border style="width: 100%">
      <el-table-column prop="kriId" label="KRI ID" width="120" />
      <el-table-column prop="kriDesc" label="KRI Description" width="300" />
      <el-table-column prop="period" label="Period" width="120" />
      <el-table-column prop="value" label="Value" width="100" />
      <el-table-column prop="status" label="Status" width="120">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submissionDate" label="Submission Date" width="150" />
      <el-table-column prop="submittedBy" label="Submitted By" />
    </el-table>
  </div>
</template>

<script>
import { getKRIs, getKRIData } from '@/api/kri'

export default {
  name: 'KriManagement',
  data() {
    return {
      kriList: [],
      recentKRIData: [],
      currentPeriod: '',
      alertCount: 0
    }
  },
  created() {
    this.fetchKRIs()
    this.fetchRecentKRIData()
    this.setCurrentPeriod()
  },
  methods: {
    async fetchKRIs() {
      try {
        const response = await getKRIs()
        this.kriList = response
      } catch (error) {
        this.$message.error('Failed to fetch KRI data.')
        console.error(error)
      }
    },
    async fetchRecentKRIData() {
      try {
        // Get current period data
        const currentPeriod = this.getCurrentPeriod()
        const response = await getKRIData(currentPeriod)

        // Enrich data with KRI descriptions
        this.recentKRIData = response.map(item => {
          const kri = this.kriList.find(k => k.kriId === item.kriId) || {}
          return {
            ...item,
            kriDesc: kri.kriDesc || 'Unknown'
          }
        })

        // Count alerts (Warning or Critical status)
        this.alertCount = this.recentKRIData.filter(item =>
          item.status === 'Warning' || item.status === 'Critical'
        ).length
      } catch (error) {
        this.$message.error('Failed to fetch recent KRI data.')
        console.error(error)
      }
    },
    setCurrentPeriod() {
      this.currentPeriod = this.getCurrentPeriod()
    },
    getCurrentPeriod() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      return `${year}-${month}`
    },
    getStatusType(status) {
      switch (status) {
        case 'Normal':
          return 'success'
        case 'Warning':
          return 'warning'
        case 'Critical':
          return 'danger'
        default:
          return 'info'
      }
    },
    navigateTo(path) {
      this.$router.push(path)
    }
  }
}
</script>

<style scoped>
.kri-management-container {
  padding: 20px;
}

.page-title {
  margin-bottom: 30px;
  color: #303133;
  text-align: center;
}

.dashboard-cards {
  margin-bottom: 30px;
}

.dashboard-card {
  height: 200px;
  cursor: pointer;
  transition: all 0.3s;
}

.dashboard-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  font-weight: bold;
  font-size: 18px;
}

.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 120px;
}

.card-content i {
  font-size: 40px;
  margin-bottom: 10px;
  color: #409EFF;
}

.card-stat {
  margin-top: 10px;
  font-weight: bold;
  color: #606266;
}
</style>
