<template>
  <div class="kri-edit-dialog">
    <el-tabs v-model="activeTab">
      <el-tab-pane
        label="Basic Information"
        name="basic"
      >
        <el-form
          ref="kriForm"
          :model="form"
          :rules="formRules"
          label-width="140px"
          size="small"
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

              <el-form-item
                label="KRI Name"
                prop="name"
              >
                <el-input v-model="form.name" />
              </el-form-item>

              <el-form-item
                label="Description"
                prop="kriDesc"
              >
                <el-input
                  v-model="form.kriDesc"
                  type="textarea"
                  :rows="4"
                  :maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item
                label="Data Provider"
                prop="dataProvider"
              >
                <el-input v-model="form.dataProvider" />
              </el-form-item>

              <el-form-item
                label="Owner"
                prop="owner"
              >
                <el-input v-model="form.owner" />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item
                label="L1 Risk Type"
                prop="l1RiskType"
              >
                <el-select
                  v-model="form.l1RiskType"
                  placeholder="Select L1 Risk Type"
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

              <el-form-item
                label="L2 Risk Type"
                prop="l2RiskType"
              >
                <el-select
                  v-model="form.l2RiskType"
                  placeholder="Select L2 Risk Type"
                  style="width: 100%"
                  :disabled="!form.l1RiskType"
                >
                  <el-option
                    v-for="type in l2RiskTypeOptions"
                    :key="type"
                    :label="type"
                    :value="type"
                  />
                </el-select>
              </el-form-item>

              <el-form-item
                label="Report Cycle"
                prop="reportCycle"
              >
                <el-select
                  v-model="form.reportCycle"
                  placeholder="Select Report Cycle"
                  style="width: 100%"
                >
                  <el-option
                    label="Daily"
                    value="Daily"
                  />
                  <el-option
                    label="Weekly"
                    value="Weekly"
                  />
                  <el-option
                    label="Monthly"
                    value="Monthly"
                  />
                  <el-option
                    label="Quarterly"
                    value="Quarterly"
                  />
                </el-select>
              </el-form-item>

              <el-form-item
                label="RAS Status"
                prop="rasStatus"
              >
                <el-radio-group v-model="form.rasStatus">
                  <el-radio label="RAS">
                    RAS
                  </el-radio>
                  <el-radio label="Non-RAS">
                    Non-RAS
                  </el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item
                label="Status"
                prop="status"
              >
                <el-select
                  v-model="form.status"
                  placeholder="Select Status"
                  style="width: 100%"
                >
                  <el-option
                    label="Active"
                    value="Active"
                  />
                  <el-option
                    label="Inactive"
                    value="Inactive"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-divider content-position="left">
            Thresholds
          </el-divider>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item
                label="Yellow Threshold"
                prop="yellowThreshold"
              >
                <el-input-number
                  v-model="form.yellowThreshold"
                  :precision="2"
                  :step="1"
                  :min="0"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item
                label="Red Threshold"
                prop="redThreshold"
              >
                <el-input-number
                  v-model="form.redThreshold"
                  :precision="2"
                  :step="1"
                  :min="0"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-tab-pane>

      <el-tab-pane
        label="Documents"
        name="documents"
      >
        <kri-document-upload
          ref="documentUpload"
          :kri-id="form.kriId"
          :existing-documents="form.documents"
          @document-deleted="handleDocumentDeleted"
        />
      </el-tab-pane>

      <el-tab-pane
        label="Workflow"
        name="workflow"
      >
        <kri-workflow-status
          ref="workflowStatus"
          :status="form.workflowStatus"
          @workflow-updated="handleWorkflowUpdate"
          @action-selected="handleActionSelected"
        />
      </el-tab-pane>
    </el-tabs>

    <span
      slot="footer"
      class="dialog-footer"
    >
      <el-button @click="handleClose">Cancel</el-button>
      <el-button
        v-if="activeTab === 'workflow' && workflowActionSelected"
        type="primary"
        :loading="submitting"
        @click="submitWorkflow"
      >
        Submit Workflow Action
      </el-button>
      <el-button
        v-else
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
    KRIDocumentUpload,
    KRIWorkflowStatus
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
      submitting: false,
      workflowActionSelected: false,

      // Form data
      form: {
        kriId: '',
        name: '',
        kriDesc: '',
        dataProvider: '',
        owner: '',
        l1RiskType: '',
        l2RiskType: '',
        reportCycle: '',
        rasStatus: 'RAS',
        status: 'Active',
        yellowThreshold: 0,
        redThreshold: 0,
        documents: [],
        workflowStatus: 'Draft'
      },

      // Form validation rules
      formRules: {
        name: [
          { required: true, message: 'Please enter KRI name', trigger: 'blur' },
          { min: 3, max: 100, message: 'Length should be 3 to 100 characters', trigger: 'blur' }
        ],
        kriDesc: [
          { required: true, message: 'Please enter KRI description', trigger: 'blur' },
          { min: 3, max: 500, message: 'Length should be 3 to 500 characters', trigger: 'blur' }
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
        ]
      },

      // Risk type options
      riskTypes: ['Credit Risk', 'Market Risk', 'Operational Risk', 'Liquidity Risk', 'Compliance Risk', 'Technology Risk', 'Strategic Risk'],

      l2RiskTypesMap: {
        'Credit Risk': ['Default Risk', 'Concentration Risk', 'Country Risk', 'Settlement Risk'],
        'Market Risk': ['Interest Rate Risk', 'Foreign Exchange Risk', 'Commodity Risk', 'Equity Risk'],
        'Operational Risk': ['Process Risk', 'People Risk', 'System Risk', 'External Events'],
        'Liquidity Risk': ['Funding Risk', 'Market Liquidity Risk', 'Asset Liability Mismatch', 'Contingency Risk'],
        'Compliance Risk': ['Regulatory Risk', 'Legal Risk', 'Conduct Risk', 'Financial Crime Risk'],
        'Technology Risk': ['Cybersecurity Risk', 'Data Integrity Risk', 'System Availability Risk', 'IT Change Risk'],
        'Strategic Risk': ['Business Model Risk', 'Reputation Risk', 'Macroeconomic Risk', 'Competition Risk']
      }
    };
  },

  computed: {
    l2RiskTypeOptions() {
      return this.form.l1RiskType ? this.l2RiskTypesMap[this.form.l1RiskType] || [] : [];
    }
  },

  watch: {

    kriData: {
      immediate: true,
      handler(newData) {
        console.log('KRIEditDialog - kriData prop changed:', newData);
        if (newData && Object.keys(newData).length > 0) {
          this.initializeForm(newData);
        }
      }
    }
  },

  mounted() {
    console.log('KRIEditDialog mounted - kriData:', this.kriData);
  },

  methods: {
    initializeForm(data) {
      // Initialize form with KRI data
      this.form = {
        kriId: data.kriId || '',
        name: data.name || '',
        kriDesc: data.kriDesc || '',
        dataProvider: data.dataProvider || '',
        owner: data.owner || '',
        l1RiskType: data.l1RiskType || '',
        l2RiskType: data.l2RiskType || '',
        reportCycle: data.reportCycle || '',
        rasStatus: data.rasStatus || 'RAS',
        status: data.status || 'Active',
        yellowThreshold: (data.threshold && data.threshold.yellow) || 0,
        redThreshold: (data.threshold && data.threshold.red) || 0,
        documents: data.documents || [],
        workflowStatus: data.workflowStatus || 'Draft'
      };
    },

    handleClose() {
      this.activeTab = 'basic';
      this.workflowActionSelected = false;
    },

    async saveKRI() {
      try {
        await this.$refs.kriForm.validate();
      } catch (error) {
        this.$message.error('Please fix the form errors before saving');
        return;
      }

      this.submitting = true;

      try {
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
    },

    handleActionSelected() {
      this.workflowActionSelected = true;
    },

    async submitWorkflow() {
      if (!this.$refs.workflowStatus) {
        this.$message.error('Workflow component not initialized');
        return;
      }

      const workflowResult = this.$refs.workflowStatus.submitAction();
      if (!workflowResult) {
        return;
      }

      this.submitting = true;

      try {
        // Update the KRI with the new workflow status
        const kriData = {
          ...this.kriData,
          workflowStatus: workflowResult.newStatus,
          lastUpdated: new Date().toISOString()
        };

        // Call the API to update the KRI
        const response = await updateKRI(this.form.kriId, kriData);

        this.$message.success(`Workflow action submitted successfully. New status: ${workflowResult.newStatus}`);
        this.$emit('kri-updated', response.updatedKRI);
        this.workflowActionSelected = false;
      } catch (error) {
        this.$message.error('Failed to submit workflow action');
        console.error('Error submitting workflow action:', error);
      } finally {
        this.submitting = false;
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.kri-edit-dialog {
  .el-tabs {
    margin-bottom: 20px;
  }

  .el-divider {
    margin: 20px 0;
  }
}
</style>
