## Intershop v.1.0

A simple educational online store application built with Java 21 and Spring Boot 3.5.3.
Includes a product showcase, session-based shopping cart, order management, search, sorting, and pagination — all deployed as an executable JAR using embedded Tomcat.
PostgreSQL is used as the primary database and runs in a Docker container.

## Technologies

- Java 21
- Spring Boot 3.5.3 (Spring Web, Spring Data JPA, Thymeleaf)
- Gradle (with Spring Dependency Management)
- PostgreSQL (Docker container)
- Embedded Tomcat
- ModelMapper
- JUnit 5 + Spring Boot Test + JUnit Platform Launcher

## Project Structure

- MVC layered architecture (Controller, Service, Repository)
- Thymeleaf templates for the user interface
- Session-based shopping cart with + / – / delete actions
- Product search, sorting (alphabet, price), and pagination
- Order submission and order list
- Flash messages for user feedback

## Database Setup (PostgreSQL via Docker)

- Make sure Docker is installed and running
- Start PostgreSQL using the following command:
```docker run --name intershop-postgres -e POSTGRES_DB=intershop \
  -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 -d postgres```
- Confirm PostgreSQL is accessible at localhost:5432.
- Your application.properties should be configured as:
``` spring.datasource.url=${DB_URL}
	spring.datasource.username=${DB_USERNAME}
	spring.datasource.password=${DB_PASSWORD}
	spring.jpa.hibernate.ddl-auto=update
	spring.jpa.show-sql=true```


## Build Executable JAR

- To compile and package the application:
``./gradlew clean build``
- The executable JAR will be located at: 
``build/libs/intershop-0.0.1-SNAPSHOT.jar``
- You can then run the application using:
``java -jar build/libs/intershop-0.0.1-SNAPSHOT.jar``
- The app will be available at: ``http://localhost:8080/``

## Run the Application

- You can also run the application without building the JAR:  
`` ./gradlew bootRun``

## Running Tests

This project includes unit and integration tests for: 
- Cart service and cart utility classes
- Controller layer (MainController, CartController, OrderController)
- Repository layer (with Spring Boot Test)
- To run all tests:`` ./gradlew test``

## Usage

- Open /main/items to browse the product catalog
- Use the search bar, sort dropdown, and pagination controls
- Add items to cart using “+” or “Add to Cart”
- Open the cart via /cart/items and place an order
- View submitted orders at /orders