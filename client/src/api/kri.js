import request from '@/utils/request';


/**
 * Upload a document for a KRI
 * @param {string} kriId - The ID of the KRI
 * @param {File} file - The file to upload
 * @returns {Promise} Promise that resolves to the response data
 */
export const uploadKRIDocument = async (kriId, file) => {
    console.log("Uploading document for KRI", kriId, file.name);
    const formData = new FormData();
    formData.append('file', file);

    const response = await request.post(
        `/kri/uploadDocument`,
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

/**
 * KRI Tasks API Functions
 */

/**
 * Fetch all KRI tasks
 * @returns {Promise} Promise that resolves to an array of KRI tasks
 */
export const getKriTasks = async () => {
  try {
    console.log("API: Fetching KRI tasks from new endpoint");
    const response = await request.get(`/kri/alltasks`);
    console.log("API: KRI tasks response:", response.data);
    return response.data || [];
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
  try {
    console.log("Submitting KRI value", kriValueData);
    const response = await request.post(
      `/kri/tasks/submitValue`,
      kriValueData
    );
    return response.data;
  } catch (error) {
    console.error('Error submitting KRI value:', error);
    throw error;
  }
};