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
            <i :class="currentStatus === 'Rejected' ? 'el-icon-close' : 'el-icon-check'" />
          </div>
        </template>
      </el-step>
    </el-steps>

    <div class="workflow-actions">
      <el-form label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Current Status">
              <el-tag :type="getStatusType(currentStatus)">
                {{ currentStatus }}
              </el-tag>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Action">
              <el-select
                v-model="selectedAction"
                placeholder="Select action"
                :disabled="!canTakeAction"
                @change="handleActionChange"
              >
                <el-option
                  v-for="action in availableActions"
                  :key="action.value"
                  :label="action.label"
                  :value="action.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

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
    }
  },

  methods: {
    getStatusType(status) {
      const statusMap = {
        'Draft': 'info',
        'Pending Review': 'warning',
        'Under Review': 'warning',
        'Approved': 'success',
        'Rejected': 'danger'
      };
      return statusMap[status] || 'info';
    },

    handleActionChange() {
      // Emit event to parent component
      this.$emit('action-selected', !!this.selectedAction);
    },

    getWorkflowData() {
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
      
      return {
        previousStatus: this.currentStatus,
        newStatus,
        action: this.selectedAction,
        comments: this.comments,
        timestamp: new Date().toISOString()
      };
    },

    updateStatus(newStatus) {
      this.currentStatus = newStatus;
      this.selectedAction = '';
      this.comments = '';
      
      this.$emit('workflow-updated', {
        previousStatus: this.status,
        newStatus,
        timestamp: new Date().toISOString()
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.workflow-status {
  .custom-step-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background-color: #c0c4cc;
    color: #fff;
    
    &.active {
      background-color: #409eff;
    }
  }
  
  .workflow-actions {
    margin-top: 30px;
    padding: 20px;
    background-color: #f5f7fa;
    border-radius: 4px;
  }
}
</style>
