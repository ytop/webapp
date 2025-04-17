import axios from 'axios'

// Create an Axios instance with custom configuration
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API, // URL prefix
  timeout: 30000 // Request timeout in milliseconds
})

// Request interceptor
service.interceptors.request.use(
  config => {
    // Log request details in development
    if (process.env.NODE_ENV === 'development') {
      console.log('API Request:', config.method.toUpperCase(), config.url)
    }
    return config
  },
  error => {
    // Log request errors
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  response => {
    return response
  },
  error => {
    // Log response errors
    console.error('Response error:', error)
    return Promise.reject(error)
  }
)

export default service
