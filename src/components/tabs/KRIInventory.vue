<template>
  <div class="kri-inventory-container">
    <div class="table-operations">
      <el-input
        v-model="searchQuery"
        placeholder="Search KRIs..."
        prefix-icon="el-icon-search"
        clearable
        style="width: 250px"
        @input="debouncedSearch"
        @clear="handleSearchClear"
      />
    </div>
    <el-table
      v-loading="loading"
      :data="paginatedData"
      border
      style="width: 100%"
      :empty-text="loading ? 'Loading...' : 'No data found'"
      @sort-change="handleSortChange"
    >
      <el-table-column
        prop="kriId"
        label="KRI ID"
        width="180"
        sortable="custom"
      />
      <el-table-column
        prop="kriDesc"
        label="KRI Description"
        show-overflow-tooltip
        sortable="custom"
      />
      <el-table-column
        fixed="right"
        label="Actions"
        width="120"
        align="center"
      >
        <template slot-scope="scope">
          <el-button
            type="text"
            size="small"
            @click="handleEdit(scope.row)"
          >
            Edit
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <el-pagination
      v-if="filteredKRIList.length > 0"
      :current-page="currentPage"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pageSize"
      :total="filteredKRIList.length"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <!-- Edit Dialog -->
    <el-dialog
      title="Update KRI Description"
      :visible.sync="dialogVisible"
      width="50%"
    >
      <el-form
        ref="updateForm"
        :model="updateForm"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item
          label="KRI ID"
          prop="kriId"
        >
          <el-input
            v-model="updateForm.kriId"
            disabled
          />
        </el-form-item>
        <el-form-item
          label="KRI Description"
          prop="kriDesc"
        >
          <el-input
            v-model="updateForm.kriDesc"
            type="textarea"
            :rows="4"
            :disabled="!updateForm.kriId"
            :maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <span
        slot="footer"
        class="dialog-footer"
      >
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button
          type="primary"
          :loading="updating"
          :disabled="!updateForm.kriId"
          @click="handleUpdateDescription"
        >
          Update
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getKRIs, updateKRIDescription } from '@/api/kri'
import debounce from 'lodash/debounce'

export default {
  name: 'KRIInventoryComponent',

  data() {
    return {
      loading: false,
      updating: false,
      dialogVisible: false,
      kriList: [],
      searchQuery: '',
      currentPage: 1,
      pageSize: 10,
      sortBy: 'kriId',
      sortOrder: 'ascending',
      updateForm: {
        kriId: null,
        kriDesc: ''
      },
      formRules: {
        kriDesc: [
          { required: true, message: 'Please enter KRI description', trigger: 'blur' },
          { min: 3, max: 500, message: 'Length should be 3 to 500 characters', trigger: 'blur' }
        ]
      }
    }
  },

  computed: {
    filteredKRIList() {
      if (!this.searchQuery) return this.sortedData

      const query = this.searchQuery.toLowerCase()
      return this.sortedData.filter(kri =>
        kri.kriId.toLowerCase().includes(query) ||
        kri.kriDesc.toLowerCase().includes(query)
      )
    },

    sortedData() {
      if (!this.sortBy) return this.kriList

      return [...this.kriList].sort((a, b) => {
        const aValue = a[this.sortBy]
        const bValue = b[this.sortBy]

        if (this.sortOrder === 'ascending') {
          return typeof aValue === 'string' && typeof bValue === 'string'
            ? aValue.localeCompare(bValue)
            : aValue > bValue ? 1 : aValue < bValue ? -1 : 0
        } else {
          return typeof aValue === 'string' && typeof bValue === 'string'
            ? bValue.localeCompare(aValue)
            : bValue > aValue ? 1 : bValue < aValue ? -1 : 0
        }
      })
    },

    paginatedData() {
      const startIndex = (this.currentPage - 1) * this.pageSize
      const endIndex = startIndex + this.pageSize
      return this.filteredKRIList.slice(startIndex, endIndex)
    }
  },

  created() {
    this.fetchKRIs()
    // Debounce search for better performance
    this.debouncedSearch = debounce(() => {
      // Reset to first page when searching
      this.currentPage = 1
    }, 300)
  },

  beforeDestroy() {
    // Clean up debounced function
    this.debouncedSearch.cancel()
  },

  methods: {
    async fetchKRIs() {
      this.loading = true
      try {
        const response = await getKRIs()
        this.kriList = response
      } catch (error) {
        this.$message.error('Failed to fetch KRI data')
        console.error('Error fetching KRIs:', error)
      } finally {
        this.loading = false
      }
    },

    handleEdit(row) {
      this.updateForm.kriId = row.kriId
      this.updateForm.kriDesc = row.kriDesc
      this.dialogVisible = true
    },

    async handleUpdateDescription() {
      try {
        await this.$refs.updateForm.validate()
      } catch (error) {
        return
      }

      this.updating = true
      try {
        await updateKRIDescription(this.updateForm.kriId, this.updateForm.kriDesc)
        this.$message.success('KRI Description updated successfully')
        await this.fetchKRIs()
        this.dialogVisible = false
        this.resetForm()
      } catch (error) {
        this.$message.error('Failed to update KRI Description')
        console.error('Error updating KRI description:', error)
      } finally {
        this.updating = false
      }
    },

    resetForm() {
      if (this.$refs.updateForm) {
        this.$refs.updateForm.resetFields()
      }
      this.updateForm.kriId = null
      this.updateForm.kriDesc = ''
    },

    handleSearchClear() {
      this.searchQuery = ''
      this.currentPage = 1
    },

    handleSortChange({ prop, order }) {
      if (prop) {
        this.sortBy = prop
        this.sortOrder = order || 'ascending'
      } else {
        this.sortBy = 'kriId'
        this.sortOrder = 'ascending'
      }
    },

    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },

    handleCurrentChange(page) {
      this.currentPage = page
    }
  }
}
</script>

<style lang="scss" scoped>
.kri-inventory-container {
  padding: 20px;

  .table-operations {
    margin-bottom: 16px;
    display: flex;
    justify-content: flex-end;
  }

  .el-table {
    margin-bottom: 20px;
  }

  .el-form {
    max-width: 600px;
    margin: 0 auto;
  }

  .pagination {
    margin-top: 20px;
    text-align: right;
  }
}

@media screen and (max-width: 768px) {
  .kriinventory {
    padding: 10px;

    .table-operations {
      flex-direction: column;
      align-items: stretch;
    }

    .pagination {
      text-align: center;
    }
  }
}
</style>
