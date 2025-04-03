<template>
  <div id="app">
    <el-container>
      <el-header height="60px">
        <div class="header-container">
          <div class="logo">
            <h2>KRI Management System</h2>
          </div>
          <el-menu
            :default-active="activeIndex"
            class="nav-menu"
            mode="horizontal"
            router
            background-color="#545c64"
            text-color="#fff"
            active-text-color="#ffd04b"
          >
            <el-menu-item index="/management">Dashboard</el-menu-item>
            <el-menu-item index="/inventory">KRI Inventory</el-menu-item>
            <el-menu-item index="/data-collection">Data Collection</el-menu-item>
            <el-menu-item index="/reporting">Reporting</el-menu-item>
          </el-menu>
          <div class="user-info">
            <span>{{ currentUser }}</span>
            <el-dropdown trigger="click">
              <span class="el-dropdown-link">
                <i class="el-icon-user"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item>Profile</el-dropdown-item>
                <el-dropdown-item>Settings</el-dropdown-item>
                <el-dropdown-item divided>Logout</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>

      <el-footer height="40px">
        <div class="footer-content">
          <span>© {{ currentYear }} Banking KRI Management System</span>
          <span>Version 1.0.0</span>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      currentUser: 'Admin User',
      currentYear: new Date().getFullYear()
    }
  },
  computed: {
    activeIndex() {
      return this.$route.path
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Reset some element-ui styles */
body {
  margin: 0;
  padding: 0;
}

.el-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.el-header {
  background-color: #545c64;
  color: #fff;
  padding: 0;
}

.header-container {
  display: flex;
  align-items: center;
  height: 100%;
}

.logo {
  margin-right: 20px;
  padding-left: 20px;
  width: 220px;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
}

.user-info {
  display: flex;
  align-items: center;
  padding-right: 20px;
}

.user-info span {
  margin-right: 10px;
}

.el-dropdown-link {
  cursor: pointer;
  color: #fff;
}

.el-main {
  flex: 1;
  padding: 0;
  overflow-x: hidden;
  background-color: #f0f2f5;
}

.el-footer {
  background-color: #545c64;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  width: 100%;
  padding: 0 20px;
  font-size: 12px;
}
</style>
