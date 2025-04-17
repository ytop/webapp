import Vue from 'vue'
import Router from 'vue-router'
import KRIManagement from './views/KRIManagement.vue'

Vue.use(Router)

// Create router instance outside export for better performance
const router = new Router({
  mode: 'history',
  base: process.env.BASE_URL || '/', // Use BASE_URL with fallback
  routes: [
    {
      path: '/',
      name: 'KRIManagement',
      component: KRIManagement,
      // Add meta for potential future use
      meta: {
        title: 'KRI Management'
      }
    }
  ]
})

// Add navigation guard for better error handling
router.onError((error) => {
  console.error('Router error:', error)
})

export default router
