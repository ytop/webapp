<template>
  <div class="kri-edit-dialog">
    <!-- Workflow Status Section at the top -->
    <div class="workflow-section">
      <h3>Workflow Status</h3>
      <kri-workflow-status
        v-if="form && form.workflowStatus"
        ref="workflowStatus"
        :status="form.workflowStatus"
        @workflow-updated="handleWorkflowUpdate"
        @action-selected="handleActionSelected"
      />
      <div
        v-else
        class="loading-placeholder"
      >
        <i class="el-icon-loading" /> Loading workflow status...
      </div>
    </div>

    <!-- KRI Details Form -->
    <el-form
      ref="form"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="kri-form"
    >
      <el-tabs v-model="activeTab">
        <el-tab-pane
          label="Basic Information"
          name="basic"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item
                label="KRI ID"
                prop="kriId"
              >
                <el-input
                  v-model="form.kriId"
                  disabled
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item
                label="Name"
                prop="name"
              >
                <el-input v-model="form.name" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item
            label="Description"
            prop="kriDesc"
          >
            <el-input
              v-model="form.kriDesc"
              type="textarea"
              :rows="3"
            />
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item
                label="Data Provider"
                prop="dataProvider"
              >
                <el-input v-model="form.dataProvider" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item
                label="Owner"
                prop="owner"
              >
                <el-input v-model="form.owner" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item
                label="L1 Risk Type"
                prop="l1RiskType"
              >
                <el-select
                  v-model="form.l1RiskType"
                  placeholder="Select risk type"
                  style="width: 100%"
                >
                  <el-option
                    v-for="type in riskTypes"
                    :key="type"
                    :label="type"
                    :value="type"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item
                label="L2 Risk Type"
                prop="l2RiskType"
              >
                <el-select
                  v-model="form.l2RiskType"
                  placeholder="Select L2 risk type"
                  style="width: 100%"
                >
                  <el-option
                    v-for="type in l2RiskTypeOptions"
                    :key="type"
                    :label="type"
                    :value="type"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item
                label="Report Cycle"
                prop="reportCycle"
              >
                <el-select
                  v-model="form.reportCycle"
                  placeholder="Select report cycle"
                  style="width: 100%"
                >
                  <el-option
                    v-for="cycle in reportCycles"
                    :key="cycle"
                    :label="cycle"
                    :value="cycle"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item
                label="RAS Status"
                prop="rasStatus"
              >
                <el-select
                  v-model="form.rasStatus"
                  placeholder="Select RAS status"
                  style="width: 100%"
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
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item
            label="Status"
            prop="status"
          >
            <el-radio-group v-model="form.status">
              <el-radio label="Active">
                Active
              </el-radio>
              <el-radio label="Inactive">
                Inactive
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-tab-pane>

        <el-tab-pane
          label="Thresholds"
          name="thresholds"
        >
          <el-form-item
            label="Yellow Threshold"
            prop="yellowThreshold"
          >
            <el-input-number
              v-model="form.yellowThreshold"
              :min="0"
              :precision="2"
              :step="1"
              style="width: 200px"
            />
          </el-form-item>

          <el-form-item
            label="Red Threshold"
            prop="redThreshold"
          >
            <el-input-number
              v-model="form.redThreshold"
              :min="0"
              :precision="2"
              :step="1"
              style="width: 200px"
            />
          </el-form-item>
        </el-tab-pane>
      </el-tabs>
    </el-form>

    <!-- Document Management Section at the bottom -->
    <div class="document-section">
      <kri-document-upload
        v-if="form.kriId"
        ref="documentUpload"
        :kri-id="form.kriId"
        :existing-documents="form.documents"
        @document-deleted="handleDocumentDeleted"
      />
    </div>

    <span
      slot="footer"
      class="dialog-footer"
    >
      <el-button @click="handleClose">Cancel</el-button>
      <el-button
        v-if="workflowActionSelected"
        type="primary"
        :loading="submitting"
        @click="submitWorkflow"
      >
        Submit Workflow Action
      </el-button>
      <el-button
        type="primary"
        :loading="submitting"
        @click="saveKRI"
      >
        Save
      </el-button>
    </span>
  </div>
</template>

<script>
import { updateKRI } from '@/api/kri';
import KRIDocumentUpload from './KRIDocumentUpload.vue';
import KRIWorkflowStatus from './KRIWorkflowStatus.vue';

export default {
  name: 'KRIEditDialog',

  components: {
    'kri-document-upload': KRIDocumentUpload,
    'kri-workflow-status': KRIWorkflowStatus
  },

  props: {
    kriData: {
      type: Object,
      default: () => ({})
    }
  },

  data() {
    return {
      activeTab: 'basic',
      form: {
        kriId: '',
        name: '',
        kriDesc: '',
        dataProvider: '',
        owner: '',
        l1RiskType: '',
        l2RiskType: '',
        reportCycle: '',
        rasStatus: '',
        status: 'Active',
        yellowThreshold: 0,
        redThreshold: 0,
        documents: [],
        workflowStatus: 'Draft'
      },
      rules: {
        name: [
          { required: true, message: 'Please enter KRI name', trigger: 'blur' }
        ],
        kriDesc: [
          { required: true, message: 'Please enter KRI description', trigger: 'blur' }
        ],
        dataProvider: [
          { required: true, message: 'Please enter data provider', trigger: 'blur' }
        ],
        owner: [
          { required: true, message: 'Please enter owner', trigger: 'blur' }
        ],
        l1RiskType: [
          { required: true, message: 'Please select L1 risk type', trigger: 'change' }
        ],
        l2RiskType: [
          { required: true, message: 'Please select L2 risk type', trigger: 'change' }
        ],
        reportCycle: [
          { required: true, message: 'Please select report cycle', trigger: 'change' }
        ],
        rasStatus: [
          { required: true, message: 'Please select RAS status', trigger: 'change' }
        ]
      },
      riskTypes: ['Credit Risk', 'Market Risk', 'Operational Risk', 'Liquidity Risk', 'Compliance Risk', 'Technology Risk', 'Strategic Risk'],
      l2RiskTypeMap: {
        'Credit Risk': ['Default Risk', 'Concentration Risk', 'Country Risk', 'Settlement Risk'],
        'Market Risk': ['Interest Rate Risk', 'Foreign Exchange Risk', 'Commodity Risk', 'Equity Risk'],
        'Operational Risk': ['Process Risk', 'People Risk', 'System Risk', 'External Events'],
        'Liquidity Risk': ['Funding Risk', 'Market Liquidity Risk', 'Asset Liability Mismatch', 'Contingency Risk'],
        'Compliance Risk': ['Regulatory Risk', 'Legal Risk', 'Conduct Risk', 'Financial Crime Risk'],
        'Technology Risk': ['Cybersecurity Risk', 'Data Integrity Risk', 'System Availability Risk', 'IT Change Risk'],
        'Strategic Risk': ['Business Model Risk', 'Reputation Risk', 'Macroeconomic Risk', 'Competition Risk']
      },
      reportCycles: ['Daily', 'Weekly', 'Monthly', 'Quarterly'],
      submitting: false,
      workflowActionSelected: false
    };
  },

  computed: {
    l2RiskTypeOptions() {
      return this.form.l1RiskType ? this.l2RiskTypeMap[this.form.l1RiskType] || [] : [];
    }
  },

  watch: {
    kriData: {
      immediate: true,
      handler(newData) {
        if (newData && Object.keys(newData).length > 0) {
          this.initializeForm(newData);
        }
      }
    },
    'form.l1RiskType'(newValue) {
      // Reset L2 risk type when L1 changes
      if (newValue && (!this.form.l2RiskType || !this.l2RiskTypeOptions.includes(this.form.l2RiskType))) {
        this.form.l2RiskType = '';
      }
    }
  },

  methods: {
    initializeForm(data) {
      this.form = {
        kriId: data.kriId || '',
        name: data.name || '',
        kriDesc: data.kriDesc || '',
        dataProvider: data.dataProvider || '',
        owner: data.owner || '',
        l1RiskType: data.l1RiskType || '',
        l2RiskType: data.l2RiskType || '',
        reportCycle: data.reportCycle || '',
        rasStatus: data.rasStatus || '',
        status: data.status || 'Active',
        yellowThreshold: data.threshold?.yellow || 0,
        redThreshold: data.threshold?.red || 0,
        documents: data.documents || [],
        workflowStatus: data.workflowStatus || 'Draft'
      };
    },

    handleClose() {
      this.$emit('close');
    },

    handleActionSelected(selected) {
      this.workflowActionSelected = selected;
    },

    async submitWorkflow() {
      if (!this.$refs.workflowStatus) return;

      this.submitting = true;
      try {
        const workflowData = this.$refs.workflowStatus.getWorkflowData();
        console.log('Submitting workflow action:', workflowData);

        // Update the KRI with the new workflow status
        const kriData = {
          ...this.form,
          workflowStatus: workflowData.newStatus,
          threshold: {
            yellow: this.form.yellowThreshold,
            red: this.form.redThreshold
          }
        };

        const response = await updateKRI(this.form.kriId, kriData);

        this.$message.success(`Workflow action submitted successfully. New status: ${workflowData.newStatus}`);
        this.$emit('kri-updated', response.updatedKRI);
        this.handleClose();
      } catch (error) {
        this.$message.error('Failed to submit workflow action');
        console.error('Error submitting workflow action:', error);
      } finally {
        this.submitting = false;
      }
    },

    async saveKRI() {
      try {
        await this.$refs.form.validate();
        this.submitting = true;

        // Upload any pending documents
        let uploadedDocs = [];
        if (this.$refs.documentUpload) {
          uploadedDocs = await this.$refs.documentUpload.uploadFiles();
        }

        // Prepare the KRI data for saving
        const kriData = {
          name: this.form.name,
          kriDesc: this.form.kriDesc,
          dataProvider: this.form.dataProvider,
          owner: this.form.owner,
          l1RiskType: this.form.l1RiskType,
          l2RiskType: this.form.l2RiskType,
          reportCycle: this.form.reportCycle,
          rasStatus: this.form.rasStatus,
          status: this.form.status,
          threshold: {
            yellow: this.form.yellowThreshold,
            red: this.form.redThreshold
          },
          documents: [...this.form.documents, ...uploadedDocs],
          workflowStatus: this.form.workflowStatus,
          lastUpdated: new Date().toISOString()
        };

        // Call the API to update the KRI
        const response = await updateKRI(this.form.kriId, kriData);

        this.$message.success('KRI updated successfully');
        this.$emit('kri-updated', response.updatedKRI);
        this.handleClose();
      } catch (error) {
        this.$message.error('Failed to update KRI');
        console.error('Error updating KRI:', error);
      } finally {
        this.submitting = false;
      }
    },

    handleDocumentDeleted(document) {
      // Remove the document from the form data
      const index = this.form.documents.findIndex(doc => doc.id === document.id);
      if (index !== -1) {
        this.form.documents.splice(index, 1);
      }
    },

    handleWorkflowUpdate(workflowData) {
      this.form.workflowStatus = workflowData.newStatus;
      this.$message.success(`Workflow status updated to ${workflowData.newStatus}`);
    }
  }
};
</script>

<style lang="scss" scoped>
.kri-edit-dialog {
  .workflow-section {
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid #ebeef5;

    h3 {
      margin-top: 0;
      margin-bottom: 15px;
      font-size: 16px;
      font-weight: 500;
    }
  }

  .kri-form {
    margin-bottom: 20px;
  }

  .document-section {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
  }

  .loading-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100px;
    color: #909399;
  }

  .dialog-footer {
    display: block;
    text-align: right;
    margin-top: 20px;
  }
}
</style>
