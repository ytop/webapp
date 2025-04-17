import request from './request'

/**
 * Test the API connection
 * This function can be called from the browser console to test the API connection
 */
export const testApiConnection = async () => {
  console.log('Testing API connection...')
  
  try {
    // Test the health endpoint
    console.log('Testing health endpoint...')
    const healthResponse = await request.get('/health')
    console.log('Health endpoint response:', healthResponse.data)
    
    // Test the KRI all endpoint
    console.log('Testing KRI all endpoint...')
    const kriResponse = await request.get('/kri/all')
    console.log('KRI all endpoint response:', kriResponse.data)
    
    // Test the KRI tasks endpoint
    console.log('Testing KRI tasks endpoint...')
    const tasksResponse = await request.get('/kri/alltasks')
    console.log('KRI tasks endpoint response:', tasksResponse.data)
    
    console.log('API connection tests completed successfully!')
    return {
      success: true,
      health: healthResponse.data,
      kris: kriResponse.data,
      tasks: tasksResponse.data
    }
  } catch (error) {
    console.error('API connection test failed:', error)
    return {
      success: false,
      error: error.message,
      config: error.config
    }
  }
}

// Make the test function available in the browser console
if (typeof window !== 'undefined') {
  window.testApiConnection = testApiConnection
}

export default {
  testApiConnection
}
