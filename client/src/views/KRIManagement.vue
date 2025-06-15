<template>
  <div
    id="app"
    style="padding: 20px;"
  >
    <!-- API Status Component removed -->

    <el-tabs v-model="activeTab">
      <el-tab-pane
        label="Collection"
        name="task"
      >
        <task v-if="activeTab === 'task'" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
// Import child components from views subdirectories

import Task from './task/Task.vue';


export default {
  name: 'KRIManagement',
  components: {
    Task
  },
  data() {
    return {
      // Set the initially active tab
      activeTab: 'task'
    };
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
