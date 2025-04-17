import request from '@/utils/request';

/**
 * KRI Inventory API Functions
 */

/**
 * Get all KRIs
 * @returns {Promise} Promise that resolves to an array of KRIs
 */
export const getKRIs = async () => {
    console.log("Fetching KRIs " +  `/kri/all`);
    const response = await request.get(`/kri/all`);
    return response.data;
};

/**
 * Update a KRI description
 * @param {string} kri_id - The ID of the KRI to update
 * @param {string} newDescription - The new description
 * @returns {Promise} Promise that resolves to the response data
 */
export const updateKRIDescription = async (kri_id, newDescription) => {
    console.log("Updating KRI description", kri_id, newDescription);
    const response = await request.post(`/kri/updateDescription`, null, {
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
    console.log("Updating KRI", kriId, kriData);
    const response = await request.post(
        `/kri/update`,
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