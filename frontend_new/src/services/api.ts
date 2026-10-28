import axios from 'axios';

// Configuração base da API
const api = axios.create({
  baseURL: 'http://localhost:8080/api', // Porta padrão do Spring Boot
  timeout: 5000, // Se o servidor não responder em 5s, dispara erro
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
