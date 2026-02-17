# Build stage
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

COPY core core
COPY api api
COPY module-account module-account
COPY module-coupon module-coupon
COPY module-cart module-cart
COPY module-order module-order
COPY module-product module-product

RUN chmod +x gradlew
RUN ./gradlew :api:bootJar -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/api/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
