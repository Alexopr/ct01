import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true
});

// CSRF токен
let csrfToken: string | null = null;

// Функция для получения CSRF токена
const fetchCsrfToken = async () => {
  try {
    const response = await axios.get(`${API_URL}/v1/auth/csrf`, {
      withCredentials: true
    });
    // Backend возвращает ApiResponse с data.token
    csrfToken = response.data.data?.token || response.data.token;
    return csrfToken;
  } catch (error) {
    console.warn('Failed to fetch CSRF token:', error);
    return null;
  }
};

// Получаем CSRF токен при инициализации
fetchCsrfToken();

// Add request interceptor for authentication and CSRF
api.interceptors.request.use(
  async (config) => {
    // Добавляем токен аутентификации если есть
    const token = localStorage.getItem('token');
    if (token) {
      config.headers = config.headers || {};
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    // Добавляем CSRF токен для POST, PUT, DELETE запросов
    if (['post', 'put', 'delete', 'patch'].includes(config.method?.toLowerCase() || '')) {
      if (!csrfToken) {
        await fetchCsrfToken();
      }
      if (csrfToken) {
        config.headers = config.headers || {};
        config.headers['X-XSRF-TOKEN'] = csrfToken;
      }
    }
    
    return config;
  },
  (error) => Promise.reject(error)
);

// Add response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
    }
    
    // Если получили 403 (возможно проблема с CSRF), попробуем обновить токен
    if (error.response && error.response.status === 403) {
      await fetchCsrfToken();
      // Повторяем запрос с новым токеном
      if (csrfToken && error.config) {
        error.config.headers['X-XSRF-TOKEN'] = csrfToken;
        return api.request(error.config);
      }
    }
    
    return Promise.reject(error);
  }
);

export default api; 
