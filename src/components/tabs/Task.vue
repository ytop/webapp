
<template>
  <div>
    <el-row
      :gutter="20"
      style="margin-bottom: 20px;"
    >
      <el-col :span="6">
        <el-input
          v-model="searchQuery"
          placeholder="Search by name or status"
          prefix-icon="el-icon-search"
          size="small"
          clearable
          @clear="handleSearchClear"
        />
      </el-col>
      <el-col :span="12">
        <el-select
          v-model="statusFilter"
          placeholder="Filter by status"
          clearable
          size="small"
          style="width: 180px;"
        >
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-switch
          v-model="revealEditable"
          active-text="Reveal editable fields"
          size="small"
          style="margin-left: 20px;"
        />
      </el-col>
      <el-col
        :span="6"
        style="text-align: right;"
      >
        <el-pagination
          :current-page.sync="currentPage"
          :page-size="pageSize"
          :total="totalItems"
          layout="prev, pager, next"
          small
          @current-change="handlePageChange"
        />
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="18">
        <el-card shadow="never">
          <div
            v-if="loading"
            class="loading-container"
          >
            <el-skeleton
              :rows="5"
              animated
            />
          </div>
          <div v-else>
            <el-table
              v-loading="tableLoading"
              element-loading-text="Loading data..."
              :data="paginatedData"
              stripe
              style="width: 100%"
              size="mini"
              @row-click="handleRowClick"
            >
              <el-table-column
                prop="name"
                label="Name"
                min-width="300"
                sortable
              >
                <template slot-scope="scope">
                  <div>{{ scope.row.name }}</div>
                  <div style="font-size: 11px; color: grey;">
                    {{ scope.row.path }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                prop="collectionStatus"
                label="Collection Status"
                min-width="180"
                sortable
              >
                <template slot-scope="scope">
                  <el-tag
                    :type="getStatusType(scope.row.collectionStatus)"
                    size="mini"
                    effect="light"
                  >
                    {{ scope.row.collectionStatus }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column
                prop="valueDate"
                label="Value Date"
                min-width="150"
                align="center"
                sortable
              />
              <el-table-column
                prop="value"
                label="Value"
                min-width="120"
                align="center"
                sortable
              />
              <el-table-column
                prop="breachStatus"
                label="Breach Status"
                min-width="150"
                align="center"
                sortable
              >
                <template slot-scope="scope">
                  <el-tag
                    v-if="scope.row.breachStatus"
                    :type="getBreachType(scope.row.breachStatus)"
                    size="mini"
                  >
                    {{ scope.row.breachStatus }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column
                prop="tags"
                label="Tags"
                min-width="120"
                align="center"
              >
                <template slot-scope="scope">
                  <i
                    v-if="scope.row.hasTags"
                    class="el-icon-price-tag"
                  />
                </template>
              </el-table-column>
              <el-table-column
                fixed="right"
                label="Actions"
                min-width="120"
              >
                <template slot-scope="scope">
                  <el-button
                    v-if="scope.row.collectionStatus === 'Awaiting Collection'"
                    type="text"
                    size="small"
                    @click.stop="handleEdit(scope.row)"
                  >
                    Edit
                  </el-button>
                  <el-button
                    type="text"
                    size="small"
                    @click.stop="handleView(scope.row)"
                  >
                    View
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div style="margin-top: 20px; text-align: right;">
              <el-pagination
                :current-page.sync="currentPage"
                :page-sizes="[5, 10, 20, 50]"
                :page-size="pageSize"
                :total="totalItems"
                layout="total, sizes, prev, pager, next"
                @size-change="handleSizeChange"
                @current-change="handlePageChange"
              />
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card
          shadow="never"
          class="info-panel"
        >
          <div class="info-panel__header">
            <span class="required-note">* Modified Required *</span>
            <el-button
              size="mini"
              type="text"
            >
              New
            </el-button>
          </div>
          
          <div class="info-panel__note">
            Note: KRI Last Value Information section is automatically set when new KRI values are submitted
          </div>

          <div class="info-panel__attention">
            1 item requires attention.
          </div>

          <el-form
            label-position="left"
            size="mini"
            class="info-panel__form"
          >
            <el-form-item label="All Key Items (9)">
              <el-collapse>
                <el-collapse-item>
                  <el-form-item label="Frequency *" />
                  <el-form-item label="Description *" />
                  <el-form-item label="Owner *" />
                  <el-form-item label="KRI Capture *" />
                  <el-form-item label="Status *" />
                  <el-form-item label="KRI Value Approval Required *" />
                  <el-form-item label="Collection Start Date *" />
                  <el-form-item label="Yellow Threshold *" />
                  <el-form-item label="Red Threshold *" />
                </el-collapse-item>
              </el-collapse>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <kri-value-entry
      :visible.sync="dialogVisible"
      :kri-data="selectedKri"
      @value-submitted="handleValueSubmitted"
    />
  </div>
</template>

<script>
import KriValueEntry from './KriValueEntry.vue';
import { getKriTasks } from '@/api/kriTask';

export default {
  name: 'TaskComponent',
  components: {
    KriValueEntry
  },
  data() {
    return {
      revealEditable: false,
      dialogVisible: false,
      selectedKri: null,
      kriTaskList: [],
      loading: true,
      tableLoading: false,
      searchQuery: '',
      statusFilter: '',
      currentPage: 1,
      pageSize: 10,
      statusOptions: [
        { value: 'Awaiting Collection', label: 'Awaiting Collection' },
        { value: 'Awaiting Approval', label: 'Awaiting Approval' },
        { value: 'Collected', label: 'Collected' }
      ]
    };
  },
  computed: {
    /**
     * Filter the task list based on search query and status filter
     * @returns {Array} Filtered task list
     */
    filteredData() {
      let result = this.kriTaskList;

      // Apply search filter
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        result = result.filter(item => {
          return item.name.toLowerCase().includes(query) ||
                 (item.collectionStatus && item.collectionStatus.toLowerCase().includes(query)) ||
                 (item.path && item.path.toLowerCase().includes(query));
        });
      }

      // Apply status filter
      if (this.statusFilter) {
        result = result.filter(item => item.collectionStatus === this.statusFilter);
      }

      return result;
    },

    /**
     * Get the total number of items after filtering
     * @returns {Number} Total number of items
     */
    totalItems() {
      return this.filteredData.length;
    },

    /**
     * Get the paginated data based on current page and page size
     * @returns {Array} Paginated data
     */
    paginatedData() {
      const startIndex = (this.currentPage - 1) * this.pageSize;
      const endIndex = startIndex + this.pageSize;
      return this.filteredData.slice(startIndex, endIndex);
    }
  },
  created() {
    this.fetchKriTasks();
  },
  methods: {
    /**
     * Fetch KRI tasks from the API
     */
    async fetchKriTasks() {
      this.loading = true;
      try {
        const data = await getKriTasks();
        this.kriTaskList = data;
      } catch (error) {
        console.error('Error fetching KRI tasks:', error);
        this.$message.error('Failed to load KRI tasks. Please try again later.');
      } finally {
        this.loading = false;
      }
    },

    /**
     * Handle row click event
     * @param {Object} row - The clicked row data
     */
    handleRowClick(row) {
      this.handleView(row);
    },

    /**
     * Handle edit button click
     * @param {Object} row - The row data to edit
     */
    handleEdit(row) {
      if (row.collectionStatus === 'Awaiting Collection') {
        this.selectedKri = row;
        this.dialogVisible = true;
      } else {
        this.$message.info(`Only tasks with 'Awaiting Collection' status can be edited.`);
      }
    },

    /**
     * Handle view button click
     * @param {Object} row - The row data to view
     */
    handleView(row) {
      if (row.collectionStatus === 'Awaiting Collection') {
        this.handleEdit(row);
      } else {
        console.log("Viewing details for: ", row.name);
        this.$message.info(`Viewing details for ${row.name} (Read-Only).`);
        // Here you could open a read-only modal if needed
      }
    },

    /**
     * Handle value submitted event from KriValueEntry component
     * @param {Object} updatedData - The updated KRI data
     */
    handleValueSubmitted(updatedData) {
      // Find and update the task in the list
      const index = this.kriTaskList.findIndex(item => item.id === updatedData.id);
      if (index !== -1) {
        this.kriTaskList[index] = { ...this.kriTaskList[index], ...updatedData };
      }
      this.$message.success(`Value for ${updatedData.name} submitted successfully.`);
    },

    /**
     * Handle page size change event
     * @param {Number} size - The new page size
     */
    handleSizeChange(size) {
      this.pageSize = size;
      this.currentPage = 1; // Reset to first page when changing page size
    },

    /**
     * Handle page change event
     * @param {Number} page - The new page number
     */
    handlePageChange(page) {
      this.currentPage = page;
    },

    /**
     * Handle search clear event
     */
    handleSearchClear() {
      this.searchQuery = '';
    },

    /**
     * Get the tag type based on collection status
     * @param {String} status - The collection status
     * @returns {String} The tag type
     */
    getStatusType(status) {
      switch(status) {
        case 'Awaiting Collection': return 'warning';
        case 'Awaiting Approval': return 'info';
        case 'Collected': return 'success';
        default: return 'info';
      }
    },

    /**
     * Get the tag type based on breach status
     * @param {String} status - The breach status
     * @returns {String} The tag type
     */
    getBreachType(status) {
      switch(status) {
        case 'Red': return 'danger';
        case 'Yellow': return 'warning';
        case 'Not Determined': return 'info';
        default: return ''; // No tag if status is null/empty
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.info-panel {
  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
  }

  &__note {
    font-size: 12px;
    color: #606266;
    margin-bottom: 15px;
    padding: 8px;
    background-color: #f5f7fa;
    border-radius: 4px;
  }

  &__attention {
    color: #E6A23C;
    font-size: 12px;
    margin-bottom: 15px;
  }

  &__form {
    .el-form-item {
      margin-bottom: 8px;
    }

    .el-form-item__label {
      font-size: 12px;
    }
  }
}

.required-note {
  color: #F56C6C;
  font-size: 12px;
}
</style>
