# --- Stage 1: Build the application ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy toàn bộ source vào container
COPY . .

# Build dự án (bỏ test cho nhanh)
RUN mvn clean package -DskipTests


# --- Stage 2: Run the application ---
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy file jar từ stage build sang
COPY --from=build /app/target/*.jar app.jar

# Render sử dụng biến PORT động
ENV PORT=8080

EXPOSE 8080

# Chạy Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
