import axios from 'axios';

// Mock data
const mockKRIs = [
    { kriId: 1, name: 'KRI 1', kriDesc: 'Description 1' },
    { kriId: 2, name: 'KRI 2', kriDesc: 'Description 2' },
    { kriId: 3, name: 'KRI 3', kriDesc: 'Description 3' },
];

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