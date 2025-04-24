<template>
  <div class="dynamic-table-crud">

    <!-- Table Operations -->
    <!-- Data Table -->

    <el-table v-loading="loading" :data="filteredTableData" border class="w-full mt-5">

      <el-table-column v-for="field in tableFields" :key="field.columnName" :prop="field.columnName" :label="formatLabel(field.columnName)" sortable />

      <el-table-column label="Operations" width="200" fixed="right">

        <template slot-scope="scope">

          <el-button type="text" @click="handleEdit(scope.row)">Edit</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Dynamic Form Dialog -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="50%"
      @close="resetForm"
    >
      <el-form
        ref="form"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item
          v-for="field in formFields"
          :key="field.columnName"
          :label="formatLabel(field.columnName)"
          :prop="field.columnName"
        >
          <!-- Input type based on data type -->
          <template v-if="field.dataType === 'datetime'">
            <el-date-picker v-model="formData[field.columnName]" type="datetime" placeholder="Select date and time" class="w-full" :disabled="true" />
          </template>

          <template v-else-if="field.dataType === 'number'">
            <el-input-number v-model="formData[field.columnName]" :controls="false" class="w-full" :disabled="true" />
          </template>

          <template v-else-if="field.dataType === 'boolean'">
            <el-switch v-model="formData[field.columnName]" active-text="Yes" inactive-text="No" :disabled="true" />
          </template>

          <template v-else>
            <el-input
              v-model="formData[field.columnName]"
              :type="field.dataType === 'text' ? 'textarea' : 'text'"
              :autosize="field.dataType === 'text' ? { minRows: 2, maxRows: 4 } : false"
              :disabled="!(field.dataType === 'text' && ['cell_params', 'string_value'].includes(field.columnName))"
            />
          </template>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="submitForm">Confirm</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getTableMetadata,
  getTableData,
  createTableRecord,
  updateTableRecord,
  deleteTableRecord
} from '@/api/mra'

export default {
  name: 'DynamicTableCrud',

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
      dialogVisible: false,
      dialogTitle: '',
      formData: {},
      currentPage: 1,
      pageSize: 10,
      total: 0,
      formRules: {},
      isEdit: false
    }
  },

  computed: {
    filteredTableData() {
      if (!this.searchQuery) return this.tableData

      return this.tableData.filter(row => {
        return Object.values(row).some(value =>
          String(value).toLowerCase().includes(this.searchQuery.toLowerCase())
        )
      })
    },

    formFields() {
      return this.tableFields.filter(field => field.columnName !== 'auto_id')
    }
  },

  created() {
    this.initializeTable()
  },

  methods: {
    async initializeTable() {
      try {
        this.loading = true
        await this.fetchMetadata()
        await this.fetchData()
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

    async fetchMetadata() {
      try {
        const response = await getTableMetadata(this.tableName)
        if (response.code === 200) {
          this.tableFields = response.data
          this.generateFormRules()
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
          this.total = response.data.total
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

    async handleDelete(row) {
      try {
        await this.$confirm('Are you sure you want to delete this record?', 'Warning', {
          type: 'warning'
        })

        const response = await deleteTableRecord(this.tableName, row.auto_id)
        if (response.code === 200) {
          this.$message({
            message: 'Record deleted successfully',
            type: 'success',
            duration: 3000
          })
          await this.fetchData()
        } else {
          this.$message.error(response.msg || 'Failed to delete record')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Error deleting record:', error)
          this.$message({
            message: 'Failed to delete record',
            type: 'error',
            duration: 5000
          })
        }
      }
    },

    async submitForm() {
      try {
        await this.$refs.form.validate()

        let response
        if (this.isEdit) {
          response = await updateTableRecord(
            this.tableName,
            this.formData.auto_id,
            this.formData
          )
        } else {
          response = await createTableRecord(this.tableName, this.formData)
        }

        if (response.code === 200) {
          this.$message({
            message: `Record ${this.isEdit ? 'updated' : 'created'} successfully`,
            type: 'success',
            duration: 3000
          })
          this.dialogVisible = false
          await this.fetchData()
        } else {
          this.$message.error(response.msg || `Failed to ${this.isEdit ? 'update' : 'create'} record`)
        }
      } catch (error) {
        if (error !== false) {
          console.error('Error submitting form:', error)
          this.$message({
            message: `Failed to ${this.isEdit ? 'update' : 'create'} record`,
            type: 'error',
            duration: 5000
          })
        }
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

    formatLabel(columnName) {
      return columnName
        .split('_')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ')
    },

    handleEdit(row) {
      this.isEdit = true
      this.dialogTitle = 'Edit Record'
      this.formData = { ...row }
      this.dialogVisible = true
    },
    resetForm() {
      if (this.$refs.form) {
        this.$refs.form.resetFields()
      }
      this.formData = {}
    },

    handleSizeChange(val) {
      this.pageSize = val
      this.fetchData()
    },

    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchData()
    }
  }

}
</script>

<style scoped>
.dynamic-table-crud {
    @apply p-5;
}

.operation-bar {
    @apply flex items-center mb-5;
}
</style>
