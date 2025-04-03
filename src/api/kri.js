import axios from 'axios'
// const API_BASE_URL = '/rdaapi'

// Mock KRI inventory data
const mockKRIs = [
  {
    kriId: 'KRI-001',
    kriDesc: 'Number of system outages',
    riskCategory: 'Operational Risk',
    measurementFrequency: 'Monthly',
    warningThreshold: 3,
    criticalThreshold: 5,
    owner: 'IT Department',
    status: 'Active'
  },
  {
    kriId: 'KRI-002',
    kriDesc: 'Customer complaints',
    riskCategory: 'Reputational Risk',
    measurementFrequency: 'Monthly',
    warningThreshold: 10,
    criticalThreshold: 20,
    owner: 'Customer Service',
    status: 'Active'
  },
  {
    kriId: 'KRI-003',
    kriDesc: 'Failed regulatory compliance checks',
    riskCategory: 'Compliance Risk',
    measurementFrequency: 'Monthly',
    warningThreshold: 1,
    criticalThreshold: 3,
    owner: 'Compliance Department',
    status: 'Active'
  }
]

// Mock KRI data collection entries
const mockKRIData = [
  {
    id: 1,
    kriId: 'KRI-001',
    period: '2025-03',
    value: 2,
    status: 'Normal',
    comments: 'Within acceptable range',
    submissionDate: '2025-04-02',
    submittedBy: 'John Smith',
    approvalStatus: 'Approved',
    approvedBy: 'Sarah Manager',
    approvalDate: '2025-04-03',
    attachments: [],
    history: [
      {
        value: 2,
        status: 'Normal',
        comments: 'Within acceptable range',
        submissionDate: '2025-04-02',
        submittedBy: 'John Smith'
      }
    ]
  },
  {
    id: 2,
    kriId: 'KRI-002',
    period: '2025-03',
    value: 15,
    status: 'Warning',
    comments: 'Increase in complaints due to mobile app issues',
    submissionDate: '2025-04-02',
    submittedBy: 'Jane Doe',
    approvalStatus: 'Pending',
    approvedBy: null,
    approvalDate: null,
    attachments: [],
    history: [
      {
        value: 15,
        status: 'Warning',
        comments: 'Increase in complaints due to mobile app issues',
        submissionDate: '2025-04-02',
        submittedBy: 'Jane Doe'
      }
    ]
  },
  {
    id: 3,
    kriId: 'KRI-003',
    period: '2025-03',
    value: 0,
    status: 'Normal',
    comments: 'All compliance checks passed',
    submissionDate: '2025-04-01',
    submittedBy: 'Robert Johnson',
    approvalStatus: 'Approved',
    approvedBy: 'Sarah Manager',
    approvalDate: '2025-04-02',
    attachments: [],
    history: [
      {
        value: 0,
        status: 'Normal',
        comments: 'All compliance checks passed',
        submissionDate: '2025-04-01',
        submittedBy: 'Robert Johnson'
      }
    ]
  }
]

// Get all KRIs from inventory
export const getKRIs = async() => {
  console.log('Fetching KRIs from inventory')
  if (process.env.VUE_APP_MOCK === 'MOCK') {
    return mockKRIs
  }
  const response = await axios.get(`/${process.env.VUE_APP_BACKEND_NAME}/kri/all`)
  return response.data
}

// Update KRI description
export const updateKRIDescription = async(kriId, newDescription) => {
  if (process.env.VUE_APP_MOCK === 'MOCK') {
    const kri = mockKRIs.find(k => k.kriId === kriId)
    if (kri) {
      kri.kriDesc = newDescription
      return { success: true }
    }
    return { success: false, message: 'KRI not found' }
  }
  const response = await axios.post(`/${process.env.VUE_APP_BACKEND_NAME}/kri/updateDescription`, null, {
    params: {
      kriId,
      newDescription
    }
  })
  return response.data
}

// Add new KRI to inventory
export const addKRI = async(kriData) => {
  if (process.env.VUE_APP_MOCK === 'MOCK') {
    // Check if KRI ID already exists
    if (mockKRIs.some(k => k.kriId === kriData.kriId)) {
      return { success: false, message: 'KRI ID already exists' }
    }
    mockKRIs.push(kriData)
    return { success: true, data: kriData }
  }
  const response = await axios.post(`/${process.env.VUE_APP_BACKEND_NAME}/kri/add`, kriData)
  return response.data
}

// Update KRI in inventory
export const updateKRI = async(kriId, kriData) => {
  if (process.env.VUE_APP_MOCK === 'MOCK') {
    const index = mockKRIs.findIndex(k => k.kriId === kriId)
    if (index !== -1) {
      mockKRIs[index] = { ...mockKRIs[index], ...kriData }
      return { success: true, data: mockKRIs[index] }
    }
    return { success: false, message: 'KRI not found' }
  }
  const response = await axios.put(`/${process.env.VUE_APP_BACKEND_NAME}/kri/update/${kriId}`, kriData)
  return response.data
}

// Delete KRI from inventory
export const deleteKRI = async(kriId) => {
  if (process.env.VUE_APP_MOCK === 'MOCK') {
    const index = mockKRIs.findIndex(k => k.kriId === kriId)
    if (index !== -1) {
      mockKRIs.splice(index, 1)
      return { success: true }
    }
    return { success: false, message: 'KRI not found' }
  }
  const response = await axios.delete(`/${process.env.VUE_APP_BACKEND_NAME}/kri/delete/${kriId}`)
  return response.data
}

// Get KRI data collection entries
export const getKRIData = async(period = null, kriId = null) => {
  if (process.env.VUE_APP_MOCK === 'MOCK') {
    let filteredData = [...mockKRIData]
    if (period) {
      filteredData = filteredData.filter(d => d.period === period)
    }
    if (kriId) {
      filteredData = filteredData.filter(d => d.kriId === kriId)
    }
    return filteredData
  }
  let url = `/${process.env.VUE_APP_BACKEND_NAME}/kri/data`
  const params = {}
  if (period) params.period = period
  if (kriId) params.kriId = kriId

  const response = await axios.get(url, { params })
  return response.data
}

// Submit KRI data for a period
export const submitKRIData = async(dataEntry) => {
  if (process.env.VUE_APP_MOCK === 'MOCK') {
    // Check if entry already exists
    const existingIndex = mockKRIData.findIndex(
      d => d.kriId === dataEntry.kriId && d.period === dataEntry.period
    )

    const currentDate = new Date().toISOString().split('T')[0];
    const historyEntry = {
      value: dataEntry.value,
      status: dataEntry.status,
      comments: dataEntry.comments,
      submissionDate: currentDate,
      submittedBy: dataEntry.submittedBy
    };

    if (existingIndex !== -1) {
      // Update existing entry
      const existingData = mockKRIData[existingIndex];

      // Add to history if values changed
      const history = [...(existingData.history || [])];
      history.push(historyEntry);

      mockKRIData[existingIndex] = {
        ...existingData,
        ...dataEntry,
        submissionDate: currentDate,
        approvalStatus: 'Pending',
        approvedBy: null,
        approvalDate: null,
        history: history
      }
      return { success: true, data: mockKRIData[existingIndex] }
    } else {
      // Add new entry
      const newEntry = {
        id: mockKRIData.length + 1,
        ...dataEntry,
        submissionDate: currentDate,
        approvalStatus: 'Pending',
        approvedBy: null,
        approvalDate: null,
        attachments: [],
        history: [historyEntry]
      }
      mockKRIData.push(newEntry)
      return { success: true, data: newEntry }
    }
  }
  const response = await axios.post(`/${process.env.VUE_APP_BACKEND_NAME}/kri/data/submit`, dataEntry)
  return response.data
}

// Approve KRI data submission
export const approveKRIData = async(kriId, period, approverData) => {
  if (process.env.VUE_APP_MOCK === 'MOCK') {
    const existingIndex = mockKRIData.findIndex(
      d => d.kriId === kriId && d.period === period
    )

    if (existingIndex !== -1) {
      mockKRIData[existingIndex] = {
        ...mockKRIData[existingIndex],
        approvalStatus: 'Approved',
        approvedBy: approverData.approvedBy,
        approvalDate: new Date().toISOString().split('T')[0]
      }
      return { success: true, data: mockKRIData[existingIndex] }
    } else {
      return { success: false, message: 'KRI data not found' }
    }
  }
  const response = await axios.post(`/${process.env.VUE_APP_BACKEND_NAME}/kri/data/approve`, {
    kriId,
    period,
    ...approverData
  })
  return response.data
}

// Reject KRI data submission
export const rejectKRIData = async(kriId, period, rejectionData) => {
  if (process.env.VUE_APP_MOCK === 'MOCK') {
    const existingIndex = mockKRIData.findIndex(
      d => d.kriId === kriId && d.period === period
    )

    if (existingIndex !== -1) {
      mockKRIData[existingIndex] = {
        ...mockKRIData[existingIndex],
        approvalStatus: 'Rejected',
        approvedBy: rejectionData.rejectedBy,
        approvalDate: new Date().toISOString().split('T')[0],
        rejectionReason: rejectionData.rejectionReason
      }
      return { success: true, data: mockKRIData[existingIndex] }
    } else {
      return { success: false, message: 'KRI data not found' }
    }
  }
  const response = await axios.post(`/${process.env.VUE_APP_BACKEND_NAME}/kri/data/reject`, {
    kriId,
    period,
    ...rejectionData
  })
  return response.data
}

// Upload attachment for KRI data
export const uploadKRIAttachment = async(kriId, period, file) => {
  if (process.env.VUE_APP_MOCK === 'MOCK') {
    const existingIndex = mockKRIData.findIndex(
      d => d.kriId === kriId && d.period === period
    )

    if (existingIndex !== -1) {
      // Simulate file upload
      const attachment = {
        id: Date.now(),
        name: file.name,
        size: file.size,
        type: file.type,
        uploadDate: new Date().toISOString().split('T')[0],
        uploadedBy: localStorage.getItem('userName') || 'Current User'
      }

      mockKRIData[existingIndex].attachments = [
        ...(mockKRIData[existingIndex].attachments || []),
        attachment
      ]

      return { success: true, data: attachment }
    } else {
      return { success: false, message: 'KRI data not found' }
    }
  }

  // In a real implementation, we would use FormData to upload the file
  const formData = new FormData()
  formData.append('file', file)
  formData.append('kriId', kriId)
  formData.append('period', period)

  const response = await axios.post(
    `/${process.env.VUE_APP_BACKEND_NAME}/kri/data/attachment`,
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }
  )
  return response.data
}

// Get KRI data history
export const getKRIHistory = async(kriId, period) => {
  if (process.env.VUE_APP_MOCK === 'MOCK') {
    const entry = mockKRIData.find(
      d => d.kriId === kriId && d.period === period
    )

    if (entry) {
      return entry.history || []
    } else {
      return []
    }
  }

  const response = await axios.get(`/${process.env.VUE_APP_BACKEND_NAME}/kri/data/history`, {
    params: { kriId, period }
  })
  return response.data
}

// Get KRI data summary for reporting
export const getKRIDataSummary = async(startPeriod, endPeriod) => {
  if (process.env.VUE_APP_MOCK === 'MOCK') {
    // Filter data within the period range
    const filteredData = mockKRIData.filter(
      d => d.period >= startPeriod && d.period <= endPeriod
    )

    // Group by KRI ID
    const summary = {}
    filteredData.forEach(entry => {
      if (!summary[entry.kriId]) {
        const kri = mockKRIs.find(k => k.kriId === entry.kriId)
        summary[entry.kriId] = {
          kriId: entry.kriId,
          kriDesc: kri ? kri.kriDesc : 'Unknown',
          riskCategory: kri ? kri.riskCategory : 'Unknown',
          data: []
        }
      }
      summary[entry.kriId].data.push({
        period: entry.period,
        value: entry.value,
        status: entry.status,
        approvalStatus: entry.approvalStatus
      })
    })

    return Object.values(summary)
  }

  const response = await axios.get(`/${process.env.VUE_APP_BACKEND_NAME}/kri/data/summary`, {
    params: { startPeriod, endPeriod }
  })
  return response.data
}

// Export KRI data to CSV
export const exportKRIDataToCSV = (period) => {
  if (process.env.VUE_APP_MOCK === 'MOCK') {
    // Filter data for the selected period
    const filteredData = mockKRIData.filter(d => d.period === period)

    // Prepare data for CSV
    const csvData = filteredData.map(entry => {
      const kri = mockKRIs.find(k => k.kriId === entry.kriId) || {}
      return {
        'KRI ID': entry.kriId,
        'KRI Description': kri.kriDesc || 'Unknown',
        'Risk Category': kri.riskCategory || 'Unknown',
        'Period': entry.period,
        'Value': entry.value,
        'Status': entry.status,
        'Warning Threshold': kri.warningThreshold,
        'Critical Threshold': kri.criticalThreshold,
        'Comments': entry.comments,
        'Submitted By': entry.submittedBy,
        'Submission Date': entry.submissionDate,
        'Approval Status': entry.approvalStatus || 'Pending',
        'Approved By': entry.approvedBy || '',
        'Approval Date': entry.approvalDate || ''
      }
    })

    return csvData
  }

  // In a real implementation, we would call an API endpoint to get the CSV data
  // or generate it on the client side from the API response
  return []
}

// Import KRI data from CSV
export const importKRIDataFromCSV = async(csvData, period, submitter) => {
  if (process.env.VUE_APP_MOCK === 'MOCK') {
    const results = {
      success: [],
      errors: []
    }

    // Process each row in the CSV
    for (const row of csvData) {
      try {
        const kriId = row['KRI ID']
        const value = parseFloat(row['Value'])

        // Validate KRI ID
        const kri = mockKRIs.find(k => k.kriId === kriId)
        if (!kri) {
          results.errors.push({
            kriId,
            error: 'KRI ID not found in inventory'
          })
          continue
        }

        // Determine status based on thresholds
        let status = 'Normal'
        if (value >= kri.criticalThreshold) {
          status = 'Critical'
        } else if (value >= kri.warningThreshold) {
          status = 'Warning'
        }

        // Prepare data entry
        const dataEntry = {
          kriId,
          period,
          value,
          status,
          comments: row['Comments'] || '',
          submittedBy: submitter
        }

        // Submit the data
        const result = await submitKRIData(dataEntry)
        if (result.success) {
          results.success.push({
            kriId,
            message: 'Data imported successfully'
          })
        } else {
          results.errors.push({
            kriId,
            error: result.message || 'Failed to import data'
          })
        }
      } catch (error) {
        results.errors.push({
          kriId: row['KRI ID'] || 'Unknown',
          error: error.message || 'Unknown error'
        })
      }
    }

    return results
  }

  // In a real implementation, we would call an API endpoint to import the CSV data
  const response = await axios.post(`/${process.env.VUE_APP_BACKEND_NAME}/kri/data/import`, {
    csvData,
    period,
    submitter
  })
  return response.data
}
