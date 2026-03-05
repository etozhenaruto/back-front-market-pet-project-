# BeerTestShop

Pet-проект для практики понимания апи приложений, юнит тестов и автотестов — интернет-магазин пива с REST API.

---

## 🛠 Технологии и фреймворки

**Backend:** Java 17, Spring Boot 3.2.3, Spring Security, Spring Data JPA, Spring Validation, Lombok, SpringDoc OpenAPI  
**Database:** PostgreSQL 16  
**Build & Run:** Maven, Docker, Docker Compose  
**Frontend:** HTML5, Bootstrap 5.3.2, Vanilla JavaScript

**Swagger UI:** http://localhost:8080/swagger-ui.html  
**Web:** http://localhost:8080

---

## 🚀 Запуск

### Локальный запуск (без Docker)

1. Сборка проекта:
```bash
mvn clean package -DskipTests
```

2. Запуск приложения:
```bash
mvn spring-boot:run
```
Или запустить JAR:
```bash
java -jar target/beer-test-shop-0.0.1-SNAPSHOT.jar
```

3. Проверка: http://localhost:8080/swagger-ui.html

---

### Запуск в Docker (всё приложение целиком)

1. Сборка и запуск контейнеров:
```bash
docker-compose up --build
```

2. Проверка: http://localhost:8080/swagger-ui.html

3. Остановка:
```bash
docker-compose down
```

Для удаления данных БД:
```bash
docker-compose down -v
```

---

### Запуск только БД в Docker + приложение локально

1. Запуск только PostgreSQL:
```bash
docker-compose up postgres
```

2. В отдельном терминале запустить приложение локально:
```bash
mvn spring-boot:run
```

3. Проверка: http://localhost:8080/swagger-ui.html

---

## 🧪 Юнит-тесты

Юнит-тесты покрывают все контроллеры (Product, Feedback, Cart) и сервисы приложения. Тесты проверяют успешные сценарии, валидацию данных, обработку ошибок и бизнес-логику.

**Запуск всех тестов:**
```bash
mvn test
```

**Запуск тестов по классам:**
```bash
mvn test -Dtest=ProductControllerTest
mvn test -Dtest=CartServiceTest
```

**Запуск с покрытием (JaCoCo):**
```bash
mvn clean test jacoco:report
```

**Подробная документация по юнит-тестам:** [TESTS_DOCUMENTATION.md](TESTS_DOCUMENTATION.md)

