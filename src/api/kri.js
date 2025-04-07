import axios from 'axios';

// Mock data with comprehensive KRI fields
const mockKRIs = [
  {
    kriId: 'KRI-001',
    name: 'Credit Risk Exposure',
    kriDesc: 'Measures the total credit risk exposure across all portfolios',
    dataProvider: 'Risk Analytics Team',
    owner: 'John Smith',
    l1RiskType: 'Credit Risk',
    l2RiskType: 'Portfolio Concentration',
    reportCycle: 'Monthly',
    rasStatus: 'RAS',
    status: 'Active',
    lastUpdated: '2023-05-15',
    threshold: { yellow: 85, red: 95 },
    documents: [],
    workflowStatus: 'Approved'
  },
  {
    kriId: 'KRI-002',
    name: 'Liquidity Coverage Ratio',
    kriDesc: 'Measures the bank\'s ability to meet short-term obligations',
    dataProvider: 'Treasury Department',
    owner: 'Jane Doe',
    l1RiskType: 'Liquidity Risk',
    l2RiskType: 'Funding Stability',
    reportCycle: 'Weekly',
    rasStatus: 'RAS',
    status: 'Active',
    lastUpdated: '2023-05-10',
    threshold: { yellow: 110, red: 100 },
    documents: [],
    workflowStatus: 'Pending Review'
  },
  {
    kriId: 'KRI-003',
    name: 'Operational Loss Events',
    kriDesc: 'Tracks the number of operational loss events above threshold',
    dataProvider: 'Operational Risk Team',
    owner: 'Robert Johnson',
    l1RiskType: 'Operational Risk',
    l2RiskType: 'Process Execution',
    reportCycle: 'Monthly',
    rasStatus: 'Non-RAS',
    status: 'Active',
    lastUpdated: '2023-05-01',
    threshold: { yellow: 5, red: 10 },
    documents: [],
    workflowStatus: 'Draft'
  },
  {
    kriId: 'KRI-004',
    name: 'IT System Availability',
    kriDesc: 'Measures the availability of critical IT systems',
    dataProvider: 'IT Department',
    owner: 'Sarah Williams',
    l1RiskType: 'Technology Risk',
    l2RiskType: 'System Availability',
    reportCycle: 'Daily',
    rasStatus: 'RAS',
    status: 'Active',
    lastUpdated: '2023-05-14',
    threshold: { yellow: 99.5, red: 99 },
    documents: [],
    workflowStatus: 'Approved'
  },
  {
    kriId: 'KRI-005',
    name: 'Regulatory Compliance Breaches',
    kriDesc: 'Tracks the number of regulatory compliance breaches',
    dataProvider: 'Compliance Department',
    owner: 'Michael Brown',
    l1RiskType: 'Compliance Risk',
    l2RiskType: 'Regulatory Compliance',
    reportCycle: 'Monthly',
    rasStatus: 'RAS',
    status: 'Active',
    lastUpdated: '2023-04-30',
    threshold: { yellow: 1, red: 3 },
    documents: [],
    workflowStatus: 'Under Review'
  }
];

// Generate 45 more mock KRIs to meet the 50+ requirement
for (let i = 6; i <= 50; i++) {
  const riskTypes = ['Credit Risk', 'Market Risk', 'Operational Risk', 'Liquidity Risk', 'Compliance Risk', 'Technology Risk', 'Strategic Risk'];
  const l2RiskTypes = {
    'Credit Risk': ['Default Risk', 'Concentration Risk', 'Country Risk', 'Settlement Risk'],
    'Market Risk': ['Interest Rate Risk', 'Foreign Exchange Risk', 'Commodity Risk', 'Equity Risk'],
    'Operational Risk': ['Process Risk', 'People Risk', 'System Risk', 'External Events'],
    'Liquidity Risk': ['Funding Risk', 'Market Liquidity Risk', 'Asset Liability Mismatch', 'Contingency Risk'],
    'Compliance Risk': ['Regulatory Risk', 'Legal Risk', 'Conduct Risk', 'Financial Crime Risk'],
    'Technology Risk': ['Cybersecurity Risk', 'Data Integrity Risk', 'System Availability Risk', 'IT Change Risk'],
    'Strategic Risk': ['Business Model Risk', 'Reputation Risk', 'Macroeconomic Risk', 'Competition Risk']
  };
  const reportCycles = ['Daily', 'Weekly', 'Monthly', 'Quarterly'];
  const rasStatuses = ['RAS', 'Non-RAS'];
  const workflowStatuses = ['Draft', 'Pending Review', 'Under Review', 'Approved', 'Rejected'];

  const l1RiskType = riskTypes[Math.floor(Math.random() * riskTypes.length)];
  const l2Options = l2RiskTypes[l1RiskType];
  const l2RiskType = l2Options[Math.floor(Math.random() * l2Options.length)];

  mockKRIs.push({
    kriId: `KRI-${i.toString().padStart(3, '0')}`,
    name: `${l1RiskType} - ${l2RiskType} Indicator`,
    kriDesc: `Standard banking KRI for measuring ${l2RiskType.toLowerCase()} within ${l1RiskType.toLowerCase()}`,
    dataProvider: ['Risk Analytics Team', 'Treasury Department', 'Compliance Department', 'IT Department'][Math.floor(Math.random() * 4)],
    owner: ['John Smith', 'Jane Doe', 'Robert Johnson', 'Sarah Williams', 'Michael Brown'][Math.floor(Math.random() * 5)],
    l1RiskType,
    l2RiskType,
    reportCycle: reportCycles[Math.floor(Math.random() * reportCycles.length)],
    rasStatus: rasStatuses[Math.floor(Math.random() * rasStatuses.length)],
    status: 'Active',
    lastUpdated: `2023-${Math.floor(Math.random() * 4) + 1}-${Math.floor(Math.random() * 28) + 1}`,
    threshold: { yellow: Math.floor(Math.random() * 90) + 10, red: Math.floor(Math.random() * 90) + 10 },
    documents: [],
    workflowStatus: workflowStatuses[Math.floor(Math.random() * workflowStatuses.length)]
  });
}

export const getKRIs = async () => {
    if (process.env.VUE_APP_MOCK === 'true') {
        console.log("Using mock data for getKRIs");
        return Promise.resolve(mockKRIs);
    }

    console.log(" enter getKRIs ")
    const response = await axios.get(`/${process.env.VUE_APP_BACKEND_NAME}/kri/all`);
    return response.data;
};

export const updateKRIDescription = async (kri_id, newDescription) => {
    if (process.env.VUE_APP_MOCK === 'true') {
        console.log("Using mock data for updateKRIDescription");
        const mockResponse = {
            success: true,
            message: 'Description updated successfully',
            updatedKRI: {
                id: kri_id,
                description: newDescription
            }
        };
        return Promise.resolve(mockResponse);
    }

    const response = await axios.post(`/${process.env.VUE_APP_BACKEND_NAME}/kri/updateDescription`, null, {
        params: {
            kriId: kri_id,
            newDescription,
        },
    });
    return response.data;
};

/**
 * Update a KRI with new data
 * @param {string} kriId - The ID of the KRI to update
 * @param {Object} kriData - The updated KRI data
 * @returns {Promise} Promise that resolves to the response data
 */
export const updateKRI = async (kriId, kriData) => {
    if (process.env.VUE_APP_MOCK === 'true') {
        console.log("Using mock data for updateKRI", kriId, kriData);
        const mockResponse = {
            success: true,
            message: 'KRI updated successfully',
            updatedKRI: {
                ...kriData,
                kriId
            }
        };
        return Promise.resolve(mockResponse);
    }

    const response = await axios.post(
        `/${process.env.VUE_APP_BACKEND_NAME}/kri/update`,
        kriData,
        {
            params: { kriId }
        }
    );
    return response.data;
};

/**
 * Upload a document for a KRI
 * @param {string} kriId - The ID of the KRI
 * @param {File} file - The file to upload
 * @returns {Promise} Promise that resolves to the response data
 */
export const uploadKRIDocument = async (kriId, file) => {
    if (process.env.VUE_APP_MOCK === 'true') {
        console.log("Using mock data for uploadKRIDocument", kriId, file.name);
        // Simulate a delay for the upload process
        await new Promise(resolve => setTimeout(resolve, 1000));

        const mockResponse = {
            success: true,
            message: 'Document uploaded successfully',
            document: {
                id: Math.floor(Math.random() * 1000),
                name: file.name,
                size: file.size,
                type: file.type,
                uploadDate: new Date().toISOString(),
                url: URL.createObjectURL(file) // Create a temporary URL for the file
            }
        };
        return Promise.resolve(mockResponse);
    }

    const formData = new FormData();
    formData.append('file', file);

    const response = await axios.post(
        `/${process.env.VUE_APP_BACKEND_NAME}/kri/uploadDocument`,
        formData,
        {
            params: { kriId },
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        }
    );
    return response.data;
};