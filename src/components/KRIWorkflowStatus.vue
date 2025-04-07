<template>
  <div class="workflow-status">
    <el-steps
      :active="activeStep"
      finish-status="success"
      process-status="process"
      align-center
    >
      <el-step title="Draft">
        <template slot="icon">
          <div :class="['custom-step-icon', { active: currentStatus === 'Draft' }]">
            <i class="el-icon-edit-outline" />
          </div>
        </template>
      </el-step>
      <el-step title="Pending Review">
        <template slot="icon">
          <div :class="['custom-step-icon', { active: currentStatus === 'Pending Review' }]">
            <i class="el-icon-document-checked" />
          </div>
        </template>
      </el-step>
      <el-step title="Under Review">
        <template slot="icon">
          <div :class="['custom-step-icon', { active: currentStatus === 'Under Review' }]">
            <i class="el-icon-view" />
          </div>
        </template>
      </el-step>
      <el-step title="Approved/Rejected">
        <template slot="icon">
          <div :class="['custom-step-icon', { active: currentStatus === 'Approved' || currentStatus === 'Rejected' }]">
            <i :class="currentStatus === 'Approved' ? 'el-icon-check' : (currentStatus === 'Rejected' ? 'el-icon-close' : 'el-icon-finished')" />
          </div>
        </template>
      </el-step>
    </el-steps>

    <div class="workflow-actions">
      <el-form label-width="120px">
        <el-form-item label="Current Status">
          <el-tag :type="getStatusType(currentStatus)">
            {{ currentStatus }}
          </el-tag>
        </el-form-item>

        <el-form-item label="Action">
          <el-select
            v-model="selectedAction"
            placeholder="Select action"
            :disabled="!canTakeAction"
          >
            <el-option
              v-for="action in availableActions"
              :key="action.value"
              :label="action.label"
              :value="action.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item
          v-if="selectedAction"
          label="Comments"
        >
          <el-input
            v-model="comments"
            type="textarea"
            :rows="3"
            placeholder="Add comments (optional)"
          />
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'KRIWorkflowStatus',

  props: {
    status: {
      type: String,
      default: 'Draft',
      validator: value => !value || ['Draft', 'Pending Review', 'Under Review', 'Approved', 'Rejected'].includes(value)
    }
  },

  data() {
    return {
      currentStatus: this.status || 'Draft',
      selectedAction: '',
      comments: ''
    };
  },

  computed: {
    activeStep() {
      const statusMap = {
        'Draft': 0,
        'Pending Review': 1,
        'Under Review': 2,
        'Approved': 3,
        'Rejected': 3
      };
      return statusMap[this.currentStatus] || 0;
    },

    availableActions() {
      const actions = {
        'Draft': [
          { label: 'Submit for Review', value: 'submit' }
        ],
        'Pending Review': [
          { label: 'Start Review', value: 'review' },
          { label: 'Return to Draft', value: 'return_draft' }
        ],
        'Under Review': [
          { label: 'Approve', value: 'approve' },
          { label: 'Reject', value: 'reject' },
          { label: 'Request Changes', value: 'request_changes' }
        ],
        'Approved': [
          { label: 'Reopen', value: 'reopen' }
        ],
        'Rejected': [
          { label: 'Reopen', value: 'reopen' }
        ]
      };

      return actions[this.currentStatus] || [];
    },

    canTakeAction() {
      return this.availableActions.length > 0;
    },

    workflowData() {
      return {
        status: this.currentStatus,
        action: this.selectedAction,
        comments: this.comments,
        timestamp: new Date().toISOString()
      };
    }
  },

  watch: {
    status: {
      immediate: true,
      handler(newStatus) {
        console.log('KRIWorkflowStatus - status prop changed:', newStatus);
        this.currentStatus = newStatus;
        this.selectedAction = '';
        this.comments = '';
      }
    },

    selectedAction(newAction) {
      if (newAction) {
        this.$emit('action-selected', {
          action: newAction,
          currentStatus: this.currentStatus
        });
      }
    }
  },

  mounted() {
    console.log('KRIWorkflowStatus mounted - status prop:', this.status);
    console.log('KRIWorkflowStatus mounted - currentStatus:', this.currentStatus);
    console.log('KRIWorkflowStatus mounted - activeStep:', this.activeStep);
    console.log('KRIWorkflowStatus mounted - availableActions:', this.availableActions);
  },

  methods: {
    getStatusType(status) {
      const typeMap = {
        'Draft': 'info',
        'Pending Review': 'warning',
        'Under Review': 'warning',
        'Approved': 'success',
        'Rejected': 'danger'
      };
      return typeMap[status] || 'info';
    },

    submitAction() {
      if (!this.selectedAction) {
        this.$message.warning('Please select an action');
        return null;
      }

      // Determine the new status based on the selected action
      let newStatus = this.currentStatus;

      switch (this.selectedAction) {
        case 'submit':
          newStatus = 'Pending Review';
          break;
        case 'review':
          newStatus = 'Under Review';
          break;
        case 'return_draft':
          newStatus = 'Draft';
          break;
        case 'approve':
          newStatus = 'Approved';
          break;
        case 'reject':
          newStatus = 'Rejected';
          break;
        case 'request_changes':
          newStatus = 'Draft';
          break;
        case 'reopen':
          newStatus = 'Draft';
          break;
      }

      // Update the current status
      this.currentStatus = newStatus;

      // Return the workflow data
      const result = {
        ...this.workflowData,
        newStatus
      };

      // Reset the form
      this.selectedAction = '';
      this.comments = '';

      // Emit the event
      this.$emit('workflow-updated', result);

      return result;
    }
  }
};
</script>

<style lang="scss" scoped>
.workflow-status {
  margin-bottom: 20px;

  .el-steps {
    margin-bottom: 30px;
  }

  .custom-step-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background-color: #f5f7fa;
    color: #909399;
    transition: all 0.3s;

    &.active {
      background-color: #409EFF;
      color: white;
      transform: scale(1.2);
      box-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
    }

    i {
      font-size: 16px;
    }
  }

  .workflow-actions {
    margin-top: 30px;
    padding: 20px;
    background-color: #f8f8f8;
    border-radius: 4px;
    border: 1px solid #ebeef5;
  }

  .el-tag {
    font-size: 14px;
    padding: 6px 12px;
  }

  .el-select {
    width: 100%;
    max-width: 300px;
  }

  .el-form-item {
    margin-bottom: 20px;
  }
}
</style>
