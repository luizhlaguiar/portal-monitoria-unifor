import axios from 'axios';

// Configuração base da API
const api = axios.create({
  baseURL: 'http://localhost:8090/api', // Ajuste a porta se o seu Java estiver em outra
});

// Interceptor para adicionar o Token de Autenticação em todas as requisições
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

export default api;
