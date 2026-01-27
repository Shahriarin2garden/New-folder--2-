
# Mukti App

A community-driven food donation platform that bridges the gap between surplus food donors and those experiencing food insecurity.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [Profiles](#profiles)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Overview

**Mukti App** is a Spring Boot-based web application designed to eliminate food waste and fight hunger. The platform features a modern glassmorphism design with real-time chat capabilities, enabling donors to share surplus food with receivers in their community.

**Key Statistics:**
- 2.5+ Tons of food saved
- 8,500+ meals shared
- 425+ verified active donors

## Features

### Core Functionality

- **Donation Management** (`/food/donate`)
  - List surplus food with details (quantity, location, expiry time)
  - Real-time freshness tracking
  - Quick-tag categorization

- **Food Discovery** (`/food/receive-page`)
  - Localized search and filtering
  - Safety transparency (donor profiles, cook/expiry times)
  - One-click request system

- **Real-time Chat** (`/chat`)
  - WebSocket-based peer-to-peer messaging
  - Unified inbox for all communications
  - Secure coordination for pickup details

- **Impact Tracking** (`/food/leaderboard`)
  - Donor leaderboard and hall of fame
  - Personal dashboard with metrics
  - Detailed activity history

### Design Features

- Glassmorphism UI with ambient light effects
- Mouse-following dynamic lighting (250px radius)
- Responsive design with modern UX patterns
- Custom typography (Outfit, Inter, Hind Siliguri fonts)

## Technology Stack

- **Backend Framework:** Spring Boot 3.5.9
- **Java Version:** 17
- **Build Tool:** Maven 3.x
- **Database:** H2 (embedded, file-based)
- **Template Engine:** Thymeleaf
- **Real-time Communication:** WebSocket (port 8888)
- **Web Server:** Embedded Tomcat (port 8080)
- **Additional Libraries:**
  - Spring Data JPA
  - Spring Boot Validation
  - Spring Boot Mail
  - Lombok
  - Jackson (JSON processing)

## Prerequisites

Ensure you have the following installed on your system:

- **Java Development Kit (JDK) 17** or higher
  - Verify: `java -version`
- **Apache Maven 3.6+** (or use included Maven Wrapper)
  - Verify: `mvn -version`
- **Git** (for cloning the repository)
  - Verify: `git --version`

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/Shahriarin2garden/New-folder--2-.git
cd New-folder--2-/Mukti-app
```

### 2. Verify Project Structure

Ensure the following directories and files exist:
```
Mukti-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── data/               # H2 database storage (auto-created)
├── pom.xml
├── mvnw                # Maven wrapper (Linux/Mac)
└── mvnw.cmd            # Maven wrapper (Windows)
```

### 3. Install Dependencies

Using Maven:
```bash
mvn clean install
```

Or using Maven Wrapper (Linux/Mac):
```bash
./mvnw clean install
```

Or using Maven Wrapper (Windows):
```cmd
mvnw.cmd clean install
```

This will:
- Download all required dependencies
- Compile the source code
- Run unit tests
- Package the application

## Running the Application

### Method 1: Using Maven

```bash
mvn spring-boot:run
```

### Method 2: Using Maven Wrapper

**Linux/Mac:**
```bash
./mvnw spring-boot:run
```

**Windows:**
```cmd
mvnw.cmd spring-boot:run
```

### Method 3: Run as JAR

First, package the application:
```bash
mvn clean package
```

Then run the generated JAR:
```bash
java -jar target/Mukti_app-0.0.1-SNAPSHOT.jar
```

### Application Startup

Upon successful startup, you should see:
```
==============================================
   MUKTI APP IS READY ON PORT 8080
   NOTIFICATION SERVER READY ON PORT 8888
==============================================
```

### Accessing the Application

- **Main Application:** http://localhost:8080
- **H2 Database Console:** http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:file:./data/mukti-db`
  - Username: `sa`
  - Password: (leave blank)

## Configuration

### Application Properties

The application is configured via `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# H2 Database Configuration
spring.datasource.url=jdbc:h2:file:./data/mukti-db;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.h2.console.settings.web-allow-others=true

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Thymeleaf
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.mode=HTML
spring.thymeleaf.cache=false
```

### Database Notes

- The H2 database is file-based and stored in the `./data` directory
- Database file is auto-created on first run
- Data persists between application restarts
- Schema updates are automatic (`ddl-auto=update`)

### Switching to MySQL/PostgreSQL (Optional)

To use an external database, uncomment the relevant dependency in `pom.xml`:

```xml
<!-- For MySQL -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
    <scope>runtime</scope>
</dependency>
```

Then update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mukti_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

## Project Structure

```
Mukti-app/
├── src/
│   ├── main/
│   │   ├── java/NTSA/Mukti_app/
│   │   │   ├── MuktiApplication.java       # Main application class
│   │   │   ├── controller/                 # REST controllers
│   │   │   ├── model/                      # JPA entities
│   │   │   ├── repository/                 # Data access layer
│   │   │   ├── service/                    # Business logic
│   │   │   └── socket/                     # WebSocket/notification server
│   │   └── resources/
│   │       ├── application.properties      # Configuration
│   │       ├── templates/                  # Thymeleaf HTML templates
│   │       └── static/                     # CSS, JS, images
│   └── test/
│       └── java/NTSA/Mukti_app/           # Unit tests
├── data/                                   # H2 database files (auto-created)
├── pom.xml                                 # Maven configuration
├── mvnw / mvnw.cmd                        # Maven wrapper scripts
└── README.md                               # This file
```

## API Endpoints

### Main Routes

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Home page |
| `/food/donate` | GET/POST | Donation submission form |
| `/food/receive-page` | GET | Browse available donations |
| `/food/leaderboard` | GET | View donor rankings |
| `/chat` | GET | Chat interface |
| `/chat/send` | POST | Send chat message |
| `/chat/messages/{foodPostId}` | GET | Retrieve messages |
| `/h2-console` | GET | H2 database console |

### WebSocket Endpoints

- **Notification Server Port:** 8888
- Used for real-time notifications and updates

## Testing

### Run All Tests

```bash
mvn test
```

Or with Maven Wrapper:
```bash
./mvnw test
```

### Run Specific Test

```bash
mvn test -Dtest=MuktiApplicationTests
```

### Skip Tests During Build

```bash
mvn clean install -DskipTests
```

## Profiles

The application supports three Maven profiles:

### Development (Default)

```bash
mvn spring-boot:run -Pdev
```
- Active by default
- H2 console enabled
- SQL logging enabled
- Thymeleaf cache disabled

### Production

```bash
mvn spring-boot:run -Pprod
```
- Optimized for deployment
- Consider enabling security features
- Configure external database

### Testing

```bash
mvn spring-boot:run -Ptest
```
- Used for integration testing
- Separate test database configuration

## Troubleshooting

### Port Already in Use

If port 8080 or 8888 is already in use:

**Option 1:** Stop the conflicting process

**Option 2:** Change the port in `application.properties`:
```properties
server.port=9090
```

### Database Lock Issues

If you encounter database lock errors:
1. Ensure no other instances of the application are running
2. Delete the `./data` directory and restart (warning: deletes all data)
3. Check H2 console connections are closed

### Lombok Not Working

If you see compilation errors related to Lombok:

**IntelliJ IDEA:**
1. Install the Lombok plugin
2. Enable annotation processing: Settings → Build → Compiler → Annotation Processors

**Eclipse:**
1. Run `java -jar lombok.jar` and point to Eclipse installation
2. Restart Eclipse

### Maven Build Fails

Clear Maven cache and rebuild:
```bash
mvn clean install -U
```

### Application Won't Start

1. Verify Java version: `java -version` (must be 17+)
2. Check logs in the console for specific errors
3. Ensure `src/main/resources/application.properties` exists
4. Verify all dependencies downloaded: `mvn dependency:resolve`

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit changes: `git commit -am 'Add new feature'`
4. Push to branch: `git push origin feature/your-feature`
5. Submit a pull request

## License

© 2026 MUKTI APP. All rights reserved.

---

**For additional documentation:**
- [Chat Feature Documentation](CHAT_FEATURE_README.md)
- [Project Description](PROJECT_DESCRIPTION.md)

**Support:** For issues or questions, please open an issue on GitHub.
