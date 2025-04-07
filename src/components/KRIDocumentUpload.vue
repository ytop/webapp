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
    handleFileChange(file) {
      this.fileList = this.$refs.upload.uploadFiles;
    },

    handleFileRemove(file) {
      this.fileList = this.$refs.upload.uploadFiles;
    },

    beforeUpload(file) {
      // Check file type
      const acceptedTypes = ['application/pdf', 'application/msword',
                            'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
                            'application/vnd.ms-excel',
                            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
                            'image/jpeg', 'image/png'];
      const isAcceptedType = acceptedTypes.includes(file.type);

      // Check file size (10MB max)
      const isLessThan10MB = file.size / 1024 / 1024 < 10;

      if (!isAcceptedType) {
        this.$message.error('File type not supported');
        return false;
      }

      if (!isLessThan10MB) {
        this.$message.error('File size cannot exceed 10MB');
        return false;
      }

      return true;
    },

    async uploadFiles() {
      if (this.fileList.length === 0) {
        return [];
      }

      this.uploading = true;
      const uploadPromises = [];

      try {
        for (const file of this.fileList) {
          if (file.status !== 'success') {
            const uploadPromise = uploadKRIDocument(this.kriId, file.raw)
              .then(response => {
                file.status = 'success';
                return response.document;
              })
              .catch(error => {
                file.status = 'error';
                this.$message.error(`Failed to upload ${file.name}`);
                console.error('Upload error:', error);
                return null;
              });

            uploadPromises.push(uploadPromise);
          }
        }

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
        // In a real app, you would call an API to delete the document
        // For now, we'll just remove it from the local array
        const index = this.documents.findIndex(doc => doc.id === document.id);
        if (index !== -1) {
          this.documents.splice(index, 1);
          this.$message.success('Document deleted successfully');
          this.$emit('document-deleted', document);
        }
      }).catch(() => {
        // User cancelled the deletion
      });
    },

    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString();
    },

    getUploadedDocuments() {
      return this.documents;
    }
  }
};
</script>

<style lang="scss" scoped>
.document-upload {
  margin-bottom: 20px;

  .upload-section {
    background-color: #f8f9fa;
    border-radius: 4px;
    padding: 15px;
    margin-bottom: 25px;
    border: 1px solid #ebeef5;

    .upload-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;

      h4 {
        margin: 0;
        color: #303133;
      }
    }

    .upload-container {
      background-color: white;
      border-radius: 4px;
      padding: 10px;
      border: 1px dashed #d9d9d9;

      &:hover {
        border-color: #409EFF;
      }
    }
  }

  .document-list {
    margin-top: 25px;

    h4 {
      margin-bottom: 15px;
      display: flex;
      align-items: center;

      .el-tag {
        margin-left: 10px;
      }
    }

    .el-table {
      margin-top: 15px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    }
  }

  .delete-btn {
    color: #f56c6c;
  }
}
</style>
