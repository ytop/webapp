// Default application settings configuration
const settings = {
  title: 'RiskSphere',

  // UI Configuration
  showSettings: false, // Settings panel visibility
  tagsView: true, // Enable tags view navigation
  fixedHeader: true, // Fix header position
  sidebarLogo: true, // Show logo in sidebar

  // Search Configuration
  supportPinyinSearch: true, // Enable pinyin search in header

  // Error Logging
  errorLog: process.env.NODE_ENV === 'production' ? 'production' : ['production', 'development'],

  // Environment Configuration
  envUrl: process.env.VUE_APP_BASE_API,
  envName: process.env.VUE_APP_ENVNAME
}

module.exports = settings
