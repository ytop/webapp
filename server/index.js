console.log("index.js loaded")
const app = require('./app');
const PORT = process.env.PORT || 8888;

// For local development
if (process.env.NODE_ENV !== 'production') {
  app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
    console.log(`API available at http://localhost:${PORT}`);
  });
}
