package com.ct01.shared.exception;

/**
 * Унифицированные коды ошибок для всей системы CT.01
 * Используется во всех модулях для обеспечения консистентности
 */
public enum ApiErrorCode {
    
    // ===== Общие системные ошибки =====
    INTERNAL_ERROR("SYS_001", "Внутренняя ошибка сервера"),
    VALIDATION_ERROR("SYS_002", "Ошибка валидации данных"),
    BAD_REQUEST("SYS_003", "Неверный запрос"),
    NOT_FOUND("SYS_004", "Ресурс не найден"),
    UNAUTHORIZED("SYS_005", "Не авторизован"),
    FORBIDDEN("SYS_006", "Доступ запрещен"),
    METHOD_NOT_ALLOWED("SYS_007", "Метод не поддерживается"),
    CONFLICT("SYS_008", "Конфликт данных"),
    TOO_MANY_REQUESTS("SYS_009", "Превышен лимит запросов"),
    
    // ===== Ошибки аутентификации и авторизации =====
    AUTH_INVALID_CREDENTIALS("AUTH_001", "Неверные учетные данные"),
    AUTH_TOKEN_EXPIRED("AUTH_002", "Токен доступа истек"),
    AUTH_TOKEN_INVALID("AUTH_003", "Недействительный токен"),
    AUTH_ACCESS_DENIED("AUTH_004", "Недостаточно прав доступа"),
    AUTH_SESSION_EXPIRED("AUTH_005", "Сессия истекла"),
    AUTH_CSRF_TOKEN_INVALID("AUTH_006", "Недействительный CSRF токен"),
    
    // ===== Ошибки валидации =====
    VALIDATION_FIELD_REQUIRED("VAL_001", "Обязательное поле не заполнено"),
    VALIDATION_FIELD_INVALID("VAL_002", "Недопустимое значение поля"),
    VALIDATION_EMAIL_INVALID("VAL_003", "Неверный формат email"),
    VALIDATION_PASSWORD_WEAK("VAL_004", "Пароль не соответствует требованиям"),
    VALIDATION_DATE_INVALID("VAL_005", "Неверный формат даты"),
    VALIDATION_SIZE_EXCEEDED("VAL_006", "Превышен максимальный размер"),
    
    // ===== Ошибки криптомодуля =====
    CRYPTO_COIN_NOT_FOUND("CRYPTO_001", "Криптовалюта не найдена"),
    CRYPTO_EXCHANGE_ERROR("CRYPTO_002", "Ошибка обращения к бирже"),
    CRYPTO_PRICE_NOT_AVAILABLE("CRYPTO_003", "Цена недоступна"),
    CRYPTO_TRACKING_ERROR("CRYPTO_004", "Ошибка отслеживания"),
    CRYPTO_SYMBOL_INVALID("CRYPTO_005", "Недопустимый символ криптовалюты"),
    CRYPTO_EXCHANGE_UNAVAILABLE("CRYPTO_006", "Биржа недоступна"),
    CRYPTO_RATE_LIMIT_EXCEEDED("CRYPTO_007", "Превышен лимит запросов к бирже"),
    
    // ===== Ошибки пользователей =====
    USER_NOT_FOUND("USER_001", "Пользователь не найден"),
    USER_ALREADY_EXISTS("USER_002", "Пользователь уже существует"),
    USER_BLOCKED("USER_003", "Пользователь заблокирован"),
    USER_EMAIL_TAKEN("USER_004", "Email уже используется"),
    USER_PASSWORD_INCORRECT("USER_005", "Неверный пароль"),
    
    // ===== Ошибки подписок =====
    SUBSCRIPTION_NOT_FOUND("SUB_001", "Подписка не найдена"),
    SUBSCRIPTION_EXPIRED("SUB_002", "Подписка истекла"),
    SUBSCRIPTION_LIMIT_REACHED("SUB_003", "Достигнут лимит подписки"),
    SUBSCRIPTION_ALREADY_EXISTS("SUB_004", "Подписка уже существует"),
    
    // ===== Ошибки уведомлений =====
    NOTIFICATION_SEND_FAILED("NOTIF_001", "Не удалось отправить уведомление"),
    NOTIFICATION_INVALID_CHANNEL("NOTIF_002", "Недопустимый канал уведомлений"),
    NOTIFICATION_TELEGRAM_ERROR("NOTIF_003", "Ошибка Telegram API"),
    
    // ===== Ошибки внешних сервисов =====
    EXTERNAL_SERVICE_UNAVAILABLE("EXT_001", "Внешний сервис недоступен"),
    EXTERNAL_API_ERROR("EXT_002", "Ошибка внешнего API"),
    EXTERNAL_RATE_LIMIT("EXT_003", "Превышен лимит внешнего API"),
    EXTERNAL_TIMEOUT("EXT_004", "Таймаут внешнего сервиса"),
    
    // ===== Ошибки базы данных =====
    DATABASE_CONNECTION_ERROR("DB_001", "Ошибка подключения к БД"),
    DATABASE_CONSTRAINT_VIOLATION("DB_002", "Нарушение ограничений БД"),
    DATABASE_TRANSACTION_ERROR("DB_003", "Ошибка транзакции"),
    DATABASE_TIMEOUT("DB_004", "Таймаут операции БД"),
    
    // ===== Ошибки кэша =====
    CACHE_ERROR("CACHE_001", "Ошибка кэша"),
    CACHE_UNAVAILABLE("CACHE_002", "Кэш недоступен"),
    
    // ===== Ошибки файлов и медиа =====
    FILE_NOT_FOUND("FILE_001", "Файл не найден"),
    FILE_UPLOAD_ERROR("FILE_002", "Ошибка загрузки файла"),
    FILE_SIZE_EXCEEDED("FILE_003", "Превышен размер файла"),
    FILE_FORMAT_INVALID("FILE_004", "Неподдерживаемый формат файла");
    
    private final String code;
    private final String defaultMessage;
    
    ApiErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDefaultMessage() {
        return defaultMessage;
    }
    
    @Override
    public String toString() {
        return code + ": " + defaultMessage;
    }
} 
