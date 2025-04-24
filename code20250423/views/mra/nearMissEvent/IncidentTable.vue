<template>
  <div class="incident-table">
    <el-table
      :data="tableData"
      style="width: 100%; border-radius:10px;"
      height="610"
      :border="true"
      stripe
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="index" width="50" />
      <!-- Core Incident Information -->
      <el-table-column prop="incidentNo" label="Incident Number" width="150" sortable />
      <el-table-column prop="incidentType" label="Incident Type" width="130" />
      <el-table-column prop="incidentTitle" label="Title" width="200" show-overflow-tooltip />
      <el-table-column prop="incidentDescription" label="Description" width="300" show-overflow-tooltip />
      <!-- Status and Risk -->
      <el-table-column prop="incidentStatus" label="Status" width="120">
        <template slot-scope="scope">
          <el-tag :type="getIncidentStatusType(scope.row.incidentStatus)">{{ scope.row.incidentStatus }}</el-tag>
        </template>
      </el-table-column>


      <!-- Risk Types -->
      <el-table-column prop="ownerDepartment" label="Owner Department" width="150" show-overflow-tooltip />
      <el-table-column prop="identificationDate" label="Identification Date" width="200" sortable />
      <el-table-column prop="primaryRiskType" label="Primary Impacted Risk Area" width="210" show-overflow-tooltip />
      <!-- <el-table-column prop="primaryLevelIIRisk" label="Primary Level II Risk" width="150" show-overflow-tooltip /> -->
      <!-- <el-table-column prop="secondaryRiskType" label="Secondary Risk Type" width="200" show-overflow-tooltip /> -->
      <!-- <el-table-column prop="secondaryLevelIIRisk" label="Secondary Level II Risk" width="150" show-overflow-tooltip /> -->

      <!-- Dates -->
      <el-table-column prop="occurrenceDate" label="Occurrence Date" width="150" sortable />
      
      <!-- <el-table-column prop="dueDate" label="Due Date" width="150" sortable /> -->
      <!-- <el-table-column prop="recoveryDate" label="Recovery Date" width="150" sortable /> -->

      <!-- Ownership -->
      
      <!-- <el-table-column prop="primaryOwnerDept" label="Primary Owner Dept" width="150" show-overflow-tooltip /> -->
      <!-- <el-table-column prop="secondaryOwnerDept" label="Secondary Owner Dept" width="200" show-overflow-tooltip /> -->
      <el-table-column prop="identifiedBy" label="Identified By" width="120" />

      

    </el-table>
  </div>
</template>

<script>
export default {
  name: 'IncidentTable',
  props: {
    tableData: {
      type: Array,
      required: true
    }
  },
  methods: {
    handleSelectionChange(val) {
      this.multipleSelection = val
    },

    getRiskRatingType(rating) {
      const types = {
        HIGH: 'danger',
        MEDIUM: 'warning',
        LOW: 'success'
      }
      return types[rating] || 'info'
    },
    getIncidentStatusType(incidentStatus) {
      const types = {
        Open: 'warning',
        'Pending Closure Approval': 'primary',
        Closed: 'success'
      }
      return types[incidentStatus] || 'info'
    }
  }
}
</script>

<style scoped>
.incident-table {
  margin: 20px;
}
.el-tag {
  margin-right: 5px;
}

.el-table {
  ::-webkit-scrollbar {
    width: 15px;
    height: 15px;
  }
}

.el-table {
  ::-webkit-scrollbar-thumb {
    background-color: #B0E0E6;
    border-radius: 15px;
  }
}

.force-scrollbar /deep/ .el-table__body-wrapper {
  overflow-y: scroll !important;
}

</style>
