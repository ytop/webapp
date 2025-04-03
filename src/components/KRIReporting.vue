<template>
  <div class="kri-reporting-container">
    <div class="page-header">
      <h1 class="page-title">KRI Reporting & Analytics</h1>
    </div>

    <!-- Report Parameters -->
    <el-card class="filter-card">
      <div slot="header" class="card-header">
        <span>Report Parameters</span>
      </div>
      
      <el-form :inline="true" class="report-filters">
        <el-form-item label="Date Range:">
          <el-date-picker
            v-model="dateRange"
            type="monthrange"
            range-separator="To"
            start-placeholder="Start Month"
            end-placeholder="End Month"
            format="yyyy-MM"
            value-format="yyyy-MM"
            :picker-options="{ disabledDate: disableFutureDates }"
          />
        </el-form-item>
        
        <el-form-item label="Risk Category:">
          <el-select v-model="selectedCategory" placeholder="All Categories" clearable>
            <el-option
              v-for="item in riskCategories"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="Status:">
          <el-select v-model="selectedStatus" placeholder="All Statuses" clearable>
            <el-option label="Normal" value="Normal" />
            <el-option label="Warning" value="Warning" />
            <el-option label="Critical" value="Critical" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="generateReport" :loading="loading">Generate Report</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Summary Cards -->
    <el-row :gutter="20" class="summary-cards" v-if="reportData.length > 0">
      <el-col :span="8">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-value">{{ totalKRIs }}</div>
          <div class="summary-label">Total KRIs</div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover" class="summary-card warning">
          <div class="summary-value">{{ warningCount }}</div>
          <div class="summary-label">Warning Status</div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover" class="summary-card critical">
          <div class="summary-value">{{ criticalCount }}</div>
          <div class="summary-label">Critical Status</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- KRI Trend Charts -->
    <el-card class="chart-card" v-if="reportData.length > 0">
      <div slot="header" class="card-header">
        <span>KRI Trends</span>
        <el-select v-model="selectedKRIForChart" placeholder="Select KRI" size="small" style="width: 200px">
          <el-option
            v-for="item in reportData"
            :key="item.kriId"
            :label="`${item.kriId} - ${item.kriDesc}`"
            :value="item.kriId"
          />
        </el-select>
      </div>
      
      <div class="chart-container" v-if="selectedKRIForChart">
        <div ref="kriTrendChart" class="chart"></div>
      </div>
      
      <div class="empty-chart" v-else>
        <p>Select a KRI to view trend data</p>
      </div>
    </el-card>

    <!-- Detailed Report Table -->
    <el-card class="report-card" v-if="reportData.length > 0">
      <div slot="header" class="card-header">
        <span>Detailed Report</span>
        <el-button size="small" type="primary" icon="el-icon-download" @click="exportReport">Export</el-button>
      </div>
      
      <el-table
        :data="filteredReportData"
        border
        style="width: 100%"
      >
        <el-table-column prop="kriId" label="KRI ID" width="120" sortable />
        <el-table-column prop="kriDesc" label="KRI Description" min-width="250" show-overflow-tooltip />
        <el-table-column prop="riskCategory" label="Risk Category" width="150" sortable />
        <el-table-column label="Latest Value" width="120" sortable>
          <template slot-scope="scope">
            {{ getLatestValue(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column label="Latest Status" width="120" sortable>
          <template slot-scope="scope">
            <el-tag :type="getStatusType(getLatestStatus(scope.row))">
              {{ getLatestStatus(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Trend" width="120">
          <template slot-scope="scope">
            <span class="trend-indicator">
              <i :class="getTrendIcon(scope.row)" :style="{ color: getTrendColor(scope.row) }"></i>
              {{ getTrendLabel(scope.row) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="Details" width="100">
          <template slot-scope="scope">
            <el-button 
              size="mini" 
              type="text" 
              @click="showKRIDetails(scope.row)"
            >
              View Details
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card v-else class="empty-state-card">
      <div class="empty-state">
        <i class="el-icon-data-analysis empty-icon"></i>
        <p>Select parameters and generate a report to view KRI data</p>
      </div>
    </el-card>

    <!-- KRI Details Dialog -->
    <el-dialog 
      :title="`KRI Details: ${selectedKRIDetails.kriId}`" 
      :visible.sync="detailsDialogVisible"
      width="70%"
    >
      <div v-if="selectedKRIDetails.kriId">
        <h3>{{ selectedKRIDetails.kriDesc }}</h3>
        <p><strong>Risk Category:</strong> {{ selectedKRIDetails.riskCategory }}</p>
        
        <el-table
          :data="selectedKRIDetails.data"
          border
          style="width: 100%; margin-top: 20px"
        >
          <el-table-column prop="period" label="Period" width="120" sortable />
          <el-table-column prop="value" label="Value" width="120" sortable />
          <el-table-column prop="status" label="Status" width="120">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getKRIDataSummary } from '@/api/kri'

// We'll simulate importing a charting library
// In a real application, you would import a library like ECharts or Chart.js
const mockChartLibrary = {
  init: (element) => {
    return {
      setOption: (options) => {
        // In a real app, this would render the chart
        console.log('Chart options set:', options)
        
        // For our mock, we'll just add some visual representation
        if (element) {
          element.innerHTML = `
            <div style="text-align: center; padding: 20px;">
              <div style="font-weight: bold; margin-bottom: 10px;">Chart Visualization</div>
              <div style="display: flex; height: 200px; align-items: flex-end; justify-content: space-around; border-bottom: 1px solid #ccc; border-left: 1px solid #ccc;">
                ${options.series[0].data.map((value, index) => {
                  const height = (value / Math.max(...options.series[0].data)) * 180
                  const color = value >= options.markLine.data[1].yAxis ? '#F56C6C' : 
                                value >= options.markLine.data[0].yAxis ? '#E6A23C' : '#67C23A'
                  return `
                    <div style="display: flex; flex-direction: column; align-items: center;">
                      <div style="width: 30px; height: ${height}px; background-color: ${color}; margin-bottom: 5px;"></div>
                      <div style="font-size: 12px;">${options.xAxis.data[index]}</div>
                    </div>
                  `
                }).join('')}
              </div>
              <div style="display: flex; justify-content: space-between; margin-top: 10px;">
                <div><span style="display: inline-block; width: 10px; height: 10px; background-color: #67C23A; margin-right: 5px;"></span>Normal</div>
                <div><span style="display: inline-block; width: 10px; height: 10px; background-color: #E6A23C; margin-right: 5px;"></span>Warning (${options.markLine.data[0].yAxis})</div>
                <div><span style="display: inline-block; width: 10px; height: 10px; background-color: #F56C6C; margin-right: 5px;"></span>Critical (${options.markLine.data[1].yAxis})</div>
              </div>
            </div>
          `
        }
      },
      resize: () => {
        // In a real app, this would resize the chart
        console.log('Chart resized')
      }
    }
  }
}

export default {
  name: 'KriReporting',
  data() {
    return {
      dateRange: [],
      selectedCategory: '',
      selectedStatus: '',
      loading: false,
      reportData: [],
      selectedKRIForChart: '',
      chart: null,
      detailsDialogVisible: false,
      selectedKRIDetails: {},
      riskCategories: [
        'Credit Risk',
        'Market Risk',
        'Liquidity Risk',
        'Operational Risk',
        'Compliance Risk',
        'Strategic Risk',
        'Reputational Risk',
        'Legal Risk',
        'Cyber Risk',
        'Other'
      ]
    }
  },
  computed: {
    totalKRIs() {
      return this.reportData.length
    },
    warningCount() {
      return this.reportData.filter(kri => 
        this.getLatestStatus(kri) === 'Warning'
      ).length
    },
    criticalCount() {
      return this.reportData.filter(kri => 
        this.getLatestStatus(kri) === 'Critical'
      ).length
    },
    filteredReportData() {
      let filtered = [...this.reportData]
      
      if (this.selectedCategory) {
        filtered = filtered.filter(item => item.riskCategory === this.selectedCategory)
      }
      
      if (this.selectedStatus) {
        filtered = filtered.filter(item => this.getLatestStatus(item) === this.selectedStatus)
      }
      
      return filtered
    }
  },
  created() {
    // Set default date range to last 6 months
    const endDate = new Date()
    const startDate = new Date()
    startDate.setMonth(startDate.getMonth() - 5) // 6 months including current
    
    this.dateRange = [
      this.formatYearMonth(startDate),
      this.formatYearMonth(endDate)
    ]
  },
  mounted() {
    // Initialize with default parameters
    this.generateReport()
  },
  watch: {
    selectedKRIForChart(newVal) {
      if (newVal) {
        this.$nextTick(() => {
          this.renderChart()
        })
      }
    }
  },
  methods: {
    async generateReport() {
      if (!this.dateRange || this.dateRange.length !== 2) {
        this.$message.warning('Please select a valid date range.')
        return
      }
      
      this.loading = true
      
      try {
        const [startPeriod, endPeriod] = this.dateRange
        const response = await getKRIDataSummary(startPeriod, endPeriod)
        
        this.reportData = response
        
        // Select first KRI for chart if available
        if (this.reportData.length > 0 && !this.selectedKRIForChart) {
          this.selectedKRIForChart = this.reportData[0].kriId
        }
        
        // Render chart if KRI is selected
        if (this.selectedKRIForChart) {
          this.$nextTick(() => {
            this.renderChart()
          })
        }
      } catch (error) {
        this.$message.error('Failed to generate report.')
        console.error(error)
      } finally {
        this.loading = false
      }
    },
    renderChart() {
      const selectedKRI = this.reportData.find(kri => kri.kriId === this.selectedKRIForChart)
      
      if (!selectedKRI) return
      
      // Sort data by period
      const sortedData = [...selectedKRI.data].sort((a, b) => a.period.localeCompare(b.period))
      
      // Prepare chart data
      const periods = sortedData.map(item => item.period)
      const values = sortedData.map(item => item.value)
      
      // Get thresholds
      const kri = this.reportData.find(k => k.kriId === this.selectedKRIForChart)
      const warningThreshold = kri ? kri.warningThreshold : 0
      const criticalThreshold = kri ? kri.criticalThreshold : 0
      
      // Initialize chart
      if (!this.chart) {
        this.chart = mockChartLibrary.init(this.$refs.kriTrendChart)
      }
      
      // Set chart options
      this.chart.setOption({
        title: {
          text: `${selectedKRI.kriId} - ${selectedKRI.kriDesc}`,
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: periods
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          data: values,
          type: 'bar'
        }],
        markLine: {
          data: [
            { yAxis: warningThreshold, lineStyle: { color: '#E6A23C' } },
            { yAxis: criticalThreshold, lineStyle: { color: '#F56C6C' } }
          ]
        }
      })
    },
    showKRIDetails(kri) {
      this.selectedKRIDetails = { ...kri }
      this.detailsDialogVisible = true
    },
    getLatestValue(kri) {
      if (!kri.data || kri.data.length === 0) return 'N/A'
      
      // Sort data by period (descending) and get the latest
      const sortedData = [...kri.data].sort((a, b) => b.period.localeCompare(a.period))
      return sortedData[0].value
    },
    getLatestStatus(kri) {
      if (!kri.data || kri.data.length === 0) return 'N/A'
      
      // Sort data by period (descending) and get the latest
      const sortedData = [...kri.data].sort((a, b) => b.period.localeCompare(a.period))
      return sortedData[0].status
    },
    getTrendIcon(kri) {
      if (!kri.data || kri.data.length < 2) return 'el-icon-minus'
      
      // Sort data by period
      const sortedData = [...kri.data].sort((a, b) => a.period.localeCompare(b.period))
      
      // Get last two values
      const lastValue = sortedData[sortedData.length - 1].value
      const previousValue = sortedData[sortedData.length - 2].value
      
      if (lastValue > previousValue) return 'el-icon-top'
      if (lastValue < previousValue) return 'el-icon-bottom'
      return 'el-icon-minus'
    },
    getTrendColor(kri) {
      const icon = this.getTrendIcon(kri)
      
      if (icon === 'el-icon-top') {
        // For KRIs, increasing is typically bad
        return '#F56C6C'
      } else if (icon === 'el-icon-bottom') {
        // Decreasing is typically good
        return '#67C23A'
      }
      return '#909399'
    },
    getTrendLabel(kri) {
      const icon = this.getTrendIcon(kri)
      
      if (icon === 'el-icon-top') return 'Increasing'
      if (icon === 'el-icon-bottom') return 'Decreasing'
      return 'Stable'
    },
    formatYearMonth(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      return `${year}-${month}`
    },
    disableFutureDates(time) {
      return time.getTime() > Date.now()
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
    exportReport() {
      // In a real application, this would generate a CSV or Excel file
      this.$message.success('Report exported successfully!')
    }
  }
}
</script>

<style scoped>
.kri-reporting-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  margin: 0 0 20px 0;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.summary-cards {
  margin-bottom: 20px;
}

.summary-card {
  text-align: center;
  padding: 20px;
}

.summary-card.warning .summary-value {
  color: #E6A23C;
}

.summary-card.critical .summary-value {
  color: #F56C6C;
}

.summary-value {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 10px;
}

.summary-label {
  font-size: 16px;
  color: #606266;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
}

.chart {
  width: 100%;
  height: 100%;
}

.empty-chart {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  color: #909399;
}

.report-card {
  margin-bottom: 20px;
}

.trend-indicator {
  display: flex;
  align-items: center;
}

.trend-indicator i {
  margin-right: 5px;
}

.empty-state-card {
  text-align: center;
  padding: 40px 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.empty-icon {
  font-size: 48px;
  color: #909399;
  margin-bottom: 20px;
}
</style>
