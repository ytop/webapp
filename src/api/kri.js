import axios from 'axios'
// const API_BASE_URL = '/rdaapi'

const mockKRIs = [
  { kriId: 1, kriDesc: 'Mock KRI 1' },
  { kriId: 2, kriDesc: 'Mock KRI 2' },
  { kriId: 3, kriDesc: 'Mock KRI 3' }
]

export const getKRIs = async() => {
  console.log(' enter getKRIs ')
  if (process.env.VUE_APP_MOCK === 'MOCK') {
    return mockKRIs
  }
  const response = await axios.get(`/${process.env.VUE_APP_BACKEND_NAME}/kri/all`)
  return response.data
}

export const updateKRIDescription = async(kir_id, newDescription) => {
  if (process.env.VUE_APP_MOCK === 'MOCK') {
    const kri = mockKRIs.find(k => k.id === kir_id)
    if (kri) {
      kri.description = newDescription
      return { success: true }
    }
    return { success: false, message: 'KRI not found' }
  }
  const response = await axios.post(`/${process.env.VUE_APP_BACKEND_NAME}/kri/updateDescription`, null, {
    params: {
      kriId: kir_id,
      newDescription
    }
  })
  return response.data
}
