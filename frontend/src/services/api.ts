import axios from 'axios';
import type { AxiosInstance, AxiosResponse, AxiosError } from 'axios';
import { toast } from 'react-hot-toast';
import type {
  PaginatedResponse,
  User,
  Role,
  Coin,
  TrackedCoin,
  Exchange,
  Settings,
  LoginRequest,
  TelegramAuthRequest,
  AuthResponse,
  UserFormData,
  UserRegistrationData,
  CoinFormData,
  ExchangeFormData,
  UserStatistics,
  CoinStatistics,
  SystemStatistics,
  ApiError
} from '../types/api';

// API Configuration
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      timeout: 30000,
      withCredentials: true,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor
    this.api.interceptors.request.use(
      (config) => {
        // Add CSRF token if available
        const csrfToken = this.getCsrfToken();
        if (csrfToken) {
          config.headers['X-CSRF-TOKEN'] = csrfToken;
        }
        
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor
    this.api.interceptors.response.use(
      (response: AxiosResponse) => {
        return response;
      },
      (error: AxiosError<ApiError>) => {
        this.handleApiError(error);
        return Promise.reject(error);
      }
    );
  }

  private getCsrfToken(): string | null {
    const cookies = document.cookie.split(';');
    for (const cookie of cookies) {
      const [name, value] = cookie.trim().split('=');
      if (name === 'XSRF-TOKEN') {
        return decodeURIComponent(value);
      }
    }
    return null;
  }

  private handleApiError(error: AxiosError<ApiError>) {
    if (error.response) {
      const { status, data } = error.response;
      
      switch (status) {
        case 401:
          toast.error('Сессия истекла. Пожалуйста, войдите снова.');
          window.location.href = '/login';
          break;
        case 403:
          toast.error('Недостаточно прав для выполнения операции');
          break;
        case 404:
          toast.error('Ресурс не найден');
          break;
        case 500:
          toast.error('Внутренняя ошибка сервера');
          break;
        default:
          toast.error(data?.message || 'Произошла ошибка');
      }
    } else if (error.request) {
      toast.error('Сервер недоступен. Проверьте подключение к интернету.');
    } else {
      toast.error('Произошла неожиданная ошибка');
    }
  }

  // Generic API methods
  private async get<T>(url: string, params?: any): Promise<T> {
    const response = await this.api.get<T>(url, { params });
    return response.data;
  }

  private async post<T>(url: string, data?: any): Promise<T> {
    const response = await this.api.post<T>(url, data);
    return response.data;
  }

  private async put<T>(url: string, data?: any): Promise<T> {
    const response = await this.api.put<T>(url, data);
    return response.data;
  }

  private async delete<T>(url: string): Promise<T> {
    const response = await this.api.delete<T>(url);
    return response.data;
  }

  // Authentication API
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    return this.post<AuthResponse>('/auth/login', credentials);
  }

  async loginWithTelegram(telegramData: TelegramAuthRequest): Promise<AuthResponse> {
    return this.post<AuthResponse>('/auth/telegram', telegramData);
  }

  async logout(): Promise<void> {
    return this.post<void>('/auth/logout');
  }

  async getCurrentUser(): Promise<User> {
    return this.get<User>('/auth/me');
  }

  // User Management API
  async getUsers(page = 0, size = 20): Promise<PaginatedResponse<User>> {
    return this.get<PaginatedResponse<User>>('/v1/users', { page, size });
  }

  async getUserById(id: number): Promise<User> {
    return this.get<User>(`/v1/users/${id}`);
  }

  async createUser(userData: UserFormData): Promise<User> {
    return this.post<User>('/v1/users', userData);
  }

  async registerUser(userData: UserRegistrationData): Promise<User> {
    return this.post<User>('/auth/register', userData);
  }

  async updateUser(id: number, userData: Partial<UserFormData>): Promise<User> {
    return this.put<User>(`/v1/users/${id}`, userData);
  }

  async deleteUser(id: number): Promise<void> {
    return this.delete<void>(`/v1/users/${id}`);
  }

  async assignRole(userId: number, roleId: number): Promise<User> {
    return this.post<User>(`/v1/users/${userId}/roles/${roleId}`);
  }

  async removeRole(userId: number, roleId: number): Promise<User> {
    return this.delete<User>(`/v1/users/${userId}/roles/${roleId}`);
  }

  async changePassword(userId: number, currentPassword: string, newPassword: string): Promise<void> {
    return this.post<void>(`/v1/users/${userId}/change-password`, {
      currentPassword,
      newPassword
    });
  }

  async getUserStatistics(): Promise<UserStatistics> {
    return this.get<UserStatistics>('/v1/users/statistics');
  }

  // Role Management API
  async getRoles(): Promise<Role[]> {
    return this.get<Role[]>('/v1/users/roles');
  }

  async createRole(roleData: Partial<Role>): Promise<Role> {
    return this.post<Role>('/v1/users/roles', roleData);
  }

  async updateRole(id: number, roleData: Partial<Role>): Promise<Role> {
    return this.put<Role>(`/v1/users/roles/${id}`, roleData);
  }

  async deleteRole(id: number): Promise<void> {
    return this.delete<void>(`/v1/users/roles/${id}`);
  }

  // Coin Management API
  async getCoins(page = 0, size = 20): Promise<PaginatedResponse<Coin>> {
    return this.get<PaginatedResponse<Coin>>('/v1/coins', { page, size });
  }

  async searchCoins(query: string): Promise<Coin[]> {
    return this.get<Coin[]>('/v1/coins/search', { q: query });
  }

  async getCoinById(id: number): Promise<Coin> {
    return this.get<Coin>(`/v1/coins/${id}`);
  }

  async createCoin(coinData: CoinFormData): Promise<Coin> {
    return this.post<Coin>('/v1/coins', coinData);
  }

  async updateCoin(id: number, coinData: Partial<CoinFormData>): Promise<Coin> {
    return this.put<Coin>(`/v1/coins/${id}`, coinData);
  }

  async deleteCoin(id: number): Promise<void> {
    return this.delete<void>(`/v1/coins/${id}`);
  }

  async getTrackedCoins(): Promise<TrackedCoin[]> {
    return this.get<TrackedCoin[]>('/v1/coins/tracked');
  }

  async trackCoin(coinId: number, exchangeIds: number[]): Promise<TrackedCoin> {
    return this.post<TrackedCoin>(`/v1/coins/${coinId}/track`, { exchangeIds });
  }

  async untrackCoin(coinId: number): Promise<void> {
    return this.delete<void>(`/v1/coins/${coinId}/track`);
  }

  async getCoinStatistics(): Promise<CoinStatistics> {
    return this.get<CoinStatistics>('/v1/coins/statistics');
  }

  // Exchange Management API
  async getExchanges(): Promise<Exchange[]> {
    return this.get<Exchange[]>('/v1/exchanges');
  }

  async getExchangeById(id: number): Promise<Exchange> {
    return this.get<Exchange>(`/v1/exchanges/${id}`);
  }

  async createExchange(exchangeData: ExchangeFormData): Promise<Exchange> {
    return this.post<Exchange>('/v1/exchanges', exchangeData);
  }

  async updateExchange(id: number, exchangeData: Partial<ExchangeFormData>): Promise<Exchange> {
    return this.put<Exchange>(`/v1/exchanges/${id}`, exchangeData);
  }

  async deleteExchange(id: number): Promise<void> {
    return this.delete<void>(`/v1/exchanges/${id}`);
  }

  async getExchangeStatus(): Promise<Record<string, any>> {
    return this.get<Record<string, any>>('/v1/exchanges/status');
  }

  async testExchangeConnection(id: number): Promise<boolean> {
    return this.post<boolean>(`/v1/exchanges/${id}/test`);
  }

  // Settings API
  async getSettings(): Promise<Settings> {
    return this.get<Settings>('/v1/settings');
  }

  async getSettingsByCategory(category: string): Promise<Record<string, any>> {
    return this.get<Record<string, any>>(`/v1/settings/${category}`);
  }

  async updateSettings(category: string, settings: Record<string, any>): Promise<Record<string, any>> {
    return this.put<Record<string, any>>(`/v1/settings/${category}`, settings);
  }

  async resetSettings(category: string): Promise<Record<string, any>> {
    return this.post<Record<string, any>>(`/v1/settings/${category}/reset`);
  }

  async checkPermissions(category: string): Promise<Record<string, any>> {
    return this.get<Record<string, any>>(`/v1/settings/permissions/${category}`);
  }

  // System Statistics API
  async getSystemStatistics(): Promise<SystemStatistics> {
    return this.get<SystemStatistics>('/v1/system/statistics');
  }

  async getHealthCheck(): Promise<Record<string, any>> {
    return this.get<Record<string, any>>('/actuator/health');
  }
}

// Export singleton instance
export const apiService = new ApiService();
export default apiService; 
