package alg.coyote001.config;

import java.time.Duration;

/**
 * Centralized application constants
 * All hardcoded values should be moved here for better maintainability
 */
public final class AppConstants {
    
    private AppConstants() {
        // Utility class - no instantiation
    }
    
    // ===== API Configuration =====
    
    public static final class Api {
        public static final String VERSION = "v1";
        public static final String BASE_PATH = "/api/" + VERSION;
        
        // Rate Limiting
        public static final int DEFAULT_RATE_LIMIT_REQUESTS = 100;
        public static final int DEFAULT_RATE_LIMIT_WINDOW_SECONDS = 60;
        
        // Pagination
        public static final int DEFAULT_PAGE_SIZE = 20;
        public static final int MAX_PAGE_SIZE = 100;
        
        // Request timeouts
        public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);
        public static final Duration LONG_REQUEST_TIMEOUT = Duration.ofMinutes(2);
    }
    
    // ===== Cache Configuration =====
    
    public static final class Cache {
        // Cache names
        public static final String TICKER_DATA = "ticker_data";
        public static final String PRICE_HISTORY = "price_history";
        public static final String EXCHANGE_CONFIG = "exchange_config";
        public static final String USER_SESSIONS = "user_sessions";
        public static final String RATE_LIMITS = "rate_limits";
        
        // TTL values
        public static final Duration TICKER_TTL = Duration.ofMinutes(1);
        public static final Duration PRICE_HISTORY_TTL = Duration.ofMinutes(5);
        public static final Duration EXCHANGE_CONFIG_TTL = Duration.ofHours(1);
        public static final Duration USER_SESSION_TTL = Duration.ofHours(24);
        public static final Duration RATE_LIMIT_TTL = Duration.ofMinutes(1);
        
        // Cache sizes
        public static final int DEFAULT_MAX_SIZE = 1000;
        public static final int LARGE_CACHE_MAX_SIZE = 10000;
    }
    
    // ===== Exchange Configuration =====
    
    public static final class Exchanges {
        public static final class Binance {
            public static final String NAME = "BINANCE";
            public static final String BASE_URL = "https://api.binance.com";
            public static final int MAX_REQUESTS_PER_MINUTE = 1200;
            public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
        }
        
        public static final class Bybit {
            public static final String NAME = "BYBIT";
            public static final String BASE_URL = "https://api.bybit.com";
            public static final int MAX_REQUESTS_PER_MINUTE = 600;
            public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
        }
        
        public static final class Okx {
            public static final String NAME = "OKX";
            public static final String BASE_URL = "https://www.okx.com";
            public static final int MAX_REQUESTS_PER_MINUTE = 300;
            public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
        }
        
        // Common exchange settings
        public static final Duration DEFAULT_BACKOFF_DURATION = Duration.ofMinutes(1);
        public static final int MAX_RETRY_ATTEMPTS = 3;
        public static final Duration RETRY_DELAY = Duration.ofSeconds(5);
    }
    
    // ===== Security Configuration =====
    
    public static final class Security {
        // JWT
        public static final Duration JWT_EXPIRATION = Duration.ofHours(24);
        public static final Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(30);
        
        // Password
        public static final int BCRYPT_STRENGTH = 12;
        public static final int MIN_PASSWORD_LENGTH = 8;
        
        // Session
        public static final Duration SESSION_TIMEOUT = Duration.ofHours(2);
        public static final int MAX_SESSIONS_PER_USER = 5;
        
        // CORS
        public static final String[] ALLOWED_ORIGINS = {
            "http://localhost:3000",
            "http://localhost:5173",
            "https://crypto-dashboard.com"
        };
        
        public static final String[] ALLOWED_METHODS = {
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        };
        
        public static final String[] ALLOWED_HEADERS = {
            "Authorization", "Content-Type", "X-Requested-With", 
            "Accept", "Origin", "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        };
    }
    
    // ===== Database Configuration =====
    
    public static final class Database {
        // Connection pool
        public static final int MIN_POOL_SIZE = 5;
        public static final int MAX_POOL_SIZE = 20;
        public static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(30);
        public static final Duration IDLE_TIMEOUT = Duration.ofMinutes(10);
        
        // Query limits
        public static final int MAX_QUERY_RESULTS = 10000;
        public static final Duration QUERY_TIMEOUT = Duration.ofSeconds(30);
        
        // Batch sizes
        public static final int DEFAULT_BATCH_SIZE = 100;
        public static final int LARGE_BATCH_SIZE = 1000;
    }
    
    // ===== File and Storage =====
    
    public static final class Storage {
        // File sizes
        public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
        public static final long MAX_TOTAL_SIZE = 100 * 1024 * 1024; // 100MB
        
        // Supported file types
        public static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/webp"
        };
        
        public static final String[] ALLOWED_DOCUMENT_TYPES = {
            "application/pdf", "text/plain", "application/json"
        };
        
        // Paths
        public static final String UPLOAD_PATH = "/uploads";
        public static final String TEMP_PATH = "/temp";
        public static final String BACKUP_PATH = "/backups";
    }
    
    // ===== Monitoring and Logging =====
    
    public static final class Monitoring {
        // Metrics collection intervals
        public static final Duration METRICS_COLLECTION_INTERVAL = Duration.ofMinutes(1);
        public static final Duration HEALTH_CHECK_INTERVAL = Duration.ofMinutes(5);
        
        // Alert thresholds
        public static final double CPU_ALERT_THRESHOLD = 0.8; // 80%
        public static final double MEMORY_ALERT_THRESHOLD = 0.9; // 90%
        public static final double ERROR_RATE_THRESHOLD = 0.05; // 5%
        
        // Retention periods
        public static final Duration METRICS_RETENTION = Duration.ofDays(30);
        public static final Duration LOGS_RETENTION = Duration.ofDays(7);
        public static final Duration AUDIT_LOGS_RETENTION = Duration.ofDays(90);
    }
    
    // ===== Business Logic =====
    
    public static final class Business {
        // Price data
        public static final int PRICE_DECIMAL_PLACES = 8;
        public static final int VOLUME_DECIMAL_PLACES = 4;
        public static final int PERCENTAGE_DECIMAL_PLACES = 2;
        
        // Data freshness
        public static final Duration STALE_DATA_THRESHOLD = Duration.ofMinutes(5);
        public static final Duration VERY_STALE_DATA_THRESHOLD = Duration.ofMinutes(15);
        
        // Analysis periods
        public static final int[] CHART_PERIODS_HOURS = {1, 4, 12, 24, 168, 720}; // 1h, 4h, 12h, 1d, 1w, 1m
        public static final int DEFAULT_HISTORY_DAYS = 30;
        public static final int MAX_HISTORY_DAYS = 365;
        
        // Limits
        public static final int MAX_WATCHLIST_ITEMS = 100;
        public static final int MAX_ALERTS_PER_USER = 50;
        public static final int MAX_API_KEYS_PER_USER = 5;
    }
    
    // ===== Error Codes =====
    
    public static final class ErrorCodes {
        // Authentication & Authorization
        public static final String INVALID_CREDENTIALS = "AUTH_001";
        public static final String TOKEN_EXPIRED = "AUTH_002";
        public static final String INSUFFICIENT_PERMISSIONS = "AUTH_003";
        public static final String USER_DISABLED = "AUTH_004";
        
        // Data Validation
        public static final String INVALID_INPUT = "VAL_001";
        public static final String MISSING_REQUIRED_FIELD = "VAL_002";
        public static final String INVALID_FORMAT = "VAL_003";
        public static final String VALUE_OUT_OF_RANGE = "VAL_004";
        
        // Business Logic
        public static final String RESOURCE_NOT_FOUND = "BIZ_001";
        public static final String DUPLICATE_RESOURCE = "BIZ_002";
        public static final String OPERATION_NOT_ALLOWED = "BIZ_003";
        public static final String QUOTA_EXCEEDED = "BIZ_004";
        
        // External Services
        public static final String EXTERNAL_SERVICE_ERROR = "EXT_001";
        public static final String RATE_LIMIT_EXCEEDED = "EXT_002";
        public static final String SERVICE_UNAVAILABLE = "EXT_003";
        
        // System Errors
        public static final String INTERNAL_ERROR = "SYS_001";
        public static final String DATABASE_ERROR = "SYS_002";
        public static final String CACHE_ERROR = "SYS_003";
        public static final String CONFIGURATION_ERROR = "SYS_004";
    }
    
    // ===== Feature Flags =====
    
    public static final class FeatureFlags {
        public static final String ENABLE_WEBSOCKET = "enable.websocket";
        public static final String ENABLE_CACHING = "enable.caching";
        public static final String ENABLE_RATE_LIMITING = "enable.rate.limiting";
        public static final String ENABLE_METRICS = "enable.metrics";
        public static final String ENABLE_AUDIT_LOGGING = "enable.audit.logging";
        public static final String ENABLE_EXTERNAL_APIS = "enable.external.apis";
    }
} 