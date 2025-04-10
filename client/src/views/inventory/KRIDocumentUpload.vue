<template>
  <div class="document-upload">
    <div class="upload-section">
      <div class="upload-header">
        <h4>Document Management</h4>
        <el-button
          type="primary"
          size="small"
          icon="el-icon-upload"
          :loading="uploading"
          :disabled="fileList.length === 0"
          @click="uploadFiles"
        >
          Upload Selected Files
        </el-button>
      </div>

      <div class="upload-container">
        <el-upload
          ref="upload"
          :action="uploadAction"
          :auto-upload="false"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="beforeUpload"
          :limit="5"
          :file-list="fileList"
          drag
        >
          <i class="el-icon-upload" />
          <div class="el-upload__text">
            Drop file here or <em>click to upload</em>
          </div>
          <div
            slot="tip"
            class="el-upload__tip"
          >
            Accepted file types: PDF, DOC, DOCX, XLS, XLSX, JPG, PNG (max 10MB)
          </div>
        </el-upload>
      </div>
    </div>

    <div class="document-list">
      <h4>
        Uploaded Documents <el-tag
          size="small"
          type="info"
        >
          {{ documents.length }} document(s)
        </el-tag>
      </h4>
      <el-empty
        v-if="documents.length === 0"
        description="No documents uploaded yet"
      />
      <el-table
        v-else
        :data="documents"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column
          prop="name"
          label="Document Name"
        />
        <el-table-column
          prop="uploadDate"
          label="Upload Date"
          width="180"
        >
          <template slot-scope="scope">
            {{ formatDate(scope.row.uploadDate) }}
          </template>
        </el-table-column>
        <el-table-column
          label="Actions"
          width="120"
          align="center"
        >
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="viewDocument(scope.row)"
            >
              View
            </el-button>
            <el-button
              type="text"
              size="small"
              class="delete-btn"
              @click="deleteDocument(scope.row)"
            >
              Delete
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import { uploadKRIDocument } from '@/api/kri';

export default {
  name: 'KRIDocumentUpload',

  props: {
    kriId: {
      type: String,
      required: true
    },
    existingDocuments: {
      type: Array,
      default: () => []
    }
  },

  data() {
    return {
      fileList: [],
      documents: [],
      uploadAction: '#', // Placeholder, we'll handle upload manually
      uploading: false
    };
  },

  watch: {
    existingDocuments: {
      immediate: true,
      handler(newDocs) {
        if (newDocs && newDocs.length) {
          this.documents = [...newDocs];
        }
      }
    }
  },

  methods: {
    handleFileChange(file, fileList) {
      this.fileList = fileList;
    },

    handleFileRemove(file, fileList) {
      this.fileList = fileList;
    },

    beforeUpload(file) {
      const isValidType = [
        'application/pdf',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'application/vnd.ms-excel',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'image/jpeg',
        'image/png'
      ].includes(file.type);

      const isLt10M = file.size / 1024 / 1024 < 10;

      if (!isValidType) {
        this.$message.error('Invalid file type. Please upload PDF, DOC, DOCX, XLS, XLSX, JPG or PNG files.');
        return false;
      }

      if (!isLt10M) {
        this.$message.error('File size cannot exceed 10MB!');
        return false;
      }

      return true;
    },

    formatDate(dateString) {
      if (!dateString) return '';
      
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    },

    async uploadFiles() {
      if (this.fileList.length === 0) return [];
      
      this.uploading = true;
      
      try {
        const uploadPromises = this.fileList.map(async file => {
          try {
            const response = await uploadKRIDocument(this.kriId, file.raw);
            return response.document;
          } catch (error) {
            console.error(`Error uploading file ${file.name}:`, error);
            this.$message.error(`Failed to upload ${file.name}`);
            return null;
          }
        });
        
        const uploadedDocs = await Promise.all(uploadPromises);
        const validDocs = uploadedDocs.filter(doc => doc !== null);

        // Add newly uploaded documents to the list
        this.documents = [...this.documents, ...validDocs];

        // Clear the upload list after successful upload
        this.fileList = [];
        this.$refs.upload.clearFiles();

        return validDocs;
      } finally {
        this.uploading = false;
      }
    },

    handleUploadSuccess(response, file, fileList) {
      this.$message.success(`${file.name} uploaded successfully`);
    },

    handleUploadError(err, file, fileList) {
      this.$message.error(`${file.name} upload failed`);
    },

    viewDocument(document) {
      if (document.url) {
        window.open(document.url, '_blank');
      } else {
        this.$message.warning('Document URL not available');
      }
    },

    deleteDocument(document) {
      this.$confirm('Are you sure you want to delete this document?', 'Warning', {
        confirmButtonText: 'Yes',
        cancelButtonText: 'No',
        type: 'warning'
      }).then(() => {
        // In a real application, you would call an API to delete the document
        // For now, we'll just remove it from the local list
        const index = this.documents.findIndex(doc => doc.id === document.id);
        if (index !== -1) {
          this.documents.splice(index, 1);
          this.$emit('document-deleted', document);
          this.$message.success('Document deleted successfully');
        }
      }).catch(() => {
        // User cancelled the deletion
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.document-upload {
  .upload-section {
    margin-bottom: 20px;
    
    .upload-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
      
      h4 {
        margin: 0;
      }
    }
    
    .upload-container {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      padding: 10px;
      background-color: #fafafa;
    }
  }
  
  .document-list {
    h4 {
      margin-top: 0;
      margin-bottom: 15px;
    }
    
    .delete-btn {
      color: #f56c6c;
    }
  }
}
</style>
