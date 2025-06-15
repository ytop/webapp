console.log("kriController.js loaded")
const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const { nanoid } = require('nanoid');
const kriService = require('../services/kriService');

// Define constants for breach statuses and workflow status
const BREACH_STATUS_RED = 'Red';
const BREACH_STATUS_YELLOW = 'Yellow';
const BREACH_STATUS_GREEN = 'Green';
const AWAITING_APPROVAL = 'Awaiting Approval';

// Define threshold values
const redThreshold = 5;
const yellowThreshold = 0;

// Configure multer for file uploads
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    const uploadDir = path.join(__dirname, '../uploads');
    // Create directory if it doesn't exist
    if (!fs.existsSync(uploadDir)) {
      fs.mkdirSync(uploadDir, { recursive: true });
    }
    cb(null, uploadDir);
  },
  filename: function (req, file, cb) {
    const uniqueId = nanoid(8);
    cb(null, `${uniqueId}-${file.originalname}`);
  },
});

const upload = multer({ storage });



// Route to get all KRI tasks
router.get('/alltasks', (req, res) => {
  console.log('GET /kri/alltasks endpoint called');
  const tasks = kriService.getKRITasks();
  console.log(`Returning ${tasks ? tasks.length : 0} KRI tasks`); // Use kriService for database interactions
  res.json(tasks || []);
})

// ===== KRI Task Routes =====
// Route to get all KRI tasks (deprecated, redirects to /alltasks)
router.get('/tasks', (req, res) => {
  console.log('GET /kri/tasks endpoint called (deprecated)');
    res.redirect('/kri/alltasks');
});

// Get a specific KRI task by ID
router.get('/tasks/:id', (req, res) => {
  const { id } = req.params;
  const task = kriService.getKRITaskById(parseInt(id));

  if (!task) {
    return res.status(404).json({
        success: false,
        message: `KRI task with ID ${id} not found`,
    });
    }

    res.json(task);
})

// Submit a KRI value
router.post('/tasks/submitValue', (req, res) => {
  const kriValueData = req.body;
  const { id } = kriValueData;

  if (!id) {
    return res.status(400).json({
      success: false,
      message: 'Task ID is required',
    });
  }

  const task = kriService.getKRITaskById(parseInt(id));

  if (!task) {
      return res.status(404).json({
          success: false,
          message: `KRI task with ID ${id} not found`,
      });
  }

  // Determine breach status based on value and thresholds
  // This is a simplified example - in a real app, you'd compare against actual thresholds
  let breachStatus = 'Not Determined';
  if (kriValueData.value) {
    const value = parseFloat(kriValueData.value);
    if (value >= 5) {
      breachStatus = 'Red';
    } else if (value > 0) {
      breachStatus = 'Yellow';
    } else {
      breachStatus = 'Green';
    }
  }

  const updatedTask = updateKRITask(parseInt(id), {
    ...kriValueData,
    collectionStatus: 'Awaiting Approval',
    breachStatus
    });

    res.json({
        success: true,
        message: 'KRI value submitted successfully',
        updatedTask,
    });
})

module.exports = router;
