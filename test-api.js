/**
 * Simple script to test the API connection
 * Run with: node test-api.js
 */

const axios = require('axios');

// Configuration
const SERVER_PORT = 8888;
const CLIENT_PORT = 9527;
const API_BASE_PATH = '/rdaapi';

// Test endpoints
const endpoints = [
  { name: 'Health Check', url: `http://localhost:${SERVER_PORT}/health` },
  { name: 'KRI All (Direct)', url: `http://localhost:${SERVER_PORT}/kri/all` },
  { name: 'KRI Tasks (Direct)', url: `http://localhost:${SERVER_PORT}/kri/alltasks` },
  { name: 'KRI All (Via rdaapi)', url: `http://localhost:${SERVER_PORT}/rdaapi/kri/all` },
  { name: 'KRI Tasks (Via rdaapi)', url: `http://localhost:${SERVER_PORT}/rdaapi/kri/alltasks` },
  { name: 'KRI All (Client Proxy)', url: `http://localhost:${CLIENT_PORT}${API_BASE_PATH}/kri/all` },
  { name: 'KRI Tasks (Client Proxy)', url: `http://localhost:${CLIENT_PORT}${API_BASE_PATH}/kri/alltasks` }
];

// Test each endpoint
async function testEndpoints() {
  console.log('Testing API endpoints...\n');
  
  for (const endpoint of endpoints) {
    try {
      console.log(`Testing ${endpoint.name}: ${endpoint.url}`);
      const response = await axios.get(endpoint.url, { timeout: 5000 });
      console.log(`  Status: ${response.status}`);
      console.log(`  Data: ${typeof response.data === 'object' ? JSON.stringify(response.data).substring(0, 100) + '...' : response.data}`);
      console.log('  Result: SUCCESS\n');
    } catch (error) {
      console.log(`  Error: ${error.message}`);
      if (error.response) {
        console.log(`  Status: ${error.response.status}`);
        console.log(`  Data: ${JSON.stringify(error.response.data)}`);
      }
      console.log('  Result: FAILED\n');
    }
  }
  
  console.log('API testing completed.');
}

// Run the tests
testEndpoints().catch(error => {
  console.error('Error running tests:', error);
});
