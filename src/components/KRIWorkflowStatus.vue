<template>
  <div class="workflow-status">
    <el-steps 
      :active="activeStep" 
      finish-status="success"
      simple
    >
      <el-step title="Draft" />
      <el-step title="Pending Review" />
      <el-step title="Under Review" />
      <el-step title="Approved/Rejected" />
    </el-steps>
    
    <div class="workflow-actions">
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
    </div>
  </div>
</template>

<script>
export default {
  name: 'KRIWorkflowStatus',
  
  props: {
    status: {
      type: String,
      required: true,
      validator: value => ['Draft', 'Pending Review', 'Under Review', 'Approved', 'Rejected'].includes(value)
    }
  },
  
  data() {
    return {
      currentStatus: this.status,
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
    status(newStatus) {
      this.currentStatus = newStatus;
      this.selectedAction = '';
      this.comments = '';
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
    margin-bottom: 20px;
  }
  
  .workflow-actions {
    margin-top: 20px;
    padding: 15px;
    background-color: #f8f8f8;
    border-radius: 4px;
  }
}
</style>
