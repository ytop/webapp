const express = require('express');
const router = express.Router();
const {
  getKRITasks,
  getKRITaskById,
  updateKRITask
} = require('../db');

// Get all KRI tasks
router.get('/', (req, res) => {
  const tasks = getKRITasks();
  res.json(tasks);
});

// Get a specific KRI task by ID
router.get('/:id', (req, res) => {
  const { id } = req.params;
  const task = getKRITaskById(parseInt(id));
  
  if (!task) {
    return res.status(404).json({ 
      success: false, 
      message: `KRI task with ID ${id} not found` 
    });
  }
  
  res.json(task);
});

// Submit a KRI value
router.post('/submitValue', (req, res) => {
  const kriValueData = req.body;
  const { id } = kriValueData;
  
  if (!id) {
    return res.status(400).json({ 
      success: false, 
      message: 'Task ID is required' 
    });
  }
  
  const task = getKRITaskById(parseInt(id));
  
  if (!task) {
    return res.status(404).json({ 
      success: false, 
      message: `KRI task with ID ${id} not found` 
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
    updatedTask
  });
});

module.exports = router;
