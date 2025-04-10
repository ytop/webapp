const low = require('lowdb');
const FileSync = require('lowdb/adapters/FileSync');
const Memory = require('lowdb/adapters/Memory');
const { nanoid } = require('nanoid');
const path = require('path');

// Use in-memory adapter for development
const adapter = new Memory();
const db = low(adapter);

// Import mock data from the original API files
const { mockKRIs } = require('./data/kris');
const { mockKriTasks } = require('./data/kriTasks');

// Set default data
db.defaults({
  kris: mockKRIs,
  kriTasks: mockKriTasks,
  documents: []
}).write();

// Helper functions for working with the database
const getKRIs = () => db.get('kris').value();
const getKRIById = (id) => db.get('kris').find({ kriId: id }).value();
const updateKRI = (id, data) => {
  return db.get('kris')
    .find({ kriId: id })
    .assign(data)
    .write();
};

const getKRITasks = () => db.get('kriTasks').value();
const getKRITaskById = (id) => db.get('kriTasks').find({ id }).value();
const updateKRITask = (id, data) => {
  return db.get('kriTasks')
    .find({ id })
    .assign(data)
    .write();
};

const addDocument = (kriId, document) => {
  const docId = nanoid(10);
  const newDoc = {
    id: docId,
    kriId,
    ...document,
    uploadDate: new Date().toISOString()
  };
  
  db.get('documents')
    .push(newDoc)
    .write();
    
  // Also update the KRI's documents array
  const kri = getKRIById(kriId);
  if (kri) {
    if (!kri.documents) kri.documents = [];
    kri.documents.push(newDoc);
    updateKRI(kriId, { documents: kri.documents });
  }
  
  return newDoc;
};

const getDocumentsByKRIId = (kriId) => {
  return db.get('documents')
    .filter({ kriId })
    .value();
};

module.exports = {
  db,
  getKRIs,
  getKRIById,
  updateKRI,
  getKRITasks,
  getKRITaskById,
  updateKRITask,
  addDocument,
  getDocumentsByKRIId
};
