# Build
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia o pom e baixa as dependencias (otimizacao de cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o codigo e compila o projeto
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Cria um user no-root para rodar
RUN addgroup --system spring && adduser --system --group spring
USER spring:spring

# Copia o jar gerado para a imagem final
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]