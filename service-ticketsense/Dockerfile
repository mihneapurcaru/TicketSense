FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew
ENV GRADLE_OPTS="-Dorg.gradle.internal.http.connectionTimeout=120000 -Dorg.gradle.internal.http.socketTimeout=120000 -Dorg.gradle.daemon=false"
RUN ./gradlew dependencies --no-daemon || true
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
