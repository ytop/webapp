import Vue from 'vue'
import Router from 'vue-router'
import KRIManagement from './components/KRIManagement.vue'

// Lazy load components for better performance
const KRIInventory = () => import('./components/KRIInventory.vue')
const KRIDataCollection = () => import('./components/KRIDataCollection.vue')
const KRIReporting = () => import('./components/KRIReporting.vue')

Vue.use(Router)

// Create router instance outside export for better performance
const router = new Router({
  mode: 'history',
  base: process.env.VUE_APP_PROXY,
  routes: [
    {
      path: '/',
      redirect: '/management'
    },
    {
      path: '/management',
      name: 'KRIManagement',
      component: KRIManagement,
      meta: {
        title: 'KRI Management'
      }
    },
    {
      path: '/inventory',
      name: 'KRIInventory',
      component: KRIInventory,
      meta: {
        title: 'KRI Inventory'
      }
    },
    {
      path: '/data-collection',
      name: 'KRIDataCollection',
      component: KRIDataCollection,
      meta: {
        title: 'KRI Data Collection'
      }
    },
    {
      path: '/reporting',
      name: 'KRIReporting',
      component: KRIReporting,
      meta: {
        title: 'KRI Reporting'
      }
    }
  ]
})

// Add navigation guard for better error handling
router.onError((error) => {
  console.error('Router error:', error)
})

// Add title update on route change
router.afterEach((to) => {
  if (to.meta && to.meta.title) {
    document.title = `${to.meta.title} - KRI Management System`
  } else {
    document.title = 'KRI Management System'
  }
})

export default router
