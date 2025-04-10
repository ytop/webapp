<template>
  <div class="kri-value-entry">
    <el-form
      ref="form"
      :model="form"
      :rules="rules"
      label-width="120px"
      :disabled="isReadOnly"
    >
      <!-- KRI Information Section -->
      <div class="section-header">
        <h3>KRI Information</h3>
      </div>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="KRI Name">
            <el-input
              v-model="form.name"
              disabled
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Path">
            <el-input
              v-model="form.path"
              disabled
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="Status">
            <el-tag :type="getStatusType(form.collectionStatus)">
              {{ form.collectionStatus }}
            </el-tag>
          </el-form-item>
        </el-col>
        <el-col
          v-if="form.breachStatus !== 'Not Determined'"
          :span="12"
        >
          <el-form-item label="Breach Status">
            <el-tag :type="getBreachStatusType(form.breachStatus)">
              {{ form.breachStatus }}
            </el-tag>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- Value Entry Section -->
      <div class="section-header">
        <h3>Value Entry</h3>
      </div>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item
            label="Value Date"
            prop="valueDate"
          >
            <el-date-picker
              v-model="form.valueDate"
              type="date"
              placeholder="Select date"
              format="MM/dd/yyyy"
              value-format="MM/dd/yyyy"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
            label="Value"
            prop="value"
          >
            <el-input
              v-model="form.value"
              placeholder="Enter value"
            >
              <template slot="append">
                <el-tooltip
                  content="Enter a numeric value. Values above 0 will trigger Yellow breach status, values of 5 or higher will trigger Red breach status."
                  placement="top"
                >
                  <i class="el-icon-question" />
                </el-tooltip>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- Comments Section -->
      <div class="section-header">
        <h3>Comments</h3>
      </div>
      <el-form-item>
        <el-input
          v-model="form.comments"
          type="textarea"
          :rows="4"
          placeholder="Add any comments or notes about this KRI value"
        />
      </el-form-item>

      <!-- Action Buttons -->
      <div class="form-actions">
        <el-button @click="$emit('cancel')">
          Cancel
        </el-button>
        <el-button
          v-if="!isReadOnly"
          type="primary"
          :loading="submitting"
          @click="submitValue"
        >
          Submit Value
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script>
import { submitKriValue } from '@/api/kri';

export default {
  name: 'KriValueEntry',
  
  props: {
    kriTask: {
      type: Object,
      required: true
    }
  },
  
  data() {
    return {
      form: {
        id: this.kriTask.id,
        name: this.kriTask.name,
        path: this.kriTask.path,
        collectionStatus: this.kriTask.collectionStatus,
        valueDate: this.kriTask.valueDate || '',
        value: this.kriTask.value || '',
        breachStatus: this.kriTask.breachStatus,
        comments: ''
      },
      rules: {
        valueDate: [
          { required: true, message: 'Please select a value date', trigger: 'change' }
        ],
        value: [
          { required: true, message: 'Please enter a value', trigger: 'blur' }
        ]
      },
      submitting: false
    };
  },
  
  computed: {
    isReadOnly() {
      return this.kriTask.collectionStatus !== 'Awaiting Collection';
    }
  },
  
  methods: {
    getStatusType(status) {
      const statusMap = {
        'Awaiting Collection': 'warning',
        'Awaiting Approval': 'info',
        'Collected': 'success'
      };
      return statusMap[status] || '';
    },
    
    getBreachStatusType(status) {
      const statusMap = {
        'Green': 'success',
        'Yellow': 'warning',
        'Red': 'danger'
      };
      return statusMap[status] || '';
    },
    
    async submitValue() {
      if (this.isReadOnly) return;
      
      try {
        await this.$refs.form.validate();
        
        this.submitting = true;
        
        // Prepare submission data
        const submissionData = {
          id: this.form.id,
          valueDate: this.form.valueDate,
          value: this.form.value,
          comments: this.form.comments
        };
        
        // Call the API to submit the KRI value
        const response = await submitKriValue(submissionData);
        
        // Use the response data if needed
        console.log('API response:', response);
        
        // Emit event with updated task data
        this.$emit('value-submitted', {
          ...this.kriTask,
          valueDate: this.form.valueDate,
          value: this.form.value,
          collectionStatus: 'Awaiting Approval',
          breachStatus: this.determineBreachStatus(this.form.value)
        });
      } catch (error) {
        console.error('Error submitting KRI value:', error);
        this.$message.error('Failed to submit KRI value');
      } finally {
        this.submitting = false;
      }
    },
    
    determineBreachStatus(value) {
      if (!value) return 'Not Determined';
      
      const numValue = parseFloat(value);
      if (numValue >= 5) return 'Red';
      if (numValue > 0) return 'Yellow';
      return 'Green';
    }
  }
};
</script>

<style lang="scss" scoped>
.kri-value-entry {
  .section-header {
    margin: 20px 0 15px;
    border-bottom: 1px solid #ebeef5;
    
    h3 {
      margin: 0 0 10px;
      font-size: 16px;
      font-weight: 500;
    }
  }
  
  .form-actions {
    margin-top: 30px;
    text-align: right;
  }
}
</style>
