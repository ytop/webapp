<template>
    <div class="section-class">
        <el-row>
        <el-card style="border-radius: 15px; background-color:#c3c6c7">
            <el-row>
                <el-col span="23">
                <div class="section-title">
                    Detection Historical Data
                </div>
                </el-col>
                <el-col span="1">
                    <el-button @click="handleClose"><el-icon name="close" /></el-button>
                </el-col>
          </el-row>
            <el-row>
                <div>
                    <div>
                        <el-input v-model="searchQuery" placeholder="Search..." clearable @clear="handleSearch" @input="handleSearch" />
                    </div>
                </div>

                <el-table v-loading="loading" :data="filteredTableData" border>
                    <el-table-column v-for="field in tableFields" :key="field.columnName" :prop="field.columnName" :label="formatLabel(field.columnName)" sortable />
                </el-table>
            </el-row>
        </el-card>
        </el-row>
    </div>
</template>

<script>

import { getTableMetadata, getTableData } from '@/api/mra'


export default {
    name: 'NearMissTable',
    props: {
        tableName: {
            type: String,
            required: true
        }
    },
    data() {
        return {
            loading: false,
            tableData: [],
            tableFields: [],
            searchQuery: '',
            currentPage: 1,
            pageSize: 10,
            total: 0,
            requiredFields: ["near_miss_status", "detection_status", "detection_dept", "detection_desc", "create_time"],
            index: []
        }
    },
    computed: {
        filteredTableData() {
            if (!this.searchQuery) return this.tableData;
            return this.tableData.filter(row => {
                return Object.values(row).some(value =>
                    String(value).toLowerCase().includes(this.searchQuery.toLowerCase())
                )
            })

        }
    },
    
    created() {
        this.initializeTable()
    },

    methods: {
        handleClose() {
            this.$store.commit('changeTableName', '')
            this.$store.commit('changeDetectionId', '')
            this.$store.commit('changeActiveNames', ["1"])
        },
        formatLabel(columnName) {
             
            const ret = columnName
            .split('_')
            .map(word => word.charAt(0).toUpperCase() + word.slice(1))
            .join(' ')
            if (ret === 'Near Miss Status') {
                return 'Near Miss Events Detected'
            }
            return ret
        },

        updateCreateTime(t) {
            return t.substring(0, 10) + " " + t.substring(11, 16)
        },

        filterRequiredFields() {
            const indices = this.tableFields.map((item, index) => this.requiredFields.includes(item.columnName) ? index : -1)
            .filter(index => index !== -1)
            this.index = indices
            const selectedFieldName = this.index.map(ind => this.tableFields[ind])
            this.tableFields = selectedFieldName
            const filter_id = this.tableData.filter(obj => obj['detection_id'] === parseInt(this.$store.getters.detection_id))
            this.tableData = filter_id
            const filter_time = this.tableData.map(obj => {
                return {
                    ...obj,
                    create_time: this.updateCreateTime(obj.create_time)
                }
            })
            this.tableData = filter_time
            const filteredData = this.tableData.filter(obj => this.requiredFields.every(key => key in obj));
            this.tableData = filteredData
        },

        async initializeTable() {
            try {                
                this.loading = true
                await this.fetchMetadata()
                await this.fetchData()
                // this.filterRequiredFields()
            } catch (error) {
                console.error('Error initializing table:', error)
                this.$message({
                    message: 'Failed to initialize table',
                    type: 'error',
                    duration: 5000
                })
            } finally {
                this.loading = false
            }
        },
        generateFormRules() {
            const rules = {}
            this.tableFields.forEach(field => {
                if (field.nullable === false && field.columnName !== 'auto_id') {
                    rules[field.columnName] = [{
                        required: true,
                        message: `${this.formatLabel(field.columnName)} is required`,
                        trigger: 'blur'
                    }]
                }
            })
            this.formRules = rules
        },
    async fetchMetadata() {
      try {
        const response = await getTableMetadata(this.tableName)
        if (response.code === 200) {
          this.tableFields = response.data
          this.generateFormRules()
          const indices = this.tableFields.map((item, index) => this.requiredFields.includes(item.columnName) ? index : -1)
            .filter(index => index !== -1)
            this.index = indices
            const selectedFieldName = this.index.map(ind => this.tableFields[ind])
            this.tableFields = selectedFieldName
        } else {
          this.$message.error(response.msg || 'Failed to fetch table metadata')
        }
      } catch (error) {
        console.error('Error fetching metadata:', error)
        this.$message({
          message: 'Failed to fetch table metadata',
          type: 'error',
          duration: 5000
        })
        throw error
      }
    },

    async fetchData() {
      try {
        const response = await getTableData(this.tableName, {
          page: this.currentPage,
          size: this.pageSize
        })

        if (response.code === 200) {
          this.tableData = response.data.content
          const filter_id = this.tableData.filter(obj => obj['detection_id'] === parseInt(this.$store.getters.detection_id))
            this.tableData = filter_id
            const filter_time = this.tableData.map(obj => {
                return {
                    ...obj,
                    create_time: this.updateCreateTime(obj.create_time_ets)
                }
            })
            this.tableData = filter_time
            const filteredData = this.tableData.filter(obj => this.requiredFields.every(key => key in obj));
            this.tableData = filteredData

          this.total = this.tableData.length
        } else {
          this.$message.error(response.msg || 'Failed to fetch table data')
        }
      } catch (error) {
        console.error('Error fetching data:', error)
        this.$message({
          message: 'Failed to fetch table data',
          type: 'error',
          duration: 5000
        })
        throw error
      }
    },
        handleSearch() {
            this.currentPage = 1;
            this.fetchData()
            // this.filterRequiredFields()
        },
    }
}
</script>

<style>
.section-class {
  padding: 10px;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: #0x52ba;
  border-radius: 10px;
  background-color:#c3c6c7;
  height: 50px;
}
</style>
