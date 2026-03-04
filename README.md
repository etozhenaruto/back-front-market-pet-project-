# BeerTestShop

Pet-проект для практики автотестов — интернет-магазин пива с REST API.

## 📋 Требования

- **Java 17** или выше
- **Maven 3.6+**
- Интернет-соединение (для загрузки зависимостей)

## 🚀 Установка и запуск

### 1. Клонирование/копирование проекта

Убедитесь, что проект находится в папке без кириллических символов в пути (опционально, но рекомендуется).

### 2. Сборка проекта

Откройте терминал в папке проекта и выполните:

```bash
mvn clean package -DskipTests
```

### 3. Запуск приложения

```bash
mvn spring-boot:run
```

Или запустите собранный JAR-файл:

```bash
java -jar target/beer-test-shop-0.0.1-SNAPSHOT.jar
```

### 4. Проверка работы

После запуска откройте в браузере:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs:** http://localhost:8080/api-docs

## 🔐 Тестовые учётные данные

| Роль | Логин | Пароль | Права доступа |
|------|-------|--------|---------------|
| Admin | `admin` | `admin123` | Полный доступ ко всем эндпоинтам |
| User | `user` | `user123` | Доступ к корзине и публичным эндпоинтам |

## 📦 API Endpoints

### Публичные (без аутентификации)
- `GET /api/v1/products` — список всех продуктов
- `GET /api/v1/products/{id}` — продукт по ID
- `GET /api/v1/feedback` — список отзывов
- `POST /api/v1/feedback` — создать отзыв

### Требуется аутентификация (USER/ADMIN)
- `GET /api/v1/cart` — получить корзину
- `POST /api/v1/cart/add` — добавить товар в корзину
- `DELETE /api/v1/cart/remove` — удалить товар из корзины
- `DELETE /api/v1/cart/clear` — очистить корзину

### Только ADMIN
- `PUT /api/v1/products/{id}` — обновить продукт
- `DELETE /api/v1/admin/products/{id}` — удалить продукт
- `PUT /api/v1/admin/feedback/{id}` — обновить отзыв
- `DELETE /api/v1/admin/feedback/{id}` — удалить отзыв

### Аутентификация
- `POST /api/v1/auth/login` — вход в систему
- `POST /api/v1/auth/logout` — выход из системы
- `POST /api/v1/auth/me` — получить текущего пользователя

## 🛠️ Технологический стек

- **Java 17**
- **Spring Boot 3.2.3**
- **Spring Security** (Cookie-based сессия)
- **Spring Validation**
- **Lombok**
- **SpringDoc OpenAPI** (Swagger)
- **Maven**
- **In-Memory хранилище** (ConcurrentHashMap)

## 📝 Инициализация данных

При старте приложение автоматически создаёт:

### Пользователи
- `admin` / `admin123` (ROLE_ADMIN)
- `user` / `user123` (ROLE_USER)

### Продукты (5 сортов пива, по 2 шт каждого)
1. Light Lager — 150₽
2. Amber Ale — 180₽
3. India Pale Ale — 220₽
4. Dark Stout — 200₽
5. Wheat Beer — 170₽

### Отзывы (2 шт)
- Иван Петров — отзыв об IPA
- Мария Сидорова — отзыв о Wheat Beer

## ⚙️ Конфигурация

Основной файл конфигурации: `src/main/resources/application.properties`

```properties
server.port=8080
spring.application.name=BeerTestShop
logging.level.com.beertestshop=DEBUG
springdoc.swagger-ui.path=/swagger-ui.html
```


## 📌 Ограничения

- **Без базы данных** — все данные хранятся в оперативной памяти
- При перезапуске приложения все данные сбрасываются
- Лимит добавления товара в корзину: **5 шт одного товара**
- Нельзя добавить в корзину больше, чем есть на складе

## 🔧 Остановка приложения

Нажмите `Ctrl+C` в терминале, где запущено приложение.


### Страницы

| Страница | URL | Описание |
|----------|-----|----------|
| Главная | `/` | Приветствие, преимущества, навигация |
| Вход | `/login.html` | Форма аутентификации |
| Каталог | `/catalog.html` | Карточки товаров с кнопками "В корзину" |
| Корзина | `/cart.html` | Список товаров, итоги, оформление |
| Отзывы | `/feedback.html` | Форма создания + список отзывов |
| Админ | `/admin.html` | Управление товарами и отзывами |

### Тестирование UI

Все интерактивные элементы имеют `data-testid` атрибуты для автотестов:

```html
<!-- Пример -->
<input data-testid="input-username">
<input data-testid="input-password">
<button data-testid="btn-submit-login">
<button data-testid="btn-add-to-cart-{id}">
<div data-testid="feedback-list">
```

### Стек Frontend
- **HTML5**
- **Bootstrap 5.3.2** (CDN)
- **Bootstrap Icons 1.11.1**
- **Vanilla JavaScript** (Fetch API)
- **CSS3** (кастомные стили)
