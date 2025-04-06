import axios from 'axios';

// Mock data for KRI tasks
const mockKriTasks = [
  { id: 1, name: 'Z-KRI-0045-KRIV-0008', path: 'Global Financial Services > North America > Retail Banking', collectionStatus: 'Awaiting Collection', valueDate: null, value: null, breachStatus: 'Not Determined', hasTags: false, requiresAttention: true },
  { id: 2, name: 'Z-KRI-0045-KRIV-0007', path: 'Global Financial Services > North America > Retail Banking', collectionStatus: 'Awaiting Approval', valueDate: '11/14/2012', value: '0.50', breachStatus: 'Yellow', hasTags: true },
  { id: 3, name: 'Z-KRI-0045-KRIV-0006', path: 'Global Financial Services > North America > Retail Banking', collectionStatus: 'Collected', valueDate: '10/18/2012', value: '1.00', breachStatus: 'Yellow', hasTags: true },
  { id: 4, name: 'Z-KRI-0045-KRIV-0005', path: 'Global Financial Services > North America > Retail Banking', collectionStatus: 'Collected', valueDate: '9/12/2012', value: '1.00', breachStatus: 'Yellow', hasTags: true },
  { id: 5, name: 'Z-KRI-0045-KRIV-0004', path: 'Global Financial Services > North America > Retail Banking', collectionStatus: 'Collected', valueDate: '8/15/2012', value: '1.00', breachStatus: 'Yellow', hasTags: true },
  { id: 6, name: 'Z-KRI-0045-KRIV-0003', path: 'Global Financial Services > North America > Retail Banking', collectionStatus: 'Collected', valueDate: '7/12/2012', value: '1.00', breachStatus: 'Yellow', hasTags: true },
  { id: 7, name: 'Z-KRI-0045-KRIV-0002', path: 'Global Financial Services > North America > Retail Banking', collectionStatus: 'Collected', valueDate: '6/16/2012', value: '5.00', breachStatus: 'Red', hasTags: true },
  { id: 8, name: 'Z-KRI-0045-KRIV-0001', path: 'Global Financial Services > North America > Retail Banking', collectionStatus: 'Collected', valueDate: '5/16/2012', value: '1.00', breachStatus: 'Yellow', hasTags: true },
];

/**
 * Fetch all KRI tasks
 * @returns {Promise} Promise that resolves to an array of KRI tasks
 */
export const getKriTasks = async () => {
  if (process.env.VUE_APP_MOCK === 'true') {
    console.log("Using mock data for getKriTasks");
    return Promise.resolve(mockKriTasks);
  }

  try {
    const response = await axios.get(`/${process.env.VUE_APP_BACKEND_NAME}/kri/tasks`);
    return response.data;
  } catch (error) {
    console.error('Error fetching KRI tasks:', error);
    throw error;
  }
};

/**
 * Submit a KRI value
 * @param {Object} kriValueData - The KRI value data to submit
 * @returns {Promise} Promise that resolves to the response data
 */
export const submitKriValue = async (kriValueData) => {
  if (process.env.VUE_APP_MOCK === 'true') {
    console.log("Using mock data for submitKriValue", kriValueData);
    const mockResponse = {
      success: true,
      message: 'KRI value submitted successfully',
      updatedTask: {
        ...kriValueData,
        collectionStatus: 'Awaiting Approval'
      }
    };
    return Promise.resolve(mockResponse);
  }

  try {
    const response = await axios.post(
      `/${process.env.VUE_APP_BACKEND_NAME}/kri/submitValue`, 
      kriValueData
    );
    return response.data;
  } catch (error) {
    console.error('Error submitting KRI value:', error);
    throw error;
  }
};
