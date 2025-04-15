const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const path = require('path');
const app = require('./app');
// Import routes
const kriRouter = require('./controllers/kriController');
const PORT = process.env.PORT || 8889;


// Middleware
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Set up uploads directory for document storage
const uploadsDir = path.join(__dirname, 'uploads');
app.use('/uploads', express.static(uploadsDir));

// Routes
app.use("/api/kri",kriRouter)

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date() });
});

// Test endpoint for KRI tasks
app.get('/test-alltasks', (req, res) => {
  const { getKRITasks } = require('./db');
  const tasks = getKRITasks();
  console.log(`Test endpoint: Found ${tasks ? tasks.length : 0} KRI tasks`);
  res.json({
    count: tasks ? tasks.length : 0,
    tasks: tasks || []
  });
});

// For local development
if (process.env.NODE_ENV !== 'production') {
  app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
    console.log(`API available at http://localhost:${PORT}`);
  });
}

// Export the Express API for Vercel
module.exports = app;
