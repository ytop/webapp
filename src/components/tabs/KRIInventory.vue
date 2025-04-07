<template>
  <div class="kri-inventory-container">
    <div class="table-operations">
      <el-row :gutter="20">
        <el-col
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-input
            v-model="searchQuery"
            placeholder="Search KRIs..."
            prefix-icon="el-icon-search"
            clearable
            style="width: 100%"
            @input="debouncedSearch"
            @clear="handleSearchClear"
          />
        </el-col>
        <el-col
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-select
            v-model="filterRiskType"
            placeholder="Filter by Risk Type"
            clearable
            style="width: 100%"
            @change="handleFilterChange"
          >
            <el-option
              v-for="type in riskTypes"
              :key="type"
              :label="type"
              :value="type"
            />
          </el-select>
        </el-col>
        <el-col
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-select
            v-model="filterRasStatus"
            placeholder="Filter by RAS Status"
            clearable
            style="width: 100%"
            @change="handleFilterChange"
          >
            <el-option
              label="RAS"
              value="RAS"
            />
            <el-option
              label="Non-RAS"
              value="Non-RAS"
            />
          </el-select>
        </el-col>
        <el-col
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          class="text-right"
        >
          <el-button
            type="primary"
            icon="el-icon-refresh"
            @click="fetchKRIs"
          >
            Refresh
          </el-button>
        </el-col>
      </el-row>
    </div>

    <el-table
      v-loading="loading"
      :data="paginatedData"
      border
      style="width: 100%"
      :empty-text="loading ? 'Loading...' : 'No data found'"
      row-key="kriId"
      stripe
      highlight-current-row
      @sort-change="handleSortChange"
      @row-click="handleRowClick"
    >
      <el-table-column
        prop="kriId"
        label="KRI ID"
        width="100"
        sortable="custom"
        fixed
      />
      <el-table-column
        prop="name"
        label="KRI Name"
        min-width="200"
        sortable="custom"
        show-overflow-tooltip
      />
      <el-table-column
        prop="dataProvider"
        label="Data Provider"
        min-width="150"
        sortable="custom"
        show-overflow-tooltip
      />
      <el-table-column
        prop="owner"
        label="Owner"
        min-width="120"
        sortable="custom"
      />
      <el-table-column
        prop="l1RiskType"
        label="L1 Risk Type"
        min-width="140"
        sortable="custom"
      />
      <el-table-column
        prop="l2RiskType"
        label="L2 Risk Type"
        min-width="140"
        sortable="custom"
      />
      <el-table-column
        prop="reportCycle"
        label="Report Cycle"
        min-width="120"
        sortable="custom"
      />
      <el-table-column
        prop="rasStatus"
        label="RAS Status"
        min-width="120"
        sortable="custom"
      >
        <template slot-scope="scope">
          <el-tag
            :type="scope.row.rasStatus === 'RAS' ? 'success' : 'info'"
            size="small"
          >
            {{ scope.row.rasStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="workflowStatus"
        label="Workflow Status"
        min-width="140"
        sortable="custom"
      >
        <template slot-scope="scope">
          <el-tag
            :type="getWorkflowStatusType(scope.row.workflowStatus)"
            size="small"
          >
            {{ scope.row.workflowStatus }}
          </el-tag>
        </template>
      </el-table-column>
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
            @click.stop="handleEdit(scope.row)"
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

    <!-- KRI Edit Dialog -->
    <el-dialog
      :visible.sync="dialogVisible"
      :title="`Edit KRI: ${selectedKRI.name || ''}`"
      width="80%"
      top="5vh"
      :fullscreen="false"
      :before-close="() => dialogVisible = false"
      append-to-body
      :modal-append-to-body="true"
      :close-on-click-modal="false"
      :z-index="3000"
    >
      <kri-edit-dialog
        :kri-data="selectedKRI"
        @kri-updated="handleKRIUpdated"
      />
    </el-dialog>
  </div>
</template>

<script>
import { getKRIs } from '@/api/kri'
import debounce from 'lodash/debounce'
import KRIEditDialog from '@/components/KRIEditDialog.vue'

export default {
  name: 'KRIInventoryComponent',

  components: {
    'kri-edit-dialog': KRIEditDialog
  },

  data() {
    return {
      loading: false,
      dialogVisible: false,
      kriList: [],
      selectedKRI: {},
      searchQuery: '',
      filterRiskType: '',
      filterRasStatus: '',
      currentPage: 1,
      pageSize: 50, // Default to 50 rows per page
      sortBy: 'kriId',
      sortOrder: 'ascending',
      riskTypes: ['Credit Risk', 'Market Risk', 'Operational Risk', 'Liquidity Risk', 'Compliance Risk', 'Technology Risk', 'Strategic Risk']
    }
  },

  computed: {
    filteredKRIList() {
      let filtered = this.kriList;

      // Apply search filter
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        filtered = filtered.filter(kri =>
          kri.kriId.toLowerCase().includes(query) ||
          kri.name.toLowerCase().includes(query) ||
          kri.kriDesc.toLowerCase().includes(query) ||
          kri.owner.toLowerCase().includes(query) ||
          kri.dataProvider.toLowerCase().includes(query)
        );
      }

      // Apply risk type filter
      if (this.filterRiskType) {
        filtered = filtered.filter(kri => kri.l1RiskType === this.filterRiskType);
      }

      // Apply RAS status filter
      if (this.filterRasStatus) {
        filtered = filtered.filter(kri => kri.rasStatus === this.filterRasStatus);
      }

      return this.sortData(filtered);
    },

    sortedData() {
      return this.sortData(this.kriList);
    },

    paginatedData() {
      const startIndex = (this.currentPage - 1) * this.pageSize;
      const endIndex = startIndex + this.pageSize;
      return this.filteredKRIList.slice(startIndex, endIndex);
    }
  },

  created() {
    this.fetchKRIs();
    // Debounce search for better performance
    this.debouncedSearch = debounce(() => {
      // Reset to first page when searching
      this.currentPage = 1;
    }, 300);
  },

  beforeDestroy() {
    // Clean up debounced function
    this.debouncedSearch.cancel();
  },

  methods: {
    async fetchKRIs() {
      this.loading = true;
      try {
        const response = await getKRIs();
        this.kriList = response;
      } catch (error) {
        this.$message.error('Failed to fetch KRI data');
        console.error('Error fetching KRIs:', error);
      } finally {
        this.loading = false;
      }
    },

    sortData(data) {
      if (!this.sortBy) return data;

      return [...data].sort((a, b) => {
        const aValue = a[this.sortBy];
        const bValue = b[this.sortBy];

        if (this.sortOrder === 'ascending') {
          return typeof aValue === 'string' && typeof bValue === 'string'
            ? aValue.localeCompare(bValue)
            : aValue > bValue ? 1 : aValue < bValue ? -1 : 0;
        } else {
          return typeof aValue === 'string' && typeof bValue === 'string'
            ? bValue.localeCompare(aValue)
            : bValue > aValue ? 1 : bValue < aValue ? -1 : 0;
        }
      });
    },

    handleRowClick(row) {
      console.log('Row clicked:', row);
      // Make a deep copy of the row data
      this.selectedKRI = JSON.parse(JSON.stringify(row));
      // Open the dialog
      this.dialogVisible = true;
    },

    handleEdit(row) {
      console.log('Edit button clicked for row:', row);
      // Make a deep copy of the row data
      this.selectedKRI = JSON.parse(JSON.stringify(row));
      // Open the dialog
      this.dialogVisible = true;
    },

    handleKRIUpdated(updatedKRI) {
      // Find and update the KRI in the list
      const index = this.kriList.findIndex(kri => kri.kriId === updatedKRI.kriId);
      if (index !== -1) {
        this.kriList.splice(index, 1, updatedKRI);
      }
    },

    handleSearchClear() {
      this.searchQuery = '';
      this.currentPage = 1;
    },

    handleFilterChange() {
      this.currentPage = 1;
    },

    handleSortChange({ prop, order }) {
      if (prop) {
        this.sortBy = prop;
        this.sortOrder = order || 'ascending';
      } else {
        this.sortBy = 'kriId';
        this.sortOrder = 'ascending';
      }
    },

    handleSizeChange(size) {
      this.pageSize = size;
      this.currentPage = 1;
    },

    handleCurrentChange(page) {
      this.currentPage = page;
    },

    getWorkflowStatusType(status) {
      const typeMap = {
        'Draft': 'info',
        'Pending Review': 'warning',
        'Under Review': 'warning',
        'Approved': 'success',
        'Rejected': 'danger'
      };
      return typeMap[status] || 'info';
    }
  }
}
</script>

<style lang="scss" scoped>
.kri-inventory-container {
  padding: 20px;

  .table-operations {
    margin-bottom: 20px;
  }

  .el-table {
    margin-bottom: 20px;

    // Make table rows clickable
    ::v-deep tbody tr {
      cursor: pointer;
    }
  }

  .pagination {
    margin-top: 20px;
    text-align: right;
  }

  .text-right {
    text-align: right;
  }
}

@media screen and (max-width: 768px) {
  .kri-inventory-container {
    padding: 10px;

    .table-operations {
      .el-col {
        margin-bottom: 10px;
      }
    }

    .pagination {
      text-align: center;
    }

    .text-right {
      text-align: left;
    }
  }
}
</style>
