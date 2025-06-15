import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

// Initialize Vue with optimizations
Vue.use(ElementUI, { size: 'medium' }) // Set default component size
Vue.config.productionTip = false
Vue.config.silent = true // Suppress console warnings in production

// Create Vue instance with async setup
const app = new Vue({
  el: '#app',
  router,
  render: h => h(App)
})


// Export app instance for potential external usage
export default app
