<template>
  <el-row>
    <el-row>

      <el-col>

        <div class="section-class">
          <!-- <el-card class="filter-card" shadow="hover"> -->
          <el-card style="border-radius: 15px; background-color:#ffffff">
          <div slot="header" class="card-header">
            <span class="title">Filters</span>
          </div>
          
          <el-row style="height: 40px;">


            <el-form :model="filterForm" :inline="true" size="small" class="filter-form">

                <el-col :span="4">
                  <el-form-item class="keyword-input">
                      <div class="search-input-group">
                        <el-input clearable v-model="filterForm.keyword" :placeholder="'Enter keyword'" style="width: 250px;"/>
                      </div>
                    </el-form-item>
                </el-col>

                <el-col :span="4">
                  <el-form-item label="From Date:">
                    <el-date-picker :clearable="false" v-model="filterForm.fromDate" type="date" placeholder="Please select start time" style="width:150px;" />
                  </el-form-item>
                </el-col>

                <el-col :span="4">
                  <el-form-item label="To Date:">
                    <el-date-picker :clearable="false" v-model="filterForm.toDate" type="date" placeholder="Please select end time" style="width:150px;" />
                  </el-form-item>
                </el-col>
                
                <el-col :span="4">
                  <el-form-item label="Risk Type:">
                    <el-select v-model="filterForm.riskType" filterable clearable placeholder="Select" style="width:150px">
                      <el-option
                        v-for="option in riskTypeOpts"
                        :key="option.value"
                        :label="option.label"
                        :value="option.value"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>

                <el-col :span="4">
                  <el-form-item label="Owner Dept:">
                    <el-select v-model="filterForm.ownerDept" filterable clearable placeholder="Select" style="width:150px">
                      <el-option
                        v-for="option in ownerDeptOptions"
                        :key="option.value"
                        :label="option.label"
                        :value="option.value"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>

                <el-col :span="4">
                <el-form-item>
                  <el-col>
                    <el-button icon="el-icon-download" size="small" :loading="exportLoading" @click="handleExport" style="width:210px; background: #B0E0E6">Export</el-button>
                  </el-col>
                </el-form-item>
                </el-col>
            </el-form>
          </el-row>
        </el-card>
      </div>

      </el-col>
    </el-row>

    <el-row>
      <div class="section-class">
        <el-card style="border-radius: 15px; background-color:#c3c6c7">
          <transition name="fade" mode="out-in">
            <incident-table :table-data="tableData" />
          </transition>
        </el-card>
      </div>
    </el-row>
  </el-row>

</template>

<script>
import * as XLSX from 'xlsx'
import IncidentTable from './IncidentTable.vue'
import {
  getIncidentList, getIncidentData
} from '@/api/mra'

import {
  STATUS_ITEMS,
  DUE_DATE_OPTIONS,
  RISK_RATING_OPTIONS,
  OWNER_DEPT_OPTIONS,
  RISK_TYPE_OPTIONS,
  RISK_TYPE_OPTS
} from './constants'

export default {
  name: 'Incident',

  components: {
    IncidentTable
  },

  data() {
    return {
      tableData: [],
      TABLE_DATA: [],
      loading: false,
      selectedIncident: null,
      isEditMode: false,
      showForm: false,
      exportLoading: false,
      filterForm: {
        keyWord: '',
        fromDate: new Date(Date.now() - 6 * 24 * 60 * 60 * 1000),
        toDate: new Date(),
        dueDate: '',
        ownerDept: '',
        riskType: ''
      },
      queryParams: {
        identificationDateFrom: '',
        identificationDateTo: '',
        ownerDept:'',
        primaryRiskType:''
      },
      statusItems: STATUS_ITEMS,
      dueDateOptions: DUE_DATE_OPTIONS,
      ownerDeptOptions: OWNER_DEPT_OPTIONS,
      riskTypeOptions: RISK_TYPE_OPTIONS,
      riskTypeOpts: RISK_TYPE_OPTS
    }
  },

  watch: {
    filterForm: {
      handler: 'applyFilters',
      deep: true
    }
  },

  created() {
    this.fetchIncidents()
  },

  methods: {
    async fetchIncidents(params = {}) {
      try {
        this.loading = true
        const response = await getIncidentList(params)
        this.tableData = response.data.map(row => {
          return {
            ...row,
            identificationDate: row.identificationDate !== undefined ? row.identificationDate.split("T")[0] : row.identificationDate,
            occurrenceDate: row.occurrenceDate !== undefined ? row.occurrenceDate.split("T")[0] : row.occurrenceDate,
            dueDate: row.dueDate !== undefined ? row.dueDate.split("T")[0] : row.dueDate
          }
        })
      this.TABLE_DATA = this.tableData
      
      // set to be empty
      this.filterForm = {
        keyWord: '',
        dueDate: '',
        riskRating: '',
        ownerDept: '',
        riskType: '',
        fromDate: new Date(Date.now() - 6 * 24 * 60 * 60 * 1000),
        toDate: new Date()
      }

      this.applyFilters()
      } catch (error) {
        console.error('Error fetching incidents:', error)
        this.$message.error('Failed to fetch incidents')
      } finally {
        this.loading = false
      }
    },
    
    applyFilters() {
      let filteredData = this.TABLE_DATA


      if (this.filterForm.fromDate) {
        const t = new Date(this.filterForm.fromDate)
        filteredData = filteredData.filter(item => (new Date(item.identificationDate.substring(0, 10)).toISOString().split("T")[0]) >= t.toISOString().split("T")[0])
        this.queryParams.identificationDateFrom = t.toISOString().split("T")[0]
      } else {
        const t = new Date(Date.now() - 6 * 24 * 60 * 60 * 1000)
        this.filterForm.fromDate = new Date(Date.now() - 6 * 24 * 60 * 60 * 1000)
        filteredData = filteredData.filter(item => (new Date(item.identificationDate.substring(0, 10)).toISOString().split("T")[0]) >= t.toISOString().split("T")[0])
        this.queryParams.identificationDateFrom = t.toISOString().split("T")[0]
      }

      if (this.filterForm.toDate) {
        const t = new Date(this.filterForm.toDate)
        filteredData = filteredData.filter(item => (new Date(item.identificationDate.substring(0, 10)).toISOString().split("T")[0]) <= t.toISOString().split("T")[0])
        this.queryParams.identificationDateTo = t.toISOString().split("T")[0]
      } else {
        const t = new Date()
        this.filterForm.toDate = new Date(Date.now())
        filteredData = filteredData.filter(item => (new Date(item.identificationDate.substring(0, 10)).toISOString().split("T")[0]) <= t.toISOString().split("T")[0])
        this.queryParams.identificationDateTo = t.toISOString().split("T")[0]
      }

       if (this.filterForm.riskType) {
        filteredData = filteredData.filter(item => item.primaryRiskType === this.filterForm.riskType)
        this.queryParams.primaryRiskType = this.filterForm.riskType
      }

      // Apply owner department filter
      if (this.filterForm.ownerDept) {
        filteredData = filteredData.filter(item => item.ownerDepartment === this.filterForm.ownerDept)
        this.queryParams.ownerDept = this.filterForm.ownerDept
      }


      // Apply keyword
      if (this.filterForm.keyword) {
        const keyword = this.filterForm.keyword.toLowerCase()
        filteredData = filteredData.filter(row => {
          return Object.entries(row).some(([key, value]) => {
            if (value == null) return false
            const stringValue = value.toString().toLowerCase()
            return stringValue.includes(keyword)
          })
        })

      }
      this.tableData = filteredData
    },
    handleExport() {
      const exportData = this.tableData.map(item => ({
        'Incident No'    : item.incidentNo,
        'Incident Type'    : item.incidentType,
        'Status'    : item.incidentStatus,
        'Occurrence Date'    : item.occurrenceDate,
        'Identification Date'    : item.identificationDate,
        'Title'    : item.incidentTitle,
        'Incident Identifier Department'    : item.identifiedBy,
        'Incident Owner Department'    : item.ownerDepartment,
        'Description'    : item.incidentDescription,
        'Primary Impacted Risk Area'    : item.primaryRiskType,
        'IRM Primary Owner Dept'    : item.primaryOwnerDept,
        'IRM Secondary Owner Dept'    : item.secondaryOwnerDept
      }))

      const ws = XLSX.utils.json_to_sheet(exportData)
      const wb = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(wb, ws, 'Sheet1')     
      XLSX.writeFile(wb, 'IncidentManualExport.xlsx')  
    },

    createDownloadDom(data, name) {
      const content = data
      const fileName = name
      const blob = new Blob([content], {
        type: 'application/vnd.ms-excel'
      }) // 新建blob对象
      if ('download' in document.createElement('a')) {
        // 非IE下载
        const elink = document.createElement('a')
        elink.download = fileName
        elink.style.display = 'none'
        elink.href = URL.createObjectURL(blob)
        document.body.appendChild(elink)
        elink.click()
        URL.revokeObjectURL(elink.href) // 释放URL 对象
        document.body.removeChild(elink)
      } else {
        // IE10+下载
        navigator.msSaveBlob(blob, fileName)
      }
      this.$message({
        message: 'Success',
        type: 'success',
        showClose: true
      })
    }
  }
}
</script>

<style scoped>
.grid-container {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 20px;
}

.left-column {
  grid-column: 1 / 2;
}

.right-column {
  grid-column: 2 / 3;
}

.status-card {
  margin-bottom: 20px;
  height: calc(100% - 20px);
}

.search-card {
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 16px;
  font-weight: bold;
}

.subtitle {
  font-size: 14px;
  color: #909399;
}

.status-section {
  display: flex;
  flex-direction: column;
}

.status-item {
  cursor: pointer;
  margin-bottom: 10px;
}

.status-button {
  width: 100%;
  text-align: left;
}

.status-active {
  background-color: #f5f7fa;
}

.status-counts {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.count-item {
  font-size: 14px;
  color: #606266;
}

.search-form {
  display: flex;
  justify-content: space-between;
  height:70px;
}

.keyword-input {
  flex: 1;
  margin-bottom: 0;
}

.search-button {
  margin-left: 0;
}

.search-input-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px;
}

.hover-effect:hover {
  background-color: #f5f7fa;
}

.is-selected {
  background-color: #e6f7ff;
  border-color: #91d5ff;
  color: #409eff;
}

.right-column {
  display: flex;
  flex-direction: column;
}

.search-card,
.filter-card {
  flex: 1;
}

.search-form {
  display: flex;
  align-items: left;
}

.search-button {
  margin-left: 0;
  margin-top: 0;
}

.button-container {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
}


.section-class {
  padding: 10px;
}


</style>
