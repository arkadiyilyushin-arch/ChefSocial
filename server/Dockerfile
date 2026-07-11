FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY server ./server
RUN chmod +x gradlew && ./gradlew :server:installDist --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/server/build/install/server /app
RUN mkdir -p /app/data/uploads
VOLUME ["/app/data"]
EXPOSE 8080
CMD ["/app/bin/server"]
