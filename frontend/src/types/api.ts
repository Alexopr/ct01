// API Response Types
export interface ApiResponse<T = any> {
  data?: T;
  message?: string;
  status: number;
  timestamp?: string;
  error?: string;
  details?: Record<string, any>;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}

// User Types
export interface User {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  telegramId?: number;
  telegramUsername?: string;
  telegramFirstName?: string;
  telegramLastName?: string;
  telegramPhotoUrl?: string;
  isFromTelegram?: boolean;
  roles: Role[];
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Role {
  id: number;
  name: string;
  description?: string;
  permissions: Permission[];
  createdAt: string;
  updatedAt: string;
}

export interface Permission {
  id: number;
  name: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

// Coin Types
export interface Coin {
  id: number;
  symbol: string;
  name: string;
  description?: string;
  logoUrl?: string;
  marketCap?: number;
  currentPrice?: number;
  priceChange24h?: number;
  volume24h?: number;
  rank?: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TrackedCoin {
  id: number;
  coin: Coin;
  exchanges: Exchange[];
  isActive: boolean;
  trackingStarted: string;
  lastPriceUpdate?: string;
  averagePrice?: number;
  minPrice?: number;
  maxPrice?: number;
}

// Exchange Types
export interface Exchange {
  id: number;
  name: string;
  displayName: string;
  baseUrl: string;
  apiKey?: string;
  secretKey?: string;
  isActive: boolean;
  rateLimitPerMinute: number;
  connectionTimeout: number;
  readTimeout: number;
  retryAttempts: number;
  healthStatus: ExchangeHealthStatus;
  lastHealthCheck?: string;
  supportedCoins: string[];
  createdAt: string;
  updatedAt: string;
}

export enum ExchangeHealthStatus {
  HEALTHY = 'HEALTHY',
  DEGRADED = 'DEGRADED',
  DOWN = 'DOWN',
  UNKNOWN = 'UNKNOWN'
}

// Price Types
export interface PriceHistory {
  id: number;
  coin: Coin;
  exchange: Exchange;
  price: number;
  volume?: number;
  timestamp: string;
}

export interface PriceAlert {
  id: number;
  user: User;
  coin: Coin;
  exchange?: Exchange;
  targetPrice: number;
  condition: PriceCondition;
  isActive: boolean;
  isTriggered: boolean;
  triggeredAt?: string;
  createdAt: string;
}

export enum PriceCondition {
  ABOVE = 'ABOVE',
  BELOW = 'BELOW',
  EQUALS = 'EQUALS'
}

// Settings Types
export interface Settings {
  trading: TradingSettings;
  exchanges: ExchangeSettings;
  notifications: NotificationSettings;
  system: SystemSettings;
}

export interface TradingSettings {
  autoTrade: boolean;
  riskLevel: 'low' | 'medium' | 'high';
  maxPositionSize: number;
  stopLossPercentage: number;
  takeProfitPercentage: number;
}

export interface ExchangeSettings {
  defaultExchange: string;
  enabledExchanges: string[];
  rateLimitEnabled: boolean;
  maxRequestsPerMinute: number;
}

export interface NotificationSettings {
  emailEnabled: boolean;
  pushEnabled: boolean;
  priceAlerts: boolean;
  tradeAlerts: boolean;
  systemAlerts: boolean;
}

export interface SystemSettings {
  maintenanceMode: boolean;
  logLevel: 'DEBUG' | 'INFO' | 'WARN' | 'ERROR';
  cacheEnabled: boolean;
  sessionTimeout: number;
}

// Authentication Types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface TelegramAuthRequest {
  id: number;
  first_name?: string;
  last_name?: string;
  username?: string;
  photo_url?: string;
  auth_date: number;
  hash: string;
}

export interface AuthResponse {
  user: User;
  sessionId: string;
  expiresAt: string;
}

// Form Types
export interface UserFormData {
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  password?: string;
  confirmPassword?: string;
  roles?: number[];
}

export interface UserRegistrationData {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
}

export interface CoinFormData {
  symbol: string;
  name: string;
  description?: string;
  logoUrl?: string;
}

export interface ExchangeFormData {
  name: string;
  displayName: string;
  baseUrl: string;
  apiKey?: string;
  secretKey?: string;
  rateLimitPerMinute: number;
  connectionTimeout: number;
  readTimeout: number;
  retryAttempts: number;
}

// Statistics Types
export interface UserStatistics {
  totalUsers: number;
  activeUsers: number;
  newUsersToday: number;
  usersByRole: Record<string, number>;
}

export interface CoinStatistics {
  totalCoins: number;
  trackedCoins: number;
  activeExchanges: number;
  totalPriceUpdates: number;
  averageUpdateFrequency: number;
}

export interface SystemStatistics {
  uptime: number;
  memoryUsage: number;
  cpuUsage: number;
  activeConnections: number;
  requestsPerMinute: number;
}

// Error Types
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  details?: Record<string, any>;
  path?: string;
}

// WebSocket Types
export interface WebSocketMessage<T = any> {
  type: string;
  data: T;
  timestamp: string;
}

export interface PriceUpdateMessage {
  coinId: number;
  exchangeId: number;
  price: number;
  volume?: number;
  timestamp: string;
}

export interface SystemNotification {
  id: string;
  type: 'info' | 'warning' | 'error' | 'success';
  title: string;
  message: string;
  timestamp: string;
  read: boolean;
}

// Subscription Types
export interface SubscriptionPlan {
  id: string;
  name: string;
  displayName: string;
  description: string;
  price: number;
  currency: string;
  billingCycle: 'MONTHLY' | 'YEARLY';
  features: string[];
  limits: Record<string, number>;
  isRecommended?: boolean;
  trialDays?: number;
}

export interface SubscriptionStatusDto {
  id: number;
  userId: number;
  planId: string;
  planName: string;
  status: 'ACTIVE' | 'INACTIVE' | 'CANCELLED' | 'EXPIRED';
  active: boolean;
  autoRenewal: boolean;
  startDate: string;
  endDate?: string;
  trialActive: boolean;
  trialEndDate?: string;
  nextBillingDate?: string;
  paymentHistory: PaymentRecord[];
}

export interface UsageLimitDto {
  resourceType: string;
  limit: number; // -1 for unlimited
  used: number;
  resetPeriod: 'DAILY' | 'WEEKLY' | 'MONTHLY';
  nextReset: string;
}

export interface SubscriptionUpgradeRequest {
  planId: string;
  billingCycle: 'MONTHLY' | 'YEARLY';
  autoRenewal?: boolean;
  paymentMethod?: string;
  promoCode?: string;
}

export interface PaymentRecord {
  id: number;
  amount: number;
  currency: string;
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  paymentDate: string;
  description: string;
  transactionId?: string;
}

// Cryptocurrency Ticker Types
export interface CryptoTickerSettings {
  enabled: boolean;
  selectedCoins: string[];
  exchange: string;
  animationSpeed: 'slow' | 'medium' | 'fast';
  showChangePercent: boolean;
}

export interface CryptoTickerData {
  symbol: string;
  name: string;
  price: number;
  change24h: number;
  volume24h?: number;
  exchange: string;
  timestamp: number;
  icon?: string;
  exchangeUrl?: string;
}

// Price Data Types
export interface PriceData {
  symbol: string;
  price: number;
  volume?: number;
  change24h?: number;
  changePercent24h?: number;
  high24h?: number;
  low24h?: number;
  exchange: string;
  timestamp: number;
  marketCap?: number;
}

export interface PriceStatistics {
  symbol: string;
  exchange?: string;
  currentPrice: number;
  high24h: number;
  low24h: number;
  change24h: number;
  changePercent24h: number;
  volume24h: number;
  priceHistory: PriceHistoryPoint[];
}

export interface PriceHistoryPoint {
  timestamp: number;
  price: number;
  volume?: number;
}

// WebSocket Types for Price Updates
export interface PriceUpdateEvent {
  type: 'price_update';
  symbol: string;
  data: PriceData;
}

export interface WebSocketConnectionStats {
  totalConnections: number;
  totalSubscriptions: number;
  subscriptionsBySymbol: Record<string, number>;
  uptime: number;
  messagesReceived: number;
  messagesSent: number;
}

export interface SubscriptionFeatureMatrix {
  [feature: string]: {
    free: string | number | boolean;
    premium: string | number | boolean;
  };
} 
