<template>
  <div class="table-manager">

    <el-card>

      <div slot="header" class="flex items-center">

        <span class="font-medium text-lg">Configuration </span>

        <el-select v-model="selectedTable" placeholder="Select table" class="ml-5 w-64" @change="handleTableChange">

          <el-option v-for="table in availableTables" :key="table.value" :label="table.label" :value="table.value" />

        </el-select>

      </div>

      <dynamic-table-crud v-if="selectedTable" :key="selectedTable" :table-name="selectedTable" />

    </el-card>

  </div>
</template>

<script>
import DynamicTableCrud from './DynamicTableCrud.vue'
import { getAllTables } from '@/api/mra'

export default {
  name: 'TableManager',

  components: {
    DynamicTableCrud
  },

  data() {
    return {
      tables: [],
      selectedTable: ''
    }
  },

  computed: {
    availableTables() {
      return this.tables
        .map(table => ({
          value: table,
          // Convert technical table name to more readable format
          label: table.replace('web.IMP_', '')
            .split('_')
            .map(word => word.charAt(0) + word.slice(1).toLowerCase())
            .join(' ')
        }))
    }
  },

  async created() {
    await this.fetchTables()
  },

  methods: {
    async fetchTables() {
      try {
        const response = await getAllTables()
        if (response.code === 200) { // Assuming your API returns a code property
          this.tables = response.data
        } else {
          this.$message.error(response.msg || 'Failed to fetch tables')
        }
      } catch (error) {
        console.error('Error fetching tables:', error)
        this.$message({
          message: 'Failed to fetch tables ',
          type: 'error',
          duration: 5000
        })
      }
    },

    handleTableChange(value) {
      this.selectedTable = value
    }
  }
}
</script>

<style scoped>
.table-manager {
    @apply p-5;
}

/* Element UI overrides */

:deep(.el-card__header) {
    @apply p-4 border-b border-gray-200;
}

:deep(.el-card__body) {
    @apply p-4;
}
</style>
