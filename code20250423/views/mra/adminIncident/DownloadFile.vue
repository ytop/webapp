<template>
  <el-card class="download-file">
    <!-- Path navigation -->
    <el-breadcrumb separator="/">
      <el-breadcrumb-item
        v-for="(path, index) in breadcrumbs"
        :key="index"
        @click.native="navigateToBreadcrumb(index)"
      >
        {{ path }}
      </el-breadcrumb-item>
    </el-breadcrumb>

    <!-- Main content -->
    <div v-loading="loading" class="content-wrapper">
      <el-table
        :data="items"
        style="width: 100%"
        @row-dblclick="handleDoubleClick"
      >
        <!-- Name column -->
        <el-table-column label="Name" min-width="300">
          <template slot-scope="scope">
            <div class="item-name">
              <i :class="getIcon(scope.row)" class="item-icon" />
              {{ scope.row.name }}
            </div>
          </template>
        </el-table-column>

        <!-- Size column -->
        <el-table-column label="Size" width="120" align="right">
          <template slot-scope="scope">
            {{ scope.row.type === 'file' ? formatSize(scope.row.size) : '-' }}
          </template>
        </el-table-column>

        <!-- Modified date column -->
        <el-table-column label="Modified" width="180">
          <template slot-scope="scope">
            {{ formatDate(scope.row.modifiedDate) }}
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-card>
</template>

<script>
import { listDownloadFiles } from '@/api/mra'

export default {
  name: 'FileExplorer',
  data() {
    return {
      currentPath: '',
      items: [],
      breadcrumbs: [],
      loading: false
    }
  },

  created() {
    this.loadDirectory('')
  },

  methods: {
    async loadDirectory(path) {
      this.loading = true
      try {
        const response = await listDownloadFiles(path)
        console.log('Response:', JSON.stringify(response, null, 2))
        if (response.code === 200) {
          this.items = response.data.sort((a, b) => b.name.localeCompare(a.name))
          this.currentPath = path
          this.updateBreadcrumbs()
        } else {
          throw new Error('Invalid response code')
        }
      } catch (error) {
        this.$message.error('Failed to load directory')
        console.error('Failed to load directory:', error)
      } finally {
        this.loading = false
      }
    },

    handleDoubleClick(row) {
      if (row.type === 'directory') {
        this.loadDirectory(row.path)
      }
    },

    updateBreadcrumbs() {
      this.breadcrumbs = ['root']
      if (this.currentPath) {
        this.breadcrumbs.push(...this.currentPath.split('/').filter(Boolean))
      }
    },

    navigateToBreadcrumb(index) {
      if (index === 0) {
        this.loadDirectory('')
      } else {
        const path = this.breadcrumbs
          .slice(1, index + 1)
          .join('/')
        this.loadDirectory(path)
      }
    },

    getIcon(item) {
      if (item.type === 'directory') {
        return 'el-icon-folder'
      }

      const extension = item.name.split('.').pop().toLowerCase()
      const iconMap = {
        pdf: 'el-icon-document',
        doc: 'el-icon-document',
        docx: 'el-icon-document',
        xls: 'el-icon-document',
        xlsx: 'el-icon-document',
        txt: 'el-icon-document-text',
        jpg: 'el-icon-picture',
        jpeg: 'el-icon-picture',
        png: 'el-icon-picture',
        gif: 'el-icon-picture'
      }
      return iconMap[extension] || 'el-icon-document'
    },

    formatSize(bytes) {
      if (!bytes) return '0 B'
      const units = ['B', 'KB', 'MB', 'GB', 'TB']
      let i = 0
      while (bytes >= 1024 && i < units.length - 1) {
        bytes /= 1024
        i++
      }
      return `${bytes.toFixed(1)} ${units[i]}`
    },

    formatDate(timestamp) {
      if (!timestamp) return '-'
      return new Date(timestamp).toLocaleString()
    }
  }
}
</script>

<style>
.download-file {
  margin: 20px;
}

.content-wrapper {
  margin-top: 20px;
  min-height: 400px;
}

.item-name {
  display: flex;
  align-items: center;
}

.item-icon {
  margin-right: 8px;
  font-size: 18px;
}

.el-breadcrumb__item:hover {
  cursor: pointer;
  color: #409EFF;
}
</style>
