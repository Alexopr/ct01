# 🔍 Анализ архитектуры и качества кода CT.01

## 🎯 Обзор

Проект настроен для анализа архитектуры и зависимостей с помощью:
- **SonarQube** - качество кода, дублирование, сложность
- **PlantUML** - диаграммы архитектуры и зависимостей

## 🚀 Быстрый запуск

### 1. SonarQube анализ

```bash
# Запуск SonarQube сервера
docker-compose -f sonar-docker-compose.yml up -d

# Ожидание готовности (30-60 сек)
# Откройте http://localhost:9000
# Логин: admin / Пароль: admin

# Создайте проект и токен в SonarQube UI
# Затем запустите анализ:
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=ct01-crypto-tracker \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=YOUR_TOKEN_HERE
```

### 2. Генерация диаграмм

```bash
# Генерация PlantUML диаграмм
mvn compile plantuml:generate

# Диаграммы сохраняются в: docs/generated/
```

## 📊 Что анализируется

### SonarQube покажет:
- **Качество кода**: 100+ классов проанализированы
- **Дублирование**: Повторяющиеся блоки кода
- **Сложность**: Сложные методы требующие рефакторинга
- **Покрытие тестами**: Текущее покрытие ~15 тестов
- **Уязвимости**: Потенциальные проблемы безопасности
- **Запахи кода**: Нарушения best practices

### PlantUML диаграммы:
- **Архитектура системы**: `docs/diagrams/architecture.puml`
- **Зависимости пакетов**: `docs/diagrams/package-dependencies.puml`

## 🔧 Текущее состояние проекта

```
📦 CT.01 Cryptocurrency Tracker
├── 🎯 Java 17 + Spring Boot 3.4.5
├── 📊 ~100+ классов
├── 🏗️ Слоевая архитектура (Controller/Service/Repository)
├── 🔒 Spring Security + Rate Limiting  
├── 💾 PostgreSQL + Redis
├── 🔄 3 Exchange адаптера (Binance/Bybit/OKX)
├── 🧪 15+ тестов (Unit + Integration)
└── 📝 OpenAPI документация
```

## 🎨 Архитектурные паттерны

### Используемые паттерны:
- **Слоевая архитектура** - четкое разделение ответственности
- **Adapter Pattern** - для интеграции с биржами
- **Dependency Injection** - Spring IoC контейнер
- **Repository Pattern** - абстракция доступа к данным

### Ключевые зависимости:
```
Controller → Service → Repository → Entity
     ↓         ↓          ↓
   DTO    Exchange    Database
          Adapters
```

## ⚡ Команды для Windows

```cmd
# Запуск полного анализа
analyze.bat

# Или пошагово:
docker-compose -f sonar-docker-compose.yml up -d
mvn clean compile
mvn sonar:sonar -Dsonar.token=YOUR_TOKEN
```

## 📈 Рекомендации

После запуска анализа обратите внимание на:

1. **Высокую сложность** в классах адаптеров
2. **Дублирование кода** в контроллерах
3. **Покрытие тестами** - увеличить с текущих 15 тестов
4. **Архитектурные правила** - controller не должен обращаться к repository напрямую

## 🛠️ Дальнейшие шаги

1. **Настройте Quality Gates** в SonarQube
2. **Добавьте ArchUnit тесты** для проверки архитектуры
3. **Интегрируйте в CI/CD** pipeline
4. **Создайте дополнительные диаграммы** для сложных модулей 