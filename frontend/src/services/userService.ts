import { apiService } from './api';
import type {
  User,
  Role,
  LoginRequest,
  TelegramAuthRequest,
  AuthResponse,
  UserFormData,
  UserRegistrationData,
  UserStatistics,
  PaginatedResponse
} from '../types/api';

/**
 * User service for authentication and user management
 */
export class UserService {
  // Authentication methods
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    try {
      const response = await apiService.login(credentials);
      this.setCurrentUser(response.user);
      return response;
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  }

  async register(userData: UserRegistrationData): Promise<User> {
    try {
      const user = await apiService.registerUser(userData);
      return user;
    } catch (error) {
      console.error('Registration failed:', error);
      throw error;
    }
  }

  async loginWithTelegram(telegramData: TelegramAuthRequest): Promise<AuthResponse> {
    try {
      const response = await apiService.loginWithTelegram(telegramData);
      this.setCurrentUser(response.user);
      return response;
    } catch (error) {
      console.error('Telegram login failed:', error);
      throw error;
    }
  }

  async logout(): Promise<void> {
    try {
      await apiService.logout();
      this.clearCurrentUser();
    } catch (error) {
      console.error('Logout failed:', error);
      // Clear user data even if logout request fails
      this.clearCurrentUser();
      throw error;
    }
  }

  async getCurrentUser(): Promise<User | null> {
    try {
      const user = await apiService.getCurrentUser();
      this.setCurrentUser(user);
      return user;
    } catch (error) {
      console.error('Failed to get current user:', error);
      this.clearCurrentUser();
      return null;
    }
  }

  // User management methods
  async getUsers(page = 0, size = 20): Promise<PaginatedResponse<User>> {
    return apiService.getUsers(page, size);
  }

  async getUserById(id: number): Promise<User> {
    return apiService.getUserById(id);
  }

  async createUser(userData: UserFormData): Promise<User> {
    return apiService.createUser(userData);
  }

  async updateUser(id: number, userData: Partial<UserFormData>): Promise<User> {
    return apiService.updateUser(id, userData);
  }

  async deleteUser(id: number): Promise<void> {
    return apiService.deleteUser(id);
  }

  async assignRole(userId: number, roleId: number): Promise<User> {
    return apiService.assignRole(userId, roleId);
  }

  async removeRole(userId: number, roleId: number): Promise<User> {
    return apiService.removeRole(userId, roleId);
  }

  async changePassword(userId: number, currentPassword: string, newPassword: string): Promise<void> {
    return apiService.changePassword(userId, currentPassword, newPassword);
  }

  async getUserStatistics(): Promise<UserStatistics> {
    return apiService.getUserStatistics();
  }

  // Role management methods
  async getRoles(): Promise<Role[]> {
    return apiService.getRoles();
  }

  async createRole(roleData: Partial<Role>): Promise<Role> {
    return apiService.createRole(roleData);
  }

  async updateRole(id: number, roleData: Partial<Role>): Promise<Role> {
    return apiService.updateRole(id, roleData);
  }

  async deleteRole(id: number): Promise<void> {
    return apiService.deleteRole(id);
  }

  // Local storage methods
  private setCurrentUser(user: User): void {
    localStorage.setItem('currentUser', JSON.stringify(user));
  }

  private clearCurrentUser(): void {
    localStorage.removeItem('currentUser');
  }

  getCurrentUserFromStorage(): User | null {
    try {
      const userJson = localStorage.getItem('currentUser');
      return userJson ? JSON.parse(userJson) : null;
    } catch (error) {
      console.error('Failed to parse user from storage:', error);
      this.clearCurrentUser();
      return null;
    }
  }

  // Permission checking methods
  hasPermission(permission: string): boolean {
    const user = this.getCurrentUserFromStorage();
    if (!user) return false;

    return user.roles.some(role =>
      role.permissions.some(perm => perm.name === permission)
    );
  }

  hasRole(roleName: string): boolean {
    const user = this.getCurrentUserFromStorage();
    if (!user) return false;

    return user.roles.some(role => role.name === roleName);
  }

  hasAnyRole(roleNames: string[]): boolean {
    const user = this.getCurrentUserFromStorage();
    if (!user) return false;

    return user.roles.some(role => roleNames.includes(role.name));
  }

  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  isAuthenticated(): boolean {
    return this.getCurrentUserFromStorage() !== null;
  }

  // Utility methods
  getFullName(user?: User): string {
    const currentUser = user || this.getCurrentUserFromStorage();
    if (!currentUser) return '';

    const { firstName, lastName, telegramFirstName, telegramLastName } = currentUser;
    
    if (firstName && lastName) {
      return `${firstName} ${lastName}`;
    }
    
    if (telegramFirstName && telegramLastName) {
      return `${telegramFirstName} ${telegramLastName}`;
    }
    
    if (firstName || telegramFirstName) {
      return firstName || telegramFirstName || '';
    }
    
    return currentUser.username;
  }

  getDisplayName(user?: User): string {
    const currentUser = user || this.getCurrentUserFromStorage();
    if (!currentUser) return '';

    return currentUser.telegramUsername || currentUser.username;
  }

  getAvatarUrl(user?: User): string | null {
    const currentUser = user || this.getCurrentUserFromStorage();
    if (!currentUser) return null;

    return currentUser.telegramPhotoUrl || null;
  }

  formatUserRole(user: User): string {
    if (!user.roles || user.roles.length === 0) return 'Пользователь';
    
    const roleNames = user.roles.map(role => {
      switch (role.name) {
        case 'ADMIN': return 'Администратор';
        case 'MODERATOR': return 'Модератор';
        case 'USER': return 'Пользователь';
        default: return role.name;
      }
    });
    
    return roleNames.join(', ');
  }
}

// Create singleton instance
const userService = new UserService();

// Export singleton instance as default
export default userService;

// Export individual methods for convenience
export const {
  login,
  register,
  loginWithTelegram,
  logout,
  getCurrentUser,
  getUsers,
  getUserById,
  createUser,
  updateUser,
  deleteUser,
  assignRole,
  removeRole,
  changePassword,
  getUserStatistics,
  getRoles,
  createRole,
  updateRole,
  deleteRole,
  getCurrentUserFromStorage,
  hasPermission,
  hasRole,
  hasAnyRole,
  isAdmin,
  isAuthenticated,
  getFullName,
  getDisplayName,
  getAvatarUrl,
  formatUserRole
} = userService;

// Export types for convenience
export type { User, Role, LoginRequest, TelegramAuthRequest, AuthResponse, UserFormData, UserRegistrationData, UserStatistics }; 
