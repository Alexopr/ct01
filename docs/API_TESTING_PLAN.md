# API Testing Plan

<div align="center">

![Testing](https://img.shields.io/badge/Testing-Critical-red)
![Coverage](https://img.shields.io/badge/Coverage-100%25-green)
![Automation](https://img.shields.io/badge/Automation-Ready-blue)

**Comprehensive API Testing Strategy для CT.01**

</div>

## 📋 Обзор плана тестирования

Этот документ содержит полный план тестирования API платформы CT.01, включая:

- **Функциональное тестирование** всех эндпоинтов
- **Тестирование безопасности** и авторизации
- **Нагрузочное тестирование** критических путей
- **Интеграционное тестирование** с внешними API
- **Автоматизированные тесты** с Postman/Newman
- **Мониторинг production** API

## 🎯 Цели тестирования

1. **Валидация функциональности** - проверка корректности работы всех эндпоинтов
2. **Безопасность** - тестирование аутентификации, авторизации и защиты данных
3. **Производительность** - проверка скорости ответа и обработки нагрузки
4. **Надежность** - стабильность работы при различных сценариях
5. **Документация** - соответствие API документации реальному поведению

## 🧪 Методология тестирования

### 1. Pyramid тестирования
```
    /\
   /E2E\     <- End-to-End Tests (5%)
  /____\
 /Integration\ <- Integration Tests (15%)
/__________\
/Unit Tests/ <- Unit Tests (80%)
```

### 2. Типы тестов
- **Unit Tests** - тестирование отдельных методов и компонентов
- **Integration Tests** - тестирование взаимодействия между компонентами
- **Contract Tests** - проверка соответствия API контрактам
- **End-to-End Tests** - полные пользовательские сценарии

### 3. Инструменты
- **JUnit 5** - unit тесты Java
- **Spring Boot Test** - интеграционные тесты
- **Postman/Newman** - API тестирование
- **JMeter** - нагрузочное тестирование
- **WireMock** - мокирование внешних сервисов

---

## 🔐 Тестирование аутентификации

### Test Case 1: Telegram авторизация
**Endpoint:** `POST /auth/telegram`

**Тест-кейсы:**
1. **Успешная авторизация**
   ```javascript
   pm.test("Successful Telegram auth", function () {
     const response = pm.response.json();
     pm.expect(response.success).to.be.true;
     pm.expect(response.data.token).to.exist;
     pm.expect(response.data.user.telegramId).to.exist;
   });
   ```

2. **Невалидные данные Telegram**
   ```javascript
   pm.test("Invalid Telegram data", function () {
     pm.expect(pm.response.code).to.be.oneOf([400, 401]);
     const response = pm.response.json();
     pm.expect(response.success).to.be.false;
   });
   ```

3. **Проверка JWT токена**
   ```javascript
   pm.test("JWT token validation", function () {
     const token = pm.response.json().data.token;
     // Decode JWT and validate structure
     const decoded = jwt.decode(token);
     pm.expect(decoded.sub).to.exist;
     pm.expect(decoded.exp).to.be.above(Date.now()/1000);
   });
   ```

### Test Case 2: Обновление токена
**Endpoint:** `POST /auth/refresh`

**Pre-conditions:** Валидный refresh token

**Тест-кейсы:**
1. Успешное обновление токена
2. Expired refresh token
3. Invalid refresh token format
4. Missing Authorization header

### Test Case 3: Выход из системы
**Endpoint:** `POST /auth/logout`

**Тест-кейсы:**
1. Успешный logout с валидным токеном
2. Logout без токена
3. Logout с expired токеном

---

## 👥 Тестирование управления пользователями

### Test Case 4: Получение профиля
**Endpoint:** `GET /users/profile`

**Автоматизированные тесты:**
```javascript
pm.test("User profile structure", function () {
  const user = pm.response.json().data;
  pm.expect(user).to.have.property('id');
  pm.expect(user).to.have.property('username');
  pm.expect(user).to.have.property('subscription');
  pm.expect(user.subscription).to.have.property('planType');
});

pm.test("User role validation", function () {
  const user = pm.response.json().data;
  pm.expect(user.roles).to.be.an('array');
  pm.expect(['USER', 'ADMIN', 'MODERATOR']).to.include.members(user.roles);
});
```

### Test Case 5: Обновление профиля
**Endpoint:** `PUT /users/profile`

**Сценарии:**
1. **Успешное обновление**
   - Valid firstName, lastName, email
   - Проверка сохранения изменений
   
2. **Валидация полей**
   - Invalid email format
   - Слишком длинные значения
   - SQL injection attempts

3. **Безопасность**
   - Попытка изменить ID другого пользователя
   - Изменение ролей через profile endpoint

---

## 💳 Тестирование системы подписок

### Test Case 6: Получение планов подписки
**Endpoint:** `GET /subscriptions/plans`

**Критерии тестирования:**
```javascript
pm.test("Subscription plans structure", function () {
  const plans = pm.response.json().data;
  pm.expect(plans).to.be.an('array');
  pm.expect(plans.length).to.be.greaterThan(0);
  
  plans.forEach(plan => {
    pm.expect(plan).to.have.property('planType');
    pm.expect(plan).to.have.property('price');
    pm.expect(plan).to.have.property('features');
    pm.expect(plan.features).to.have.property('apiCallsLimit');
  });
});
```

### Test Case 7: Статус подписки
**Endpoint:** `GET /subscriptions/status`

**Тест сценарии:**
1. **Active subscription**
   - Проверка корректности данных
   - Валидация usage limits
   
2. **Expired subscription**
   - Корректное отображение статуса
   - Restriction enforcement

3. **No subscription**
   - Default FREE plan
   - Basic limits applied

### Test Case 8: Обновление подписки
**Endpoint:** `POST /subscriptions/upgrade`

**Security Tests:**
```javascript
pm.test("Subscription upgrade security", function () {
  // Test with different user tokens
  // Verify payment processing
  // Check subscription activation
});
```

**Edge Cases:**
1. Duplicate upgrade attempts
2. Invalid payment methods
3. Downgrade scenarios
4. Concurrent upgrade requests

---

## 💰 Тестирование криптовалютных данных

### Test Case 9: Список криптовалют
**Endpoint:** `GET /coins`

**Performance Tests:**
```javascript
pm.test("Response time under 2s", function () {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});

pm.test("Pagination validation", function () {
  const data = pm.response.json().data;
  pm.expect(data.pagination.total).to.be.a('number');
  pm.expect(data.pagination.limit).to.be.a('number');
  pm.expect(data.coins.length).to.be.at.most(data.pagination.limit);
});
```

### Test Case 10: Цены криптовалют
**Endpoint:** `GET /coins/{symbol}/price`

**Real-time Data Tests:**
```javascript
pm.test("Price data freshness", function () {
  const prices = pm.response.json().data.prices;
  const now = new Date();
  
  prices.forEach(price => {
    const lastUpdate = new Date(price.lastUpdate);
    const timeDiff = now - lastUpdate;
    pm.expect(timeDiff).to.be.below(60000); // Less than 1 minute
  });
});
```

### Test Case 11: Исторические данные
**Endpoint:** `GET /coins/{symbol}/history`

**Data Integrity Tests:**
1. Chronological order validation
2. OHLCV data consistency
3. Volume data accuracy
4. Price range validation

---

## 🏛️ Тестирование бирж

### Test Case 12: Список бирж
**Endpoint:** `GET /exchanges`

**Мониторинг доступности:**
```javascript
pm.test("Active exchanges validation", function () {
  const exchanges = pm.response.json().data;
  const activeExchanges = exchanges.filter(ex => ex.isActive);
  pm.expect(activeExchanges.length).to.be.greaterThan(0);
});
```

### Test Case 13: Сравнение цен
**Endpoint:** `GET /exchanges/compare`

**Arbitrage Logic Tests:**
```javascript
pm.test("Arbitrage calculation accuracy", function () {
  const arbitrage = pm.response.json().data.arbitrage;
  
  if (arbitrage.maxProfit > 0) {
    pm.expect(arbitrage.bestBuyExchange).to.exist;
    pm.expect(arbitrage.bestSellExchange).to.exist;
    pm.expect(arbitrage.maxProfitPercent).to.be.above(0);
  }
});
```

---

## 📊 Тестирование отслеживания монет

### Test Case 14: Управление tracked coins
**Endpoints:** 
- `GET /tracked-coins`
- `POST /tracked-coins`
- `PUT /tracked-coins/{id}`
- `DELETE /tracked-coins/{id}`

**Subscription Limits Tests:**
```javascript
pm.test("Tracked coins limits enforcement", function () {
  // Test FREE plan limits
  // Test PREMIUM plan limits
  // Test limit exceeded scenarios
});
```

**CRUD Operations Tests:**
1. Create tracked coin with valid data
2. Read user's tracked coins
3. Update tracking parameters
4. Delete tracked coin
5. Authorization checks for each operation

---

## ⚙️ Тестирование настроек

### Test Case 15: Системные настройки
**Endpoint:** `GET /settings`

**Configuration Tests:**
```javascript
pm.test("Settings structure validation", function () {
  const settings = pm.response.json().data;
  pm.expect(settings).to.have.property('general');
  pm.expect(settings).to.have.property('features');
  pm.expect(settings).to.have.property('limits');
});
```

---

## 🔄 WebSocket тестирование

### Test Case 16: WebSocket соединение
**Endpoint:** `wss://ct01.herokuapp.com/ws/price-updates`

**Connection Tests:**
```javascript
// WebSocket connection test
const ws = new WebSocket('wss://ct01.herokuapp.com/ws/price-updates');

ws.onopen = function() {
  console.log('WebSocket connected');
  // Authentication test
  ws.send(JSON.stringify({
    type: 'auth',
    token: pm.environment.get('access_token')
  }));
};

ws.onmessage = function(event) {
  const data = JSON.parse(event.data);
  // Validate message structure
  // Check price update format
};
```

**Real-time Data Tests:**
1. Message format validation
2. Data freshness checks  
3. Subscription management
4. Connection stability
5. Authentication handling

---

## 🛡️ Тестирование администрирования

### Test Case 17: Админ функции
**Endpoints:**
- `GET /admin/users`
- `POST /admin/users/{id}/roles`
- `GET /admin/stats`

**Authorization Tests:**
```javascript
pm.test("Admin access control", function () {
  // Test with USER role - should fail
  // Test with ADMIN role - should succeed
  // Test with invalid token - should fail
});
```

**Data Privacy Tests:**
1. Sensitive data filtering
2. User data access controls
3. Audit logging validation

---

## 🧪 Нагрузочное тестирование

### Load Test Scenarios

#### Scenario 1: Price API Load Test
**Target:** 1000 concurrent users requesting price data

**JMeter Configuration:**
```xml
<TestPlan>
  <ThreadGroup>
    <numThreads>1000</numThreads>
    <rampTime>60</rampTime>
    <duration>300</duration>
  </ThreadGroup>
  <HTTPSampler>
    <path>/api/v1/coins/BTC/price</path>
    <method>GET</method>
  </HTTPSampler>
</TestPlan>
```

**Success Criteria:**
- 95% requests under 2 seconds
- Error rate < 1%
- No memory leaks
- Database connection pool stable

#### Scenario 2: WebSocket Connections
**Target:** 500 concurrent WebSocket connections

**Test Steps:**
1. Establish 500 WebSocket connections
2. Subscribe to price updates
3. Validate message delivery
4. Monitor resource usage

#### Scenario 3: Authentication Load
**Target:** High-frequency login attempts

**Security Validation:**
- Rate limiting effectiveness
- Token generation performance
- Session management under load

---

## 🔒 Security Testing

### Security Test Cases

#### OWASP Top 10 Validation

1. **Injection Attacks**
   ```javascript
   // SQL Injection tests
   POST /users/profile
   {
     "firstName": "'; DROP TABLE users; --",
     "email": "test@example.com"
   }
   ```

2. **Broken Authentication**
   - JWT token manipulation
   - Session fixation attacks
   - Brute force protection

3. **Sensitive Data Exposure**
   - Password field validation
   - PII data encryption
   - API response filtering

4. **XML External Entities (XXE)**
   - File upload validation
   - XML parsing security

5. **Broken Access Control**
   - Horizontal privilege escalation
   - Vertical privilege escalation
   - IDOR vulnerabilities

6. **Security Misconfiguration**
   - HTTP headers validation
   - CORS configuration
   - Error message information leakage

---

## 📈 Continuous Testing Strategy

### CI/CD Integration

#### Pre-commit Hooks
```bash
#!/bin/bash
# Run unit tests
mvn test

# Run security scan
mvn org.owasp:dependency-check-maven:check

# Run API contract tests
newman run postman_collection.json
```

#### Pipeline Stages
1. **Unit Tests** - быстрые тесты для каждого commit
2. **Integration Tests** - при merge в develop
3. **Security Tests** - еженедельно
4. **Load Tests** - перед production deployment
5. **Smoke Tests** - после production deployment

### Monitoring & Alerting

#### Production API Monitoring
```javascript
// Datadog/New Relic custom metrics
monitor('api.response.time', responseTime);
monitor('api.error.rate', errorRate);
monitor('api.throughput', requestsPerSecond);
```

#### Health Checks
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
```

---

## 📋 Test Execution Checklist

### Before Testing
- [ ] Environment setup completed
- [ ] Test data prepared
- [ ] External services mocked/configured
- [ ] Authentication tokens generated
- [ ] Database in clean state

### During Testing
- [ ] All test cases executed
- [ ] Results documented
- [ ] Failures investigated
- [ ] Performance metrics collected
- [ ] Security checks passed

### After Testing
- [ ] Test report generated
- [ ] Issues logged in tracking system
- [ ] Code coverage analyzed
- [ ] Performance baseline updated
- [ ] Documentation updated

---

## 📊 Test Reporting

### Test Results Format
```json
{
  "testRun": {
    "id": "run_20240115_001",
    "timestamp": "2024-01-15T10:30:00Z",
    "environment": "staging",
    "totalTests": 150,
    "passed": 145,
    "failed": 3,
    "skipped": 2,
    "coverage": {
      "lines": 87.5,
      "branches": 82.1,
      "functions": 91.3
    },
    "performance": {
      "averageResponseTime": 245,
      "p95ResponseTime": 890,
      "errorRate": 0.7
    }
  }
}
```

### Metrics Dashboard
- **Test Execution Trends** - pass/fail rates over time
- **Performance Trends** - response time degradation
- **Coverage Trends** - code coverage changes
- **Security Metrics** - vulnerability counts

---

## 🔧 Test Automation Tools

### Postman/Newman Scripts
```bash
# Run full API test suite
newman run CT01_API_Collection.json \
  --environment staging.json \
  --reporters html,json \
  --reporter-html-export results.html
```

### JUnit Integration Tests
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
class ApiIntegrationTest {
    
    @Test
    void testAuthenticationFlow() {
        // Test complete auth flow
    }
    
    @Test
    void testSubscriptionWorkflow() {
        // Test subscription lifecycle
    }
}
```

### Docker Test Environment
```dockerfile
# Test environment setup
FROM openjdk:17-jre-slim
COPY target/ct01-api.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app.jar", "--spring.profiles.active=test"]
```

---

## 📚 Resources & Links

- **[Postman Collection](./postman/CT01_API_Collection.json)** - полная коллекция тестов
- **[JMeter Scripts](./jmeter/)** - скрипты нагрузочного тестирования
- **[Security Checklists](./security/)** - чек-листы безопасности
- **[Test Data](./test-data/)** - тестовые данные и сценарии
- **[CI/CD Templates](./ci-cd/)** - шаблоны для автоматизации

---

<div align="center">

**[⬆️ Вернуться к началу](#api-testing-plan)**

🧪 **Quality Assurance is not a phase, it's a way of life** 🧪

</div> 