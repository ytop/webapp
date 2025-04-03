<template>
  <div class="kri-inventory-container">
    <div class="page-header">
      <h1 class="page-title">KRI Inventory Management</h1>
      <el-button type="primary" icon="el-icon-plus" @click="showAddKRIDialog">Add New KRI</el-button>
    </div>

    <!-- KRI Inventory Table -->
    <el-table
      :data="kriList"
      border
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="kriId" label="KRI ID" width="120" sortable />
      <el-table-column prop="kriDesc" label="KRI Description" min-width="250" show-overflow-tooltip />
      <el-table-column prop="riskCategory" label="Risk Category" width="150" sortable />
      <el-table-column prop="measurementFrequency" label="Frequency" width="120" />
      <el-table-column label="Thresholds" width="180">
        <template slot-scope="scope">
          <div>
            <el-tag type="warning">Warning: {{ scope.row.warningThreshold }}</el-tag>
          </div>
          <div style="margin-top: 5px;">
            <el-tag type="danger">Critical: {{ scope.row.criticalThreshold }}</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="owner" label="Owner" width="150" />
      <el-table-column prop="status" label="Status" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 'Active' ? 'success' : 'info'">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="150" fixed="right">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            icon="el-icon-edit"
            circle
            @click="handleEdit(scope.row)"
          />
          <el-button
            size="mini"
            type="danger"
            icon="el-icon-delete"
            circle
            @click="handleDelete(scope.row)"
          />
        </template>
      </el-table-column>
    </el-table>

    <!-- Add/Edit KRI Dialog -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="50%">
      <el-form :model="kriForm" :rules="rules" ref="kriForm" label-width="150px">
        <el-form-item label="KRI ID" prop="kriId" :disabled="editMode">
          <el-input v-model="kriForm.kriId" :disabled="editMode" />
        </el-form-item>
        <el-form-item label="KRI Description" prop="kriDesc">
          <el-input v-model="kriForm.kriDesc" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="Risk Category" prop="riskCategory">
          <el-select v-model="kriForm.riskCategory" placeholder="Select Risk Category" style="width: 100%">
            <el-option
              v-for="item in riskCategories"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Measurement Frequency" prop="measurementFrequency">
          <el-select v-model="kriForm.measurementFrequency" placeholder="Select Frequency" style="width: 100%">
            <el-option label="Daily" value="Daily" />
            <el-option label="Weekly" value="Weekly" />
            <el-option label="Monthly" value="Monthly" />
            <el-option label="Quarterly" value="Quarterly" />
            <el-option label="Annually" value="Annually" />
          </el-select>
        </el-form-item>
        <el-form-item label="Warning Threshold" prop="warningThreshold">
          <el-input-number v-model="kriForm.warningThreshold" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Critical Threshold" prop="criticalThreshold">
          <el-input-number v-model="kriForm.criticalThreshold" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Owner" prop="owner">
          <el-input v-model="kriForm.owner" />
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-radio-group v-model="kriForm.status">
            <el-radio label="Active">Active</el-radio>
            <el-radio label="Inactive">Inactive</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="submitForm">Save</el-button>
      </span>
    </el-dialog>

    <!-- Delete Confirmation Dialog -->
    <el-dialog
      title="Confirm Delete"
      :visible.sync="deleteDialogVisible"
      width="30%"
    >
      <p>Are you sure you want to delete KRI <strong>{{ selectedKRI.kriId }}</strong>?</p>
      <p>This action cannot be undone.</p>
      <span slot="footer" class="dialog-footer">
        <el-button @click="deleteDialogVisible = false">Cancel</el-button>
        <el-button type="danger" @click="confirmDelete">Delete</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getKRIs, addKRI, updateKRI, deleteKRI } from '@/api/kri'

export default {
  name: 'KriInventory',
  data() {
    return {
      kriList: [],
      loading: false,
      dialogVisible: false,
      deleteDialogVisible: false,
      editMode: false,
      dialogTitle: 'Add New KRI',
      selectedKRI: {},
      kriForm: {
        kriId: '',
        kriDesc: '',
        riskCategory: '',
        measurementFrequency: 'Monthly',
        warningThreshold: 0,
        criticalThreshold: 0,
        owner: '',
        status: 'Active'
      },
      rules: {
        kriId: [
          { required: true, message: 'Please input KRI ID', trigger: 'blur' },
          { min: 3, max: 20, message: 'Length should be 3 to 20 characters', trigger: 'blur' }
        ],
        kriDesc: [
          { required: true, message: 'Please input KRI description', trigger: 'blur' }
        ],
        riskCategory: [
          { required: true, message: 'Please select risk category', trigger: 'change' }
        ],
        measurementFrequency: [
          { required: true, message: 'Please select measurement frequency', trigger: 'change' }
        ],
        owner: [
          { required: true, message: 'Please input owner/department', trigger: 'blur' }
        ]
      },
      riskCategories: [
        'Credit Risk',
        'Market Risk',
        'Liquidity Risk',
        'Operational Risk',
        'Compliance Risk',
        'Strategic Risk',
        'Reputational Risk',
        'Legal Risk',
        'Cyber Risk',
        'Other'
      ]
    }
  },
  created() {
    this.fetchKRIs()
  },
  methods: {
    async fetchKRIs() {
      this.loading = true
      try {
        const response = await getKRIs()
        this.kriList = response
      } catch (error) {
        this.$message.error('Failed to fetch KRI data.')
        console.error(error)
      } finally {
        this.loading = false
      }
    },
    showAddKRIDialog() {
      this.editMode = false
      this.dialogTitle = 'Add New KRI'
      this.resetForm()
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.editMode = true
      this.dialogTitle = 'Edit KRI'
      this.selectedKRI = row
      
      // Copy values to form
      this.kriForm = { ...row }
      
      this.dialogVisible = true
    },
    handleDelete(row) {
      this.selectedKRI = row
      this.deleteDialogVisible = true
    },
    resetForm() {
      if (this.$refs.kriForm) {
        this.$refs.kriForm.resetFields()
      }
      
      this.kriForm = {
        kriId: '',
        kriDesc: '',
        riskCategory: '',
        measurementFrequency: 'Monthly',
        warningThreshold: 0,
        criticalThreshold: 0,
        owner: '',
        status: 'Active'
      }
    },
    submitForm() {
      this.$refs.kriForm.validate(async (valid) => {
        if (valid) {
          try {
            if (this.editMode) {
              // Update existing KRI
              await updateKRI(this.selectedKRI.kriId, this.kriForm)
              this.$message.success('KRI updated successfully!')
            } else {
              // Add new KRI
              const result = await addKRI(this.kriForm)
              if (result.success) {
                this.$message.success('KRI added successfully!')
              } else {
                this.$message.error(result.message || 'Failed to add KRI.')
                return
              }
            }
            
            this.dialogVisible = false
            this.fetchKRIs() // Refresh the list
          } catch (error) {
            this.$message.error('Operation failed. Please try again.')
            console.error(error)
          }
        } else {
          return false
        }
      })
    },
    async confirmDelete() {
      try {
        await deleteKRI(this.selectedKRI.kriId)
        this.$message.success('KRI deleted successfully!')
        this.deleteDialogVisible = false
        this.fetchKRIs() // Refresh the list
      } catch (error) {
        this.$message.error('Failed to delete KRI.')
        console.error(error)
      }
    }
  }
}
</script>

<style scoped>
.kri-inventory-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  color: #303133;
}
</style>
