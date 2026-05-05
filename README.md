# ShopSmart-E-Commerce-Spring-Boot


commands
```bash
curl https://start.spring.io/starter.zip `
  -d dependencies=web,security,data-jpa,h2 `
  -d type=maven-project `
  -d language=java `
  -d baseDir=shopsmart `
  -d groupId=com.shopsmart `
  -d artifactId=shopsmart `
  -o shopsmart.zip
```

# 🛒 ShopSmart E-Commerce (Spring Boot)

A backend-focused e-commerce application built using Spring Boot, Spring Security, and JPA with an in-memory H2 database.

---

## 🚀 Features

- Product CRUD operations (Create, Read, Update, Delete)
- REST APIs for product management
- Simple UI using Thymeleaf
- Random product generator API
- Input validation (only alphabets for product name)
- Global exception handling
- H2 in-memory database integration

---

## 🧱 Tech Stack

- Java 22
- Spring Boot 4.x
- Spring Web
- Spring Data JPA
- Spring Security
- H2 Database
- Thymeleaf

---

## 📌 API Endpoints

### Products API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /products | Get all products |
| GET | /products/{id} | Get product by ID |
| POST | /products | Create product |
| PUT | /products/{id} | Update product |
| DELETE | /products/{id} | Delete product |
| GET | /products/random | Generate 10 random products |

---

## 🎯 Validation Rules

- Product name: Only alphabets (A-Z, a-z)
- Price: Must be greater than 0

---

## 🖥 UI

Access UI at:
[http://localhost:8080](http://localhost:8080)


---

## 🧪 Run Project

```bash
./mvnw spring-boot:run
```

---

📂 Database

H2 Console:

http://localhost:8080/h2-console

JDBC URL:

jdbc:h2:mem:testdb

👨‍💻 Author

Built as a learning project to understand Spring Boot backend development.