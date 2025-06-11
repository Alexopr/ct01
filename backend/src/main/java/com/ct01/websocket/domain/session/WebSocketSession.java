package com.ct01.websocket.domain.session;

import com.ct01.shared.domain.AggregateRoot;
import com.ct01.user.domain.UserId;
import com.ct01.websocket.domain.subscription.SymbolSubscription;
import com.ct01.websocket.domain.event.SessionConnectedEvent;
import com.ct01.websocket.domain.event.SessionDisconnectedEvent;
import com.ct01.websocket.domain.event.SubscriptionAddedEvent;
import com.ct01.websocket.domain.event.SubscriptionRemovedEvent;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WebSocket Session aggregate root
 * Управляет жизненным циклом WebSocket сессии и её подписками
 */
public class WebSocketSession extends AggregateRoot<SessionId> {
    
    private final SessionId id;
    private final UserId userId; // Optional - для аутентифицированных сессий
    private final String clientIp;
    private final LocalDateTime connectedAt;
    private LocalDateTime lastActivityAt;
    private boolean active;
    private final Set<SymbolSubscription> subscriptions;
    
    // Приватный конструктор для создания через фабричные методы
    private WebSocketSession(SessionId id, UserId userId, String clientIp, 
                           LocalDateTime connectedAt, boolean active) {
        super(id);
        this.id = id;
        this.userId = userId;
        this.clientIp = clientIp;
        this.connectedAt = connectedAt;
        this.lastActivityAt = connectedAt;
        this.active = active;
        this.subscriptions = new HashSet<>();
    }
    
    /**
     * Создать новую аутентифицированную сессию
     */
    public static WebSocketSession createAuthenticated(SessionId sessionId, UserId userId, String clientIp) {
        WebSocketSession session = new WebSocketSession(sessionId, userId, clientIp, LocalDateTime.now(), true);
        session.addDomainEvent(new SessionConnectedEvent(sessionId, userId, clientIp, true));
        return session;
    }
    
    /**
     * Создать новую анонимную сессию
     */
    public static WebSocketSession createAnonymous(SessionId sessionId, String clientIp) {
        WebSocketSession session = new WebSocketSession(sessionId, null, clientIp, LocalDateTime.now(), true);
        session.addDomainEvent(new SessionConnectedEvent(sessionId, null, clientIp, false));
        return session;
    }
    
    /**
     * Восстановить сессию из репозитория
     */
    public static WebSocketSession restore(SessionId id, UserId userId, String clientIp, 
                                         LocalDateTime connectedAt, LocalDateTime lastActivityAt, 
                                         boolean active, Set<SymbolSubscription> subscriptions) {
        WebSocketSession session = new WebSocketSession(id, userId, clientIp, connectedAt, active);
        session.lastActivityAt = lastActivityAt;
        session.subscriptions.addAll(subscriptions);
        return session;
    }
    
    /**
     * Добавить подписку на символ
     */
    public void addSubscription(String symbol) {
        if (!active) {
            throw new IllegalStateException("Cannot add subscription to inactive session");
        }
        
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        
        updateLastActivity();
        
        SymbolSubscription newSubscription = SymbolSubscription.create(symbol);
        
        // Удаляем старую подписку если есть и добавляем новую
        subscriptions.removeIf(sub -> sub.getSymbol().equals(newSubscription.getSymbol()));
        subscriptions.add(newSubscription);
        
        addDomainEvent(new SubscriptionAddedEvent(id, symbol, LocalDateTime.now()));
    }
    
    /**
     * Удалить подписку на символ
     */
    public void removeSubscription(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return;
        }
        
        updateLastActivity();
        
        String normalizedSymbol = symbol.toUpperCase().trim();
        boolean removed = subscriptions.removeIf(sub -> sub.getSymbol().equals(normalizedSymbol));
        
        if (removed) {
            addDomainEvent(new SubscriptionRemovedEvent(id, normalizedSymbol, LocalDateTime.now()));
        }
    }
    
    /**
     * Проверить подписку на символ
     */
    public boolean isSubscribedTo(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return false;
        }
        
        String normalizedSymbol = symbol.toUpperCase().trim();
        return subscriptions.stream()
                .anyMatch(sub -> sub.isActive() && sub.getSymbol().equals(normalizedSymbol));
    }
    
    /**
     * Получить все активные подписки
     */
    public Set<String> getActiveSubscriptions() {
        return subscriptions.stream()
                .filter(SymbolSubscription::isActive)
                .map(SymbolSubscription::getSymbol)
                .collect(Collectors.toSet());
    }
    
    /**
     * Обновить время последней активности
     */
    public void updateLastActivity() {
        if (active) {
            this.lastActivityAt = LocalDateTime.now();
        }
    }
    
    /**
     * Отключить сессию
     */
    public void disconnect() {
        if (active) {
            this.active = false;
            this.lastActivityAt = LocalDateTime.now();
            
            // Деактивируем все подписки
            this.subscriptions.replaceAll(SymbolSubscription::deactivate);
            
            addDomainEvent(new SessionDisconnectedEvent(id, userId, LocalDateTime.now(), 
                                                      subscriptions.size()));
        }
    }
    
    /**
     * Проверить является ли сессия аутентифицированной
     */
    public boolean isAuthenticated() {
        return userId != null;
    }
    
    /**
     * Проверить активность сессии
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Проверить не истек ли timeout сессии
     */
    public boolean isExpired(long timeoutMinutes) {
        if (!active) {
            return true;
        }
        
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
        return lastActivityAt.isBefore(timeoutThreshold);
    }
    
    /**
     * Получить количество активных подписок
     */
    public int getActiveSubscriptionCount() {
        return (int) subscriptions.stream()
                .filter(SymbolSubscription::isActive)
                .count();
    }
    
    // Getters
    @Override
    public SessionId getId() {
        return id;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public String getClientIp() {
        return clientIp;
    }
    
    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }
    
    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }
    
    public Set<SymbolSubscription> getSubscriptions() {
        return Collections.unmodifiableSet(subscriptions);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebSocketSession that = (WebSocketSession) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("WebSocketSession{id=%s, userId=%s, clientIp='%s', active=%s, subscriptions=%d}", 
                           id, userId, clientIp, active, subscriptions.size());
    }
} 