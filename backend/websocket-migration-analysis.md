# WebSocket Migration Analysis & Strategy
## From Legacy (alg.coyote001) to DDD Architecture (com.ct01)

---

## 1. Legacy WebSocket Implementation Analysis

### Current Architecture (alg.coyote001.websocket)

#### Core Components
1. **WebSocketConfig.java** - Spring WebSocket configuration
   - Enables WebSocket with `@EnableWebSocket`
   - Registers handler at `/ws/prices`
   - Allows all origins (development configuration)

2. **PriceWebSocketController.java** - Main WebSocket handler
   - Implements `WebSocketHandler` interface
   - Manages session lifecycle and message handling
   - Handles price update broadcasting

3. **WebSocketStatsController.java** - REST API for WebSocket management
   - Provides connection statistics
   - Offers test message broadcasting

#### WebSocket Features
- **Real-time price updates** for cryptocurrency symbols
- **Subscribe/Unsubscribe model** for selective updates
- **Session management** with concurrent collections
- **Error handling** with JSON error responses
- **Connection statistics** for monitoring
- **Ping/Pong heartbeat** for connection health

#### Message Protocol
```json
// Client → Server
{
  "action": "subscribe|unsubscribe|ping",
  "symbols": ["BTC", "ETH", "ADA"]
}

// Server → Client
{
  "type": "price_update|subscription_confirmed|error|pong",
  "symbol": "BTC",
  "data": {
    "price": 50000.0,
    "volume": 1234.56,
    "exchange": "binance",
    "timestamp": 1699123456789
  }
}
```

#### Data Flow
1. **PriceUpdateService** → fetches prices from exchanges
2. **PriceUpdateService** → saves to database
3. **PriceUpdateService** → calls `broadcastPriceUpdate()`
4. **PriceWebSocketController** → broadcasts to subscribed sessions

#### Limitations
- No authentication/authorization
- No message persistence
- Limited error recovery
- No message queuing
- Thread safety concerns with concurrent collections

---

## 2. DDD Architecture Design

### Target Package Structure
```
com.ct01.websocket/
├── domain/
│   ├── session/
│   │   ├── WebSocketSession.java          # Session aggregate
│   │   ├── SessionId.java                 # Value object
│   │   └── SessionRepository.java         # Repository interface
│   ├── subscription/
│   │   ├── Subscription.java              # Subscription aggregate
│   │   ├── SymbolSubscription.java        # Value object
│   │   └── SubscriptionRepository.java    # Repository interface
│   ├── message/
│   │   ├── WebSocketMessage.java          # Message aggregate
│   │   ├── MessageType.java               # Enum
│   │   └── MessagePayload.java            # Value object
│   └── event/
│       ├── SessionConnectedEvent.java     # Domain events
│       ├── SessionDisconnectedEvent.java
│       ├── SubscriptionAddedEvent.java
│       └── MessageSentEvent.java
├── application/
│   ├── facade/
│   │   └── WebSocketApplicationFacade.java # Main entry point
│   ├── usecase/
│   │   ├── ManageSessionUseCase.java
│   │   ├── HandleSubscriptionUseCase.java
│   │   └── BroadcastMessageUseCase.java
│   ├── command/
│   │   ├── ConnectSessionCommand.java
│   │   ├── SubscribeToSymbolCommand.java
│   │   └── BroadcastPriceCommand.java
│   └── dto/
│       ├── SessionDto.java
│       ├── SubscriptionDto.java
│       └── MessageDto.java
├── infrastructure/
│   ├── config/
│   │   └── DddWebSocketConfig.java        # Spring configuration
│   ├── handler/
│   │   └── DddWebSocketHandler.java       # Spring WebSocket handler
│   ├── persistence/
│   │   ├── SessionRepositoryImpl.java     # In-memory/Redis implementation
│   │   └── SubscriptionRepositoryImpl.java
│   └── messaging/
│       ├── MessageSerializer.java         # JSON serialization
│       └── EventPublisher.java           # Domain event publishing
└── api/
    ├── controller/
    │   └── WebSocketManagementApiController.java # REST API
    └── dto/
        ├── ApiSessionStatsDto.java
        └── ApiBroadcastRequestDto.java
```

### Domain Model Design

#### WebSocketSession Aggregate
```java
public class WebSocketSession extends AggregateRoot<SessionId> {
    private final SessionId id;
    private final UserId userId; // Optional - for authenticated sessions
    private final String clientIp;
    private final LocalDateTime connectedAt;
    private LocalDateTime lastActivityAt;
    private boolean active;
    private final Set<SymbolSubscription> subscriptions;
    
    // Business methods
    public void addSubscription(String symbol);
    public void removeSubscription(String symbol);
    public void updateLastActivity();
    public void disconnect();
    public boolean isSubscribedTo(String symbol);
}
```

#### Subscription Aggregate
```java
public class Subscription extends AggregateRoot<SubscriptionId> {
    private final SubscriptionId id;
    private final SessionId sessionId;
    private final String symbol;
    private final LocalDateTime subscribedAt;
    private boolean active;
    
    // Business methods
    public void activate();
    public void deactivate();
    public boolean isActive();
}
```

#### WebSocketMessage Value Object
```java
public record WebSocketMessage(
    MessageType type,
    String symbol,
    MessagePayload payload,
    LocalDateTime timestamp
) {
    public static WebSocketMessage priceUpdate(String symbol, PriceData data);
    public static WebSocketMessage subscriptionConfirmed(List<String> symbols);
    public static WebSocketMessage error(String message);
}
```

---

## 3. Integration with Existing DDD Components

### Crypto Domain Integration
- **TrackedCoin** already has `websocketEnabled` field
- **CryptoApplicationFacade** can trigger WebSocket broadcasts
- **PriceUpdatedEvent** (new) → triggers WebSocket broadcast

### User Domain Integration
- **User** authentication for WebSocket sessions
- **UserSecurityApplicationService** for session validation
- **UserAuthenticatedEvent** → create authenticated WebSocket session

### Notification Domain Integration
- **NotificationChannelType.WEBSOCKET** already exists
- **NotificationApplicationFacade** can send WebSocket notifications
- Real-time user notifications through WebSocket

---

## 4. Migration Strategy

### Phase 1: Infrastructure Foundation (Week 1)
**Goal:** Set up basic DDD WebSocket infrastructure

**Tasks:**
1. Create domain model (Session, Subscription, Message)
2. Implement basic repositories (in-memory)
3. Create WebSocket configuration and handler
4. Set up domain events and event handlers

**Deliverables:**
- Domain model classes
- Basic WebSocket handler with DDD integration
- Unit tests for domain logic

### Phase 2: Core WebSocket Functionality (Week 2)
**Goal:** Implement core WebSocket features with DDD patterns

**Tasks:**
1. Implement session management use cases
2. Create subscription handling logic
3. Implement message broadcasting
4. Add basic error handling

**Deliverables:**
- Complete use cases for session and subscription management
- Working WebSocket communication
- Integration tests

### Phase 3: Legacy Integration & Dual Mode (Week 3)
**Goal:** Run both legacy and DDD WebSocket side-by-side

**Tasks:**
1. Create legacy bridge adapter
2. Implement gradual migration strategy
3. Add monitoring and comparison tools
4. Ensure backward compatibility

**Deliverables:**
- Legacy integration bridge
- Monitoring dashboard
- Migration control switches

### Phase 4: Advanced Features & Security (Week 4)
**Goal:** Add authentication, authorization, and advanced features

**Tasks:**
1. Integrate with User security components
2. Add session-based authentication
3. Implement message persistence (optional)
4. Add rate limiting and abuse protection

**Deliverables:**
- Authenticated WebSocket sessions
- Security integration
- Advanced message handling

### Phase 5: Performance & Monitoring (Week 5)
**Goal:** Optimize performance and add comprehensive monitoring

**Tasks:**
1. Implement Redis-based session storage
2. Add connection pooling and scaling
3. Create comprehensive monitoring
4. Performance testing and optimization

**Deliverables:**
- Scalable session management
- Performance monitoring
- Load testing results

### Phase 6: Legacy Deprecation (Week 6)
**Goal:** Complete migration and remove legacy code

**Tasks:**
1. Switch all clients to new WebSocket endpoints
2. Remove legacy WebSocket components
3. Final testing and validation
4. Documentation updates

**Deliverables:**
- Complete legacy code removal
- Updated client documentation
- Migration completion report

---

## 5. Backward Compatibility Strategy

### Endpoint Compatibility
- **Legacy endpoint:** `/ws/prices`
- **New endpoint:** `/api/v1/websocket/prices`
- **Migration period:** Both endpoints active for 2 weeks

### Message Format Compatibility
- Maintain existing JSON message format
- Add optional fields for enhanced functionality
- Gradual deprecation warnings for old fields

### Client Migration Guide
```javascript
// Legacy connection
const ws = new WebSocket('ws://localhost:8080/ws/prices');

// New DDD connection
const ws = new WebSocket('ws://localhost:8080/api/v1/websocket/prices');

// Message format remains the same
ws.send(JSON.stringify({
    action: 'subscribe',
    symbols: ['BTC', 'ETH']
}));
```

---

## 6. Testing Strategy

### Unit Testing
- Domain model business logic
- Use case implementations
- Message serialization/deserialization
- Session and subscription management

### Integration Testing
- WebSocket connection lifecycle
- Message broadcasting functionality
- Database persistence (sessions/subscriptions)
- Event publishing and handling

### Performance Testing
- Concurrent connection limits (target: 1000+ connections)
- Message throughput (target: 1000+ messages/second)
- Memory usage under load
- Connection establishment/teardown times

### Client Testing
- JavaScript WebSocket client simulation
- Subscribe/unsubscribe scenarios
- Error handling and reconnection
- Message delivery validation

---

## 7. Risk Assessment & Mitigation

### High Risk
- **Session data loss during migration**
  - Mitigation: Implement gradual migration with session synchronization
- **Message delivery failures**
  - Mitigation: Add message acknowledgment and retry mechanisms
- **Performance degradation**
  - Mitigation: Comprehensive load testing before migration

### Medium Risk
- **Client compatibility issues**
  - Mitigation: Maintain backward compatibility and provide migration guide
- **Authentication integration complexity**
  - Mitigation: Phased security integration with fallback mechanisms

### Low Risk
- **Configuration management**
  - Mitigation: Use feature flags for gradual rollout
- **Monitoring integration**
  - Mitigation: Implement monitoring from day one

---

## 8. Success Criteria

### Functional Requirements
- ✅ All WebSocket functionality migrated to DDD architecture
- ✅ Backward compatibility maintained during transition
- ✅ No functionality loss compared to legacy system
- ✅ Integration with existing DDD domains (Crypto, User, Notification)

### Performance Requirements
- 📊 Support 1000+ concurrent WebSocket connections
- 📊 Message latency < 100ms for price updates
- 📊 99.9% message delivery success rate
- 📊 Connection establishment < 1 second

### Quality Requirements
- 🧪 90%+ test coverage for new WebSocket components
- 🔒 Proper authentication and authorization integration
- 📝 Complete API documentation with examples
- 🚀 Zero-downtime migration process

---

## 9. Implementation Roadmap

### Week 1-2: Foundation & Core Features
- Domain model implementation
- Basic WebSocket functionality
- Legacy integration bridge

### Week 3-4: Advanced Features & Security
- Authentication integration
- Message persistence
- Performance optimization

### Week 5-6: Migration & Cleanup
- Client migration
- Legacy code removal
- Final validation

### Total Timeline: 6 weeks
### Resource Requirements: 1 senior developer + 1 QA engineer
### Dependencies: Completed security migration (subtask 41.5)

---

## Next Steps

1. **Start Phase 1:** Create domain model and basic infrastructure
2. **Set up monitoring:** Implement metrics collection from day one
3. **Client communication:** Notify frontend teams about upcoming migration
4. **Testing setup:** Prepare automated testing infrastructure

---

*This document serves as the blueprint for migrating WebSocket functionality from legacy (alg.coyote001) to DDD architecture (com.ct01) while maintaining backward compatibility and ensuring zero-downtime migration.* 