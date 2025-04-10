<template>
  <div class="task-container">
    <div class="task-header">
      <h2>KRI Collection Tasks</h2>
      <div class="task-filters">
        <el-input
          v-model="searchQuery"
          placeholder="Search tasks..."
          prefix-icon="el-icon-search"
          clearable
          style="width: 250px; margin-right: 15px;"
        />
        <el-select
          v-model="statusFilter"
          placeholder="Filter by status"
          clearable
          style="width: 200px;"
        >
          <el-option
            v-for="option in statusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>
    </div>

    <el-table
      v-loading="loading"
      :data="filteredTasks"
      border
      style="width: 100%"
      @row-click="handleRowClick"
    >
      <el-table-column
        prop="name"
        label="KRI Name"
        min-width="180"
      />
      <el-table-column
        prop="path"
        label="Path"
        min-width="250"
        show-overflow-tooltip
      />
      <el-table-column
        prop="collectionStatus"
        label="Status"
        width="150"
      >
        <template slot-scope="scope">
          <el-tag
            :type="getStatusType(scope.row.collectionStatus)"
            size="medium"
          >
            {{ scope.row.collectionStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="valueDate"
        label="Value Date"
        width="120"
      />
      <el-table-column
        prop="value"
        label="Value"
        width="100"
      />
      <el-table-column
        prop="breachStatus"
        label="Breach Status"
        width="130"
      >
        <template slot-scope="scope">
          <el-tag
            v-if="scope.row.breachStatus !== 'Not Determined'"
            :type="getBreachStatusType(scope.row.breachStatus)"
            size="medium"
          >
            {{ scope.row.breachStatus }}
          </el-tag>
          <span v-else>{{ scope.row.breachStatus }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="Tags"
        width="100"
        align="center"
      >
        <template slot-scope="scope">
          <el-tag
            v-if="scope.row.hasTags"
            size="small"
            type="info"
          >
            Has Tags
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="Actions"
        width="120"
        align="center"
      >
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.collectionStatus === 'Awaiting Collection'"
            type="primary"
            size="mini"
            @click.stop="openValueEntryDialog(scope.row)"
          >
            Enter Value
          </el-button>
          <el-button
            v-else
            type="info"
            size="mini"
            @click.stop="openValueEntryDialog(scope.row)"
          >
            View
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredTasks.length"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- KRI Value Entry Dialog -->
    <el-dialog
      :visible.sync="dialogVisible"
      :title="getDialogTitle()"
      width="60%"
      :before-close="() => dialogVisible = false"
    >
      <kri-value-entry
        v-if="selectedKri"
        :kri-task="selectedKri"
        @value-submitted="handleValueSubmitted"
        @cancel="dialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script>
import KriValueEntry from './KriValueEntry.vue';
import { getKriTasks } from '@/api/kri';

export default {
  name: 'TaskView',
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
    filteredTasks() {
      let tasks = [...this.kriTaskList];

      // Apply search filter
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        tasks = tasks.filter(task =>
          task.name.toLowerCase().includes(query) ||
          task.path.toLowerCase().includes(query)
        );
      }

      // Apply status filter
      if (this.statusFilter) {
        tasks = tasks.filter(task => task.collectionStatus === this.statusFilter);
      }

      // Sort by requiresAttention first, then by name
      tasks.sort((a, b) => {
        if (a.requiresAttention && !b.requiresAttention) return -1;
        if (!a.requiresAttention && b.requiresAttention) return 1;
        return a.name.localeCompare(b.name);
      });

      return tasks;
    },

    paginatedTasks() {
      const startIndex = (this.currentPage - 1) * this.pageSize;
      return this.filteredTasks.slice(startIndex, startIndex + this.pageSize);
    }
  },
  created() {
    this.fetchKriTasks();
  },
  methods: {
    async fetchKriTasks() {
      this.loading = true;
      try {
        console.log('Component: Fetching KRI tasks');
        const tasks = await getKriTasks();
        console.log('Component: Received KRI tasks:', tasks);

        // Ensure we always have an array, even if the API returns null or undefined
        this.kriTaskList = Array.isArray(tasks) ? tasks : [];

        if (!this.kriTaskList.length) {
          console.warn('No KRI tasks found or empty array returned');
          this.$message.warning('No KRI tasks found');
        } else {
          console.log(`Successfully loaded ${this.kriTaskList.length} KRI tasks`);
        }
      } catch (error) {
        console.error('Error fetching KRI tasks:', error);
        this.$message.error(`Failed to load KRI tasks: ${error.message || 'Unknown error'}`);
      } finally {
        this.loading = false;
      }
    },

    getStatusType(status) {
      const statusMap = {
        'Awaiting Collection': 'warning',
        'Awaiting Approval': 'info',
        'Collected': 'success'
      };
      return statusMap[status] || '';
    },

    getBreachStatusType(status) {
      const statusMap = {
        'Green': 'success',
        'Yellow': 'warning',
        'Red': 'danger'
      };
      return statusMap[status] || '';
    },

    handleRowClick(row) {
      this.openValueEntryDialog(row);
    },

    openValueEntryDialog(kri) {
      this.selectedKri = { ...kri };
      this.dialogVisible = true;
    },

    getDialogTitle() {
      if (!this.selectedKri) return 'KRI Value Entry';

      return `${this.selectedKri.name} - ${this.selectedKri.collectionStatus}`;
    },

    handleValueSubmitted(updatedTask) {
      // Update the task in the list
      const index = this.kriTaskList.findIndex(task => task.id === updatedTask.id);
      if (index !== -1) {
        this.kriTaskList[index] = { ...updatedTask };
      }

      this.dialogVisible = false;
      this.$message.success('KRI value submitted successfully');
    },

    handleSizeChange(size) {
      this.pageSize = size;
      this.currentPage = 1;
    },

    handleCurrentChange(page) {
      this.currentPage = page;
    }
  }
};
</script>

<style lang="scss" scoped>
.task-container {
  padding: 20px;

  .task-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
    }

    .task-filters {
      display: flex;
      align-items: center;
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}

@media screen and (max-width: 768px) {
  .task-container {
    .task-header {
      flex-direction: column;
      align-items: flex-start;

      h2 {
        margin-bottom: 15px;
      }

      .task-filters {
        width: 100%;
        flex-direction: column;

        .el-input,
        .el-select {
          width: 100% !important;
          margin-right: 0 !important;
          margin-bottom: 10px;
        }
      }
    }

    .pagination-container {
      justify-content: center;
    }
  }
}
</style>
