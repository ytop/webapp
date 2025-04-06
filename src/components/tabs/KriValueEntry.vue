
<template>
  <el-dialog
    :title="'KRI Value: ' + (kriData ? kriData.name : '')"
    :visible.sync="dialogVisible"
    width="70%"
    :before-close="handleClose"
    top="5vh"
  >
    <div style="padding: 0px 20px 10px 20px; background-color: #f8f8f8; margin: -20px -20px 10px -20px; display: flex; justify-content: space-between; align-items: center;">
      <div>
        Expected Collection Date: <el-tag size="mini">
          {{ expectedCollectionDate }}
        </el-tag> <span style="margin-left: 20px;" />
        KRI Capturer: <el-tag
          type="info"
          size="mini"
        >
          {{ kriCapturer }}
        </el-tag> <span style="margin-left: 20px;" />
        Breach Status: <el-tag
          :type="getBreachType(breachStatus)"
          size="mini"
        >
          {{ breachStatus }}
        </el-tag>
      </div>
      <el-button
        type="primary"
        size="mini"
        :loading="loading"
        @click="saveValue"
      >
        {{ loading ? 'Submitting...' : 'Action' }}
      </el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-tabs
          value="task"
          size="mini"
        >
          <el-tab-pane
            label="Task"
            name="task"
          >
            <el-card
              shadow="never"
              style="margin-bottom: 15px;"
            >
              <div slot="header">
                General
              </div>
              <el-form
                label-position="top"
                size="mini"
              >
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="Name">
                      <el-input
                        :value="kriData ? kriData.name : ''"
                        disabled
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="Description">
                      <el-input
                        :value="description"
                        disabled
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="8">
                    <el-form-item label="KRI Owner">
                      <el-input
                        :value="kriOwner"
                        disabled
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="KRI Capturer">
                      <el-input
                        :value="kriCapturer"
                        disabled
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="Expected Collection Date">
                      <el-input
                        :value="expectedCollectionDate"
                        disabled
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-card>

            <el-card
              shadow="never"
              style="margin-bottom: 15px;"
            >
              <div slot="header">
                KRI Value
              </div>
              <el-form
                ref="kriValueForm"
                label-position="top"
                size="mini"
              >
                <el-row :gutter="20">
                  <el-col :span="6">
                    <el-form-item
                      label="Value"
                      prop="value"
                      :rules="[{ required: true, message: 'Value is required', trigger: 'blur' }]"
                    >
                      <el-input
                        v-model="formData.value"
                        placeholder="Enter KRI Value"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="6">
                    <el-form-item
                      label="Value Date"
                      prop="valueDate"
                      :rules="[{ required: true, message: 'Value Date is required', trigger: 'change' }]"
                    >
                      <el-date-picker
                        v-model="formData.valueDate"
                        type="date"
                        placeholder="Select date"
                        style="width: 100%;"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="6">
                    <el-form-item label="Collection Status">
                      <el-input
                        :value="collectionStatus"
                        disabled
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="6">
                    <el-form-item label="Breach Status">
                      <el-input
                        :value="breachStatus"
                        disabled
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="24">
                    <el-form-item label="KRI Value Approval Required">
                      <el-switch
                        v-model="approvalRequired"
                        disabled
                        active-text="Yes"
                        inactive-text="No"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-card>

            <el-card shadow="never">
              <div slot="header">
                Related Content
              </div>
              <el-tabs size="mini">
                <el-tab-pane label="Parent KRI" />
                <el-tab-pane label="Issues" />
                <el-tab-pane label="Related Content" />
              </el-tabs>
              <div style="padding: 10px; color: grey; font-size: 12px;">
                Related content details will appear here.
              </div>
            </el-card>
          </el-tab-pane>
          <el-tab-pane
            label="Activity"
            name="activity"
          >
            Activity Log Placeholder
          </el-tab-pane>
          <el-tab-pane
            label="Admin"
            name="admin"
          >
            Admin Settings Placeholder
          </el-tab-pane>
        </el-tabs>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never">
          <div
            slot="header"
            style="display: flex; justify-content: space-between; align-items: center;"
          >
            <span>Details</span>
            <el-button
              type="text"
              icon="el-icon-close"
              style="float: right; padding: 3px 0"
              @click="handleClose"
            />
          </div>
          <div>
            <div style="margin-bottom: 15px;">
              <div style="font-size: 12px; color: grey;">
                Stage
              </div>
              <div>Enter KRI Value (KRI Capture)</div>
            </div>
            <div style="margin-bottom: 15px;">
              <div style="font-size: 12px; color: grey;">
                Due Date
              </div>
              <div>{{ expectedCollectionDate }}</div>
            </div>
            <div style="margin-bottom: 15px;">
              <div style="font-size: 12px; color: grey;">
                Tags
              </div>
              <div style="color: grey;">
                No tags have been added yet.
              </div>
              <el-button
                type="text"
                size="mini"
              >
                + Add Tag
              </el-button>
            </div>
            <el-divider />
            <div style="margin-bottom: 15px;">
              <div style="font-weight: bold;">
                KRI Value Entry
              </div>
              <div style="font-size: 12px; color: grey; margin-top: 5px;">
                Enter your KRI value and date information.
              </div>
              <div
                v-if="!formData.value || !formData.valueDate"
                style="color: #f56c6c; font-size: 12px; margin-top: 10px;"
              >
                <i class="el-icon-warning-outline" /> 2 items require attention.
                <ul>
                  <li v-if="!formData.value">
                    Value
                  </li>
                  <li v-if="!formData.valueDate">
                    Value Date
                  </li>
                </ul>
              </div>
            </div>
            <el-divider />
            <el-button
              type="primary"
              style="width: 100%;"
              :loading="loading"
              :disabled="!formData.value || !formData.valueDate"
              @click="saveValue"
            >
              {{ loading ? 'Submitting...' : 'Select an action to validate' }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </el-dialog>
</template>

<script>
export default {
  name: 'KriValueEntry',
  props: {
    // Controls dialog visibility (use .sync modifier)
    visible: {
      type: Boolean,
      default: false
    },
    // Data for the selected KRI passed from the parent (Task.vue)
    kriData: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      // Static data based on Collect.jpg (can be made dynamic later)
      description: 'Non-standard contracts - 2023-06-14', // Note: Date discrepancy from image
      kriOwner: 'ORM User',
      kriCapturer: 'ORM User',
      expectedCollectionDate: '9/2/2013', // Should ideally come from kriData if available
      collectionStatus: 'Awaiting Collection', // Should ideally come from kriData
      breachStatus: 'Not Determined', // Should ideally come from kriData
      approvalRequired: false,
      loading: false, // Loading state for form submission
      // Form data model
      formData: {
        value: '',
        valueDate: null
      }
    };
  },
  computed: {
    // Computed property to manage dialog visibility via prop
    dialogVisible: {
      get() {
        return this.visible;
      },
      set(value) {
        this.$emit('update:visible', value); // Notify parent about visibility change
      }
    }
  },
  watch: {
    // Reset form when dialog opens with new data
    kriData(newData) {
      if (newData) {
        this.resetForm();
        // You might want to populate expectedCollectionDate etc. from newData if available
        this.collectionStatus = newData.collectionStatus || 'Awaiting Collection';
        this.breachStatus = newData.breachStatus || 'Not Determined';
        // Example: If expected date was part of the task list data
        // this.expectedCollectionDate = newData.expectedDate || '9/2/2013';
      }
    },
    // Optionally, update breach status dynamically based on entered value (requires threshold logic)
    // 'formData.value'(newValue) { ... }
  },
  methods: {
    handleClose() {
      this.dialogVisible = false; // Close the dialog
      this.resetForm();
    },
    resetForm() {
      // Reset form fields when dialog opens or closes
      this.formData.value = '';
      this.formData.valueDate = null;
      if (this.$refs.kriValueForm) {
        this.$refs.kriValueForm.clearValidate(); // Clear validation errors
      }
    },
    /**
     * Save the KRI value
     */
    async saveValue() {
      // Validate the form
      this.$refs.kriValueForm.validate(async (valid) => {
        if (valid) {
          try {
            // Show loading state
            this.loading = true;

            // Prepare data for submission
            const submissionData = {
              id: this.kriData.id,
              name: this.kriData.name,
              value: this.formData.value,
              valueDate: this.formData.valueDate,
              collectionStatus: 'Awaiting Approval', // Update status
              breachStatus: this.determineBreachStatus(this.formData.value)
            };

            // Import and use the API function if needed
            // const response = await submitKriValue(submissionData);

            // For now, simulate API call with a timeout
            await new Promise(resolve => setTimeout(resolve, 500));

            // Emit event to parent component with updated data
            this.$emit('value-submitted', submissionData);

            console.log('KRI Value submitted:', submissionData);
            this.handleClose();
          } catch (error) {
            console.error('Error submitting KRI value:', error);
            this.$message.error('Failed to submit KRI value. Please try again.');
          } finally {
            this.loading = false;
          }
        } else {
          console.log('Form validation failed');
          this.$message.error('Please fill in the required fields.');
          return false;
        }
      });
    },

    /**
     * Determine breach status based on value
     * @param {String|Number} value - The KRI value
     * @returns {String} The breach status
     */
    determineBreachStatus(value) {
      // This is a placeholder logic - should be replaced with actual threshold logic
      const numValue = parseFloat(value);
      if (isNaN(numValue)) return 'Not Determined';

      if (numValue >= 5) return 'Red';
      if (numValue >= 0.5) return 'Yellow';
      return 'Green';
    },
    // Helper to determine tag type based on breach status (copied from Task.vue for consistency)
    getBreachType(status) {
      switch(status) {
        case 'Red': return 'danger';
        case 'Yellow': return 'warning';
        case 'Not Determined': return 'info';
        default: return 'info'; // Default or if null/empty
      }
    }
  }
}
</script>

<style scoped>
/* Add specific styles for KriValueEntry if needed */
.el-form-item--mini.el-form-item {
  margin-bottom: 10px; /* Reduce margin for mini forms */
}
.el-card__header {
  padding: 10px 15px; /* Adjust card header padding */
  font-weight: bold;
  font-size: 14px;
}
.el-card__body {
  padding: 15px; /* Adjust card body padding */
}
/* Style the disabled inputs to look more like static text */
.el-input.is-disabled .el-input__inner {
  background-color: #FFF;
  border-color: #E4E7ED;
  color: #606266;
  cursor: default;
}
.el-switch.is-disabled .el-switch__core{
  cursor: default;
}
.el-switch.is-disabled .el-switch__label{
  cursor: default;
}
</style>
