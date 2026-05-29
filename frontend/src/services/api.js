import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const transpileCode = async (params) => {
  try {
    const response = await api.post('/transpile', params);
    return response.data;
  } catch (error) {
    console.error('Transpilation error:', error);
    throw error;
  }
};

export default api;
