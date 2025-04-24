// src/api/regulatory.js
import request from '@/utils/request' // Assuming you have an axios instance setup



/**
 * Get all table names
 * @returns {Promise}
 */
export function getAllTables() {
  return request({
    url: '/mra/admin/tables',
    method: 'get'
  })
}

/**
 * Get table metadata
 * @param {string} tableName - Name of the table
 * @returns {Promise}
 */
export function getTableMetadata(tableName) {
  return request({
    url: `/mra/admin/metadata/${tableName}`,
    method: 'get'
  })
}

/**
 * Get table data with pagination
 * @param {string} tableName - Name of the table
 * @param {Object} params - Query parameters
 * @param {number} [params.page=1] - Page number
 * @param {number} [params.size=10] - Page size
 * @param {string} [params.sort] - Sort field
 * @param {string} [params.order] - Sort order
 * @returns {Promise}
 */
export function getTableData(tableName, params = {}) {
  return request({
    url: `/mra/admin/table/${tableName}`,
    method: 'get',
    params: {
      page: params.page || 1,
      size: params.size || 10,
      sort: params.sort,
      order: params.order
    }
  })
}

/**
 * Create new record in table
 * @param {string} tableName - Name of the table
 * @param {Object} record - Record data
 * @returns {Promise}
 */
export function createTableRecord(tableName, record) {
  return request({
    url: `/mra/admin/table/${tableName}`,
    method: 'post',
    data: record
  })
}

/**
 * Update existing record in table
 * @param {string} tableName - Name of the table
 * @param {number} id - Record ID
 * @param {Object} record - Updated record data
 * @returns {Promise}
 */
export function updateTableRecord(tableName, id, record) {
  return request({
    url: `/mra/admin/table/${tableName}/${id}`,
    method: 'put',
    data: record
  })
}

/**
 * Delete record from table
 * @param {string} tableName - Name of the table
 * @param {number} id - Record ID
 * @returns {Promise}
 */
export function deleteTableRecord(tableName, id) {
  return request({
    url: `/mra/admin/table/${tableName}/${id}`,
    method: 'delete'
  })
}

/**
 * Execute Detection Query
 * @param {number} detectionId - Detection ID
 * @returns {Promise}
 */
export function executeDetectionQuery(detectionId) {
  return request({
    url: `/mra/admin/detection/list/${detectionId}`,
    method: 'get'
  })
}

/**
 * Get Detection History, 8 days, including today
 * @returns {Promise}
 */
export function getWeeklyDetections() {
  return request({
    url: `/mra/admin/detection/selectWeekly`,
    method: 'get'
  })
}

/**
 * Get Quarter Data
 * @returns {Promise}
 */
export function getQuarterlyDetections() {
  return request({
    url: `/mra/admin/detection/selectNearMissHistory`,
    method: 'get'
  })
}

/**
 * Upload files
 */
export function uploadFiles(data) {
  return request({
    url: '/mra/admin/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * List files and directories at the specified path
 * @param {string} path - Directory path to list
 * @returns {Promise} Response with items array
 */
export function listDownloadFiles(path) {
  return request({
    url: '/mra/admin/files',
    method: 'get',
    params: { path }
  })
}

/**
 * Get download URL for a specific file
 * @param {string} path - File path to download
 * @returns {string} Download URL for the file
 */
export function getDownloadUrl(path) {
  return `/mra/admin/download?path=${encodeURIComponent(path)}`
}

/**
 * Query Incident List
 * @param {Object} params - Query parameters
 * @param {string} [params.incidentNo] - Incident number
 * @param {string} [params.incidentStatus] - Incident status
 * @param {string} [params.primaryOwnerDept] - Primary owner department
 * @param {string} [params.riskRating] - Risk rating
 * @param {string} [params.occurrenceDateFrom] - Start date of occurrence
 * @param {string} [params.occurrenceDateTo] - End date of occurrence
 * @returns {Promise}
 */
export function getIncidentList(params = {}) {
  return request({
    url: '/mra/incident/list',
    method: 'get',
    params
  })
}

export function getIncidentData(params) {
  return request({
    url: '/mra/incident/exportIncident',
    method: 'post',
    data: params,
    responseType: 'blob'
  })
}


//export function exportDataInventory(data) {
//  return request({
//    url: '/kri/exportDataInventory',
//    method: 'post',
//    data: data,
//    responseType: 'blob'

//  })
//}
