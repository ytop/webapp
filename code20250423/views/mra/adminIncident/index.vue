<template>
  <div id="adminincidenttab">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="Meta Data" name="TableManager">
        <table-manager></table-manager>
      </el-tab-pane>
      <el-tab-pane label="File Explorer" name="DownloadFile">
        <download-file></download-file>
      </el-tab-pane>
      <el-tab-pane label="Validate" name="ValidateTrigger">
        <validate-trigger></validate-trigger>
      </el-tab-pane>
      <el-tab-pane label="Audit Trail" name="AuditTrail">
        <div v-if="activeTab === 'AuditTrail'">
          <audit-trail></audit-trail>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import TableManager from './TableManager.vue'
import DownloadFile from './DownloadFile.vue'
import ValidateTrigger from './Validate.vue'
import AuditTrail from './AuditTrail.vue'

export default {
  name: 'AdminIncidentMain',
  components: {
    TableManager,
    DownloadFile,
    ValidateTrigger,
    AuditTrail
  },
  data() {
    return {
      activeTab: 'TableManager'
    }
  }
}
</script>

<style>
#adminincidenttab {
  padding: 20px;
}
</style>
