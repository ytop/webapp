<template>
  <div class="kri-data-collection-container">
    <div class="page-header">
      <h1 class="page-title">KRI Data Collection</h1>
    </div>

    <!-- Period Selection and Filters -->
    <el-form :inline="true" class="period-selection">
      <el-form-item label="Collection Period:">
        <el-date-picker
          v-model="selectedPeriod"
          type="month"
          format="yyyy-MM"
          value-format="yyyy-MM"
          placeholder="Select month"
          :picker-options="{ disabledDate: disableFutureDates }"
          @change="handlePeriodChange"
        />
      </el-form-item>
      <el-form-item label="Risk Category:">
        <el-select v-model="filterCategory" placeholder="All Categories" clearable @change="applyFilters">
          <el-option
            v-for="category in riskCategories"
            :key="category"
            :label="category"
            :value="category">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="Status:">
        <el-select v-model="filterStatus" placeholder="All Statuses" clearable @change="applyFilters">
          <el-option label="Normal" value="Normal"></el-option>
          <el-option label="Warning" value="Warning"></el-option>
          <el-option label="Critical" value="Critical"></el-option>
        </el-select>
      </el-form-item>
    </el-form>

    <!-- Data Collection Form -->
    <el-card v-if="selectedPeriod" class="data-collection-card">
      <div slot="header" class="card-header">
        <span>Data Collection for {{ selectedPeriod }}</span>
      </div>

      <div class="table-actions">
        <div class="left-actions">
          <el-button size="small" type="primary" icon="el-icon-upload2" @click="showImportDialog">Import Data</el-button>
          <el-button size="small" type="success" icon="el-icon-download" @click="exportData">Export to Excel</el-button>
        </div>
        <div class="right-actions">
          <el-input
            placeholder="Search KRIs"
            v-model="searchQuery"
            prefix-icon="el-icon-search"
            clearable
            @input="applyFilters"
            style="width: 250px"
          ></el-input>
        </div>
      </div>

      <el-table
        :data="filteredKRIData"
        border
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="kriId" label="KRI ID" width="120" />
        <el-table-column prop="kriDesc" label="KRI Description" min-width="250" show-overflow-tooltip />
        <el-table-column prop="riskCategory" label="Risk Category" width="150" />
        <el-table-column label="Thresholds" width="180">
          <template slot-scope="scope">
            <div>
              <el-tag type="warning">Warning: {{ scope.row.warningThreshold }}</el-tag>
            </div>
            <div style="margin-top: 5px;">
              <el-tag type="danger">Critical: {{ scope.row.criticalThreshold }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="Value" width="200">
          <template slot-scope="scope">
            <el-input-number
              v-model="scope.row.value"
              :min="0"
              :precision="0"
              :disabled="scope.row.submitted"
              @change="(value) => handleValueChange(scope.row, value)"
            />
          </template>
        </el-table-column>
        <el-table-column label="Status" width="120">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Comments" min-width="200">
          <template slot-scope="scope">
            <el-input
              v-model="scope.row.comments"
              type="textarea"
              :rows="2"
              :disabled="scope.row.submitted"
              placeholder="Add comments..."
            />
          </template>
        </el-table-column>
        <el-table-column label="Approval Status" width="120">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.approvalStatus === 'Approved'" type="success">Approved</el-tag>
            <el-tag v-else-if="scope.row.approvalStatus === 'Rejected'" type="danger">Rejected</el-tag>
            <el-tag v-else-if="scope.row.submitted" type="warning">Pending</el-tag>
            <el-tag v-else type="info">Not Submitted</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="150">
          <template slot-scope="scope">
            <el-button
              v-if="!scope.row.submitted"
              size="mini"
              type="primary"
              @click="submitKRIData(scope.row)"
            >
              Submit
            </el-button>
            <el-button
              v-if="scope.row.submitted"
              size="mini"
              type="info"
              @click="showHistoryDialog(scope.row)"
            >
              History
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="bulk-actions" v-if="kriDataCollection.length > 0">
        <el-button-group>
          <el-button type="primary" @click="submitAllKRIData" :disabled="!hasUnsubmittedItems">Submit All</el-button>
          <el-button type="info" @click="refreshData">Refresh Data</el-button>
        </el-button-group>
      </div>

      <!-- History Dialog -->
      <el-dialog title="KRI Data History" :visible.sync="historyDialogVisible" width="60%">
        <div v-if="selectedKRI">
          <h3>{{ selectedKRI.kriId }} - {{ selectedKRI.kriDesc }}</h3>
          <el-table :data="kriHistory" border style="width: 100%">
            <el-table-column prop="submissionDate" label="Date" width="120" sortable />
            <el-table-column prop="value" label="Value" width="100" sortable />
            <el-table-column prop="status" label="Status" width="120">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="submittedBy" label="Submitted By" width="150" />
            <el-table-column prop="comments" label="Comments" min-width="200" show-overflow-tooltip />
          </el-table>
        </div>
      </el-dialog>

      <!-- Import Dialog -->
      <el-dialog title="Import KRI Data" :visible.sync="importDialogVisible" width="40%">
        <el-upload
          class="upload-demo"
          drag
          action="#"
          :auto-upload="false"
          :on-change="handleFileChange"
          :file-list="fileList"
          :limit="1"
        >
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">Drop file here or <em>click to upload</em></div>
          <div class="el-upload__tip" slot="tip">Only Excel or CSV files with the correct format</div>
        </el-upload>
        <div class="import-actions">
          <el-button type="primary" @click="importData" :disabled="!selectedFile">Import Data</el-button>
          <el-button @click="downloadTemplate">Download Template</el-button>
        </div>
      </el-dialog>
    </el-card>

    <el-card v-else class="empty-state-card">
      <div class="empty-state">
        <i class="el-icon-date empty-icon"></i>
        <p>Please select a collection period to start</p>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getKRIs, getKRIData, submitKRIData, getKRIHistory, exportKRIDataToCSV, importKRIDataFromCSV } from '@/api/kri'
import XLSX from 'xlsx'

export default {
  name: 'KriDataCollection',
  data() {
    return {
      selectedPeriod: '',
      kriList: [],
      kriDataCollection: [],
      loading: false,
      submitter: '',
      // Filters
      filterCategory: '',
      filterStatus: '',
      searchQuery: '',
      // Dialogs
      historyDialogVisible: false,
      importDialogVisible: false,
      selectedKRI: null,
      kriHistory: [],
      // File upload
      fileList: [],
      selectedFile: null,
      // Risk categories
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
  created() {
    // Set default period to current month
    this.selectedPeriod = this.getCurrentPeriod()

    // Get user name for submission
    this.submitter = localStorage.getItem('userName') || 'Current User'

    // Fetch KRIs
    this.fetchKRIs()
  },
  methods: {
    async fetchKRIs() {
      this.loading = true
      try {
        const response = await getKRIs()
        this.kriList = response

        // If period is selected, prepare data collection
        if (this.selectedPeriod) {
          this.prepareDataCollection()
        }
      } catch (error) {
        this.$message.error('Failed to fetch KRI data.')
        console.error(error)
      } finally {
        this.loading = false
      }
    },
    async prepareDataCollection() {
      this.loading = true
      try {
        // Get existing data for the selected period
        const existingData = await getKRIData(this.selectedPeriod)

        // Prepare data collection items
        this.kriDataCollection = this.kriList
          .filter(kri => kri.status === 'Active') // Only include active KRIs
          .map(kri => {
            // Check if data already exists for this KRI and period
            const existingEntry = existingData.find(data => data.kriId === kri.kriId)

            if (existingEntry) {
              // Use existing data
              return {
                ...kri,
                value: existingEntry.value,
                status: existingEntry.status,
                comments: existingEntry.comments,
                submitted: true,
                submittedBy: existingEntry.submittedBy,
                submissionDate: existingEntry.submissionDate,
                approvalStatus: existingEntry.approvalStatus || 'Pending',
                approvedBy: existingEntry.approvedBy,
                approvalDate: existingEntry.approvalDate,
                attachments: existingEntry.attachments || [],
                history: existingEntry.history || []
              }
            } else {
              // Create new entry
              return {
                ...kri,
                value: 0,
                status: 'Normal',
                comments: '',
                submitted: false,
                period: this.selectedPeriod,
                approvalStatus: null,
                approvedBy: null,
                approvalDate: null,
                attachments: [],
                history: []
              }
            }
          })

        // Apply any active filters
        this.applyFilters()
      } catch (error) {
        this.$message.error('Failed to prepare data collection.')
        console.error(error)
      } finally {
        this.loading = false
      }
    },
    handlePeriodChange() {
      if (this.selectedPeriod) {
        this.prepareDataCollection()
      } else {
        this.kriDataCollection = []
      }
    },
    handleValueChange(row, value) {
      // Update status based on thresholds
      if (value >= row.criticalThreshold) {
        row.status = 'Critical'
      } else if (value >= row.warningThreshold) {
        row.status = 'Warning'
      } else {
        row.status = 'Normal'
      }
    },
    async submitKRIData(row) {
      try {
        const dataEntry = {
          kriId: row.kriId,
          period: this.selectedPeriod,
          value: row.value,
          status: row.status,
          comments: row.comments,
          submittedBy: this.submitter
        }

        const result = await submitKRIData(dataEntry)

        if (result.success) {
          this.$message.success(`Data for ${row.kriId} submitted successfully!`)

          // Create a new object with updated properties to avoid race conditions
          const updatedRow = {
            ...row,
            submitted: true,
            submissionDate: result.data.submissionDate,
            submittedBy: result.data.submittedBy,
            approvalStatus: result.data.approvalStatus || 'Pending'
          }

          // Find the index of the row in the collection and replace it
          const index = this.kriDataCollection.findIndex(item => item.kriId === row.kriId)
          if (index !== -1) {
            this.$set(this.kriDataCollection, index, updatedRow)
          }
        } else {
          this.$message.error(result.message || 'Submission failed.')
        }
      } catch (error) {
        this.$message.error('Failed to submit KRI data.')
        console.error(error)
      }
    },
    async submitAllKRIData() {
      const unsubmittedItems = this.kriDataCollection.filter(item => !item.submitted)

      if (unsubmittedItems.length === 0) {
        this.$message.info('All items are already submitted.')
        return
      }

      this.loading = true

      try {
        // Create a map to track updates
        const updatedItems = new Map()

        for (const item of unsubmittedItems) {
          const dataEntry = {
            kriId: item.kriId,
            period: this.selectedPeriod,
            value: item.value,
            status: item.status,
            comments: item.comments,
            submittedBy: this.submitter
          }

          const result = await submitKRIData(dataEntry)

          if (result.success) {
            // Store the updated item in our map
            updatedItems.set(item.kriId, {
              ...item,
              submitted: true,
              submissionDate: result.data.submissionDate,
              submittedBy: result.data.submittedBy,
              approvalStatus: result.data.approvalStatus || 'Pending'
            })
          }
        }

        // Apply all updates at once to avoid race conditions
        if (updatedItems.size > 0) {
          // Create a new array with updated items
          const updatedCollection = this.kriDataCollection.map(item => {
            const updatedItem = updatedItems.get(item.kriId)
            return updatedItem || item
          })

          // Replace the entire collection
          this.kriDataCollection = updatedCollection

          this.$message.success('All KRI data submitted successfully!')
        }
      } catch (error) {
        this.$message.error('Failed to submit all KRI data.')
        console.error(error)
      } finally {
        this.loading = false
      }
    },
    getCurrentPeriod() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
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

    // Filter methods
    applyFilters() {
      // This method will be called when filters change
      // No need to implement the actual filtering logic as it's handled by the computed property
    },

    // Refresh data
    refreshData() {
      this.prepareDataCollection()
    },

    // Show history dialog
    async showHistoryDialog(row) {
      this.selectedKRI = row
      this.historyDialogVisible = true

      try {
        // If history is already in the row data, use it
        if (row.history && row.history.length > 0) {
          this.kriHistory = row.history
        } else {
          // Otherwise fetch it from the API
          this.kriHistory = await getKRIHistory(row.kriId, this.selectedPeriod)
        }
      } catch (error) {
        this.$message.error('Failed to load KRI history.')
        console.error(error)
      }
    },

    // Import/Export methods
    showImportDialog() {
      this.importDialogVisible = true
      this.fileList = []
      this.selectedFile = null
    },

    handleFileChange(file) {
      this.selectedFile = file
    },

    async importData() {
      if (!this.selectedFile) {
        this.$message.warning('Please select a file to import')
        return
      }

      // In a real implementation, we would parse the file and send the data to the API
      this.$message.info('Import functionality would be implemented here')
      this.importDialogVisible = false
    },

    exportData() {
      try {
        const csvData = exportKRIDataToCSV(this.selectedPeriod)

        // Convert to Excel
        const worksheet = XLSX.utils.json_to_sheet(csvData)
        const workbook = XLSX.utils.book_new()
        XLSX.utils.book_append_sheet(workbook, worksheet, 'KRI Data')

        // Generate file name
        const fileName = `KRI_Data_${this.selectedPeriod}.xlsx`

        // Trigger download
        XLSX.writeFile(workbook, fileName)

        this.$message.success('Data exported successfully')
      } catch (error) {
        this.$message.error('Failed to export data')
        console.error(error)
      }
    },

    downloadTemplate() {
      // Create a template for data import
      const template = this.kriList
        .filter(kri => kri.status === 'Active')
        .map(kri => ({
          'KRI ID': kri.kriId,
          'KRI Description': kri.kriDesc,
          'Risk Category': kri.riskCategory,
          'Value': '',
          'Comments': ''
        }))

      // Convert to Excel
      const worksheet = XLSX.utils.json_to_sheet(template)
      const workbook = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(workbook, worksheet, 'KRI Template')

      // Generate file name
      const fileName = `KRI_Template_${this.selectedPeriod}.xlsx`

      // Trigger download
      XLSX.writeFile(workbook, fileName)
    }
  },

  computed: {
    // Filtered KRI data based on search and filters
    filteredKRIData() {
      let result = [...this.kriDataCollection]

      // Apply category filter
      if (this.filterCategory) {
        result = result.filter(item => item.riskCategory === this.filterCategory)
      }

      // Apply status filter
      if (this.filterStatus) {
        result = result.filter(item => item.status === this.filterStatus)
      }

      // Apply search query
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase()
        result = result.filter(item =>
          item.kriId.toLowerCase().includes(query) ||
          item.kriDesc.toLowerCase().includes(query) ||
          item.riskCategory.toLowerCase().includes(query)
        )
      }

      return result
    },

    // Check if there are any unsubmitted items
    hasUnsubmittedItems() {
      return this.kriDataCollection.some(item => !item.submitted)
    }
  }
}
</script>

<style scoped>
.kri-data-collection-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  margin: 0 0 20px 0;
  color: #303133;
}

.period-selection {
  margin-bottom: 20px;
}

.data-collection-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.bulk-actions {
  margin-top: 20px;
  text-align: right;
}

.table-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.import-actions {
  margin-top: 20px;
  text-align: center;
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
