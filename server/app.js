// Import required modules
const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const path = require('path');

// Initialize Express app
const app = express();
const PORT = process.env.PORT || 8888;

// Enable CORS for cross-origin requests
app.use(cors());

// Parse incoming requests with JSON payloads
app.use(bodyParser.json());

// Parse incoming requests with URL-encoded payloads
app.use(bodyParser.urlencoded({ extended: true }));

// Set up uploads directory for document storage
const uploadsDir = path.join(__dirname, 'uploads');

// Serve static files from the 'uploads' directory under the '/uploads' route
app.use('/uploads', express.static(uploadsDir));

// Import and use KRI routes
const kriRoutes = require('./controllers/kriController');
app.use('/api/kri', kriRoutes); // Mount KRI routes under the '/api/kri' path

// Health check endpoint
app.get('/health', (req, res) => {
  // Respond with a JSON object indicating the server status and current timestamp
  res.json({ status: 'ok', timestamp: new Date() });
});

// Serve static files from the React app in production
if (process.env.NODE_ENV === 'production') {
  // Serve static files from the React build directory
  app.use(express.static(path.join(__dirname, '../dist')));
  
  // Handle all other requests by serving the React app's index.html
  app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, '../dist', 'index.html'));
  });
}

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack); // Log the error stack to the console for debugging

  // Set the response status code. Use the error's status code if available, otherwise default to 500 (Internal Server Error).
  res.status(err.status || 500);

  // Send a JSON response with an error message. Use the error's message if available, otherwise use a generic message.
  res.json({
    error: {
      message: err.message || 'An unexpected error occurred.'
    }
  });
});

// Start the server and listen for incoming requests
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
  console.log(`API available at http://localhost:${PORT}`);
});

// Export the app for use in other modules or for testing
module.exports = app;
