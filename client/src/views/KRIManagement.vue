<template>
  <div
    id="app"
    style="padding: 20px;"
  >
    <!-- API Status Component (only visible in development) -->
    <api-status v-if="isDevelopment" />

    <el-tabs v-model="activeTab">
      <el-tab-pane
        label="Dashboard"
        name="dashboard"
      >
        <dashboard v-if="activeTab === 'dashboard'" />
      </el-tab-pane>
      <el-tab-pane
        label="Collection"
        name="task"
      >
        <task v-if="activeTab === 'task'" />
      </el-tab-pane>
      <el-tab-pane
        label="Inventory"
        name="inventory"
      >
        <inventory v-if="activeTab === 'inventory'" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
// Import child components from views subdirectories
import Dashboard from './dashboard/Dashboard.vue';
import Task from './task/Task.vue';
import Inventory from './inventory/Inventory.vue';
import ApiStatus from '@/components/ApiStatus.vue';

export default {
  name: 'KRIManagement',
  components: {
    Dashboard,
    Task,
    Inventory,
    ApiStatus
  },
  data() {
    return {
      // Set the initially active tab
      activeTab: 'dashboard'
    };
  },
  computed: {
    // Check if we're in development mode
    isDevelopment() {
      return process.env.NODE_ENV === 'development';
    }
  }
}
</script>

<style>
/* Add basic styling if needed */
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}
.el-tabs__header {
  margin-bottom: 20px;
}
</style>
