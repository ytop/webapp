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

// Route to get all KRIs
router.get('/all', (req, res) => {
  console.log("GET /kri/all endpoint called")
  const kris = kriService.getKRIs();
  res.json(kris);
});

// Route to get all KRI tasks
router.get('/alltasks', (req, res) => {
  console.log('GET /kri/alltasks endpoint called');
  const tasks = db.getKRITasks();
  console.log(`Returning ${tasks ? tasks.length : 0} KRI tasks`); // Use kriService for database interactions
  res.json(tasks || []);
})

/**
 * @route   GET /kri/:kriId
 * @desc    Get a specific KRI by ID
 */
router.get('/:kriId', (req, res) => {
  const { kriId } = req.params;
  const kri = getKRIById(kriId);

  if (!kri) {
    return res.status(404).json({
      success: false,
      message: `KRI with ID ${kriId} not found`,
    });
  }

  res.json(kri);
})

/**
 * @route   POST /kri/updateDescription
 * @desc    Update the description of a KRI
 */
router.post('/updateDescription', (req, res, next) => {
  const { kriId, newDescription } = req.query;
  const kri = getKRIById(kriId);

  if (!kri) {
    return res.status(404).json({
      success: false,
      message: `KRI with ID ${kriId} not found`,
    });
  }

  const updatedKRI = kriService.updateKRI(kriId, { kriDesc: newDescription });

    res.json({
        success: true,
        message: 'Description updated successfully',
        updatedKRI: {
            id: kriId,
            description: newDescription,
        },
    });
})
/**
 * @route   POST /kri/update
 * @desc    Update a KRI's data
 */
router.post('/update', (req, res) => {
  const { kriId } = req.query;
  const kriData = req.body;

  const kri = getKRIById(kriId);

  if (!kri) {
    return res.status(404).json({
      success: false,
      message: `KRI with ID ${kriId} not found`,
    });
  }

  const updatedKRI = kriService.updateKRI(kriId, kriData);

    res.json({
        success: true,
        message: 'KRI updated successfully',
        updatedKRI,
    });
})

/**
 * @route   POST /kri/uploadDocument
 * @desc    Upload a document for a specific KRI
 */
router.post('/uploadDocument', upload.single('file'), (req, res, next) => {
  const { kriId } = req.query;
  const file = req.file;

  if (!file) {
    return res.status(400).json({
      success: false,
      message: 'No file uploaded.'
    });
  }

  // Check if the KRI exists
  const kri = kriService.getKRIById(kriId);
  if (!kri) {
    return res.status(404).json({
      success: false,
      message: `KRI with ID ${kriId} not found.`
    });
  }

  // Construct the document object
  try {
    const document = {
      name: file.originalname,
      size: file.size,
      type: file.mimetype,
      url: `/uploads/${file.filename}`
    };

    // Save the document
    const savedDocument = kriService.addDocument(kriId, document);
    res.status(200).json({
      success: true,
      message: 'Document uploaded successfully.',
      document: savedDocument
    });
  } catch (error) {
    // Pass any errors to the error handling middleware
    next(error);
  }
});

/**
 * @route   GET /kri/:kriId/documents
 * @desc    Get all documents for a specific KRI
 */
router.get('/:kriId/documents', (req, res) => {
  const { kriId } = req.params;
  const documents = kriService.getDocumentsByKRIId(kriId);

  res.json(documents);
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
