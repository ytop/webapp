<template>
  <div class="kri-management-container">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="KRI List" name="kriList">
        <el-table :data="kriList" border style="width: 100%">
          <el-table-column prop="kriId" label="KRI ID" width="180" />
          <el-table-column prop="kriDesc" label="KRI Description" />
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="KRI Description Update" name="kriUpdate">
        <el-form label-width="120px">
          <el-form-item label="KRI ID">
            <el-select v-model="selectedKriId" placeholder="Select KRI ID" @change="handleKriIdChange">
              <el-option v-for="item in kriIdOptions" :key="item.kriId" :label="item.kriId" :value="item.kriId" />
            </el-select>
          </el-form-item>
          <el-form-item label="KRI Description">
            <el-input v-model="kriDesc" type="textarea" :rows="4" :disabled="!isKriIdSelected" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :disabled="!isKriIdSelected" @click="handleUpdateDescription">Update Description
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { getKRIs, updateKRIDescription } from '@/api/kri' // Import API functions

export default {
  name: 'KriManagement',
  data() {
    return {
      activeTab: 'kriList',
      kriList: [],
      kriIdOptions: [],
      selectedKriId: null,
      kriDesc: '',
      isKriIdSelected: false
    }
  },
  created() {
    this.fetchKRIs()
  },
  methods: {
    async fetchKRIs() {
      try {
        const response = await getKRIs()
        console.log('Setting kriList:', response)
        this.kriList = response
        console.log('kriList after setting:', this.kriList)
        this.kriIdOptions = response // Populate the dropdown options
      } catch (error) {
        this.$message.error('Failed to fetch KRI data.')
        console.error(error)
      }
    },
    handleTabClick(tab) {
      if (tab.name === 'kriUpdate') {
        this.selectedKriId = null
        this.kriDesc = ''
        this.isKriIdSelected = false
      }
    },
    handleKriIdChange(value) {
      const selectedKri = this.kriList.find(kri => kri.kriId === value)
      if (selectedKri) {
        this.kriDesc = selectedKri.kriDesc
        this.isKriIdSelected = true
      } else {
        this.kriDesc = ''
        this.isKriIdSelected = false
      }
    },
    async handleUpdateDescription() {
      if (!this.selectedKriId) {
        this.$message.warning('Please select a KRI ID.')
        return
      }

      try {
        await updateKRIDescription(this.selectedKriId, this.kriDesc)
        this.$message.success('KRI Description updated successfully!')
        // Refresh KRI List to reflect changes
        this.fetchKRIs()
        this.activeTab = 'kriList'
      } catch (error) {
        this.$message.error('Failed to update KRI Description.')
        console.error(error)
      }
    }
  }
}
</script>

<style scoped>
.kri-management-container {
  padding: 20px;
}
</style>
