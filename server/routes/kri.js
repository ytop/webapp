const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const { nanoid } = require('nanoid');
const {
  getKRIs,
  getKRIById,
  updateKRI,
  addDocument,
  getDocumentsByKRIId,
  getKRITasks,
  getKRITaskById,
  updateKRITask
} = require('../db');

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
  }
});

const upload = multer({ storage });

// Get all KRIs
router.get('/all', (req, res) => {
  const kris = getKRIs();
  res.json(kris);
});

// Get all KRI tasks - new endpoint
router.get('/alltasks', (req, res) => {
  console.log('GET /kri/alltasks endpoint called');
  const tasks = getKRITasks();
  console.log(`Returning ${tasks ? tasks.length : 0} KRI tasks`);
  res.json(tasks || []);
});

// Get a specific KRI by ID
router.get('/:kriId', (req, res) => {
  const { kriId } = req.params;
  const kri = getKRIById(kriId);

  if (!kri) {
    return res.status(404).json({
      success: false,
      message: `KRI with ID ${kriId} not found`
    });
  }

  res.json(kri);
});

// Update KRI description
router.post('/updateDescription', (req, res) => {
  const { kriId, newDescription } = req.query;
  const kri = getKRIById(kriId);

  if (!kri) {
    return res.status(404).json({
      success: false,
      message: `KRI with ID ${kriId} not found`
    });
  }

  const updatedKRI = updateKRI(kriId, { kriDesc: newDescription });

  res.json({
    success: true,
    message: 'Description updated successfully',
    updatedKRI: {
      id: kriId,
      description: newDescription
    }
  });
});

// Update a KRI
router.post('/update', (req, res) => {
  const { kriId } = req.query;
  const kriData = req.body;

  const kri = getKRIById(kriId);

  if (!kri) {
    return res.status(404).json({
      success: false,
      message: `KRI with ID ${kriId} not found`
    });
  }

  const updatedKRI = updateKRI(kriId, kriData);

  res.json({
    success: true,
    message: 'KRI updated successfully',
    updatedKRI
  });
});

// Upload a document for a KRI
router.post('/uploadDocument', upload.single('file'), (req, res) => {
  const { kriId } = req.query;
  const file = req.file;

  if (!file) {
    return res.status(400).json({
      success: false,
      message: 'No file uploaded'
    });
  }

  const kri = getKRIById(kriId);

  if (!kri) {
    return res.status(404).json({
      success: false,
      message: `KRI with ID ${kriId} not found`
    });
  }

  const document = {
    name: file.originalname,
    size: file.size,
    type: file.mimetype,
    url: `/uploads/${file.filename}`
  };

  const savedDocument = addDocument(kriId, document);

  res.json({
    success: true,
    message: 'Document uploaded successfully',
    document: savedDocument
  });
});

// Get documents for a KRI
router.get('/:kriId/documents', (req, res) => {
  const { kriId } = req.params;
  const documents = getDocumentsByKRIId(kriId);

  res.json(documents);
});

// ===== KRI Task Routes =====

// Get all KRI tasks (deprecated, redirects to /alltasks)
router.get('/tasks', (req, res) => {
  console.log('GET /kri/tasks endpoint called (deprecated)');
  res.redirect('/kri/alltasks');
});

// Get a specific KRI task by ID
router.get('/tasks/:id', (req, res) => {
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
router.post('/tasks/submitValue', (req, res) => {
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
