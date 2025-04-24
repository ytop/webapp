<template>
  <div class="audit-trail">

    <el-card>

      <div slot="header" class="flex items-center">

        <span class="font-medium text-lg">For Audit Only  </span>

      </div>

      <audit-trail-data v-if="selectedTable" :key="selectedTable" :table-name="selectedTable" />

    </el-card>

  </div>
</template>

<script>
import AuditTrailData from './AuditTrailData.vue'

export default {
  name: 'AuditTrail',

  components: {
    AuditTrailData
  },

  data() {
    return {
      selectedTable: ''
    }
  },

  async created() {
    this.fetchTables()
  },

  methods: {
    fetchTables() {
        this.selectedTable = 'IMP_DETECTION_AUDIT_TRAIL'
    }
  }
}
</script>

<style scoped>
.audit-trail {
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
