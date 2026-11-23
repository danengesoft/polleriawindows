FROM eclipse-temurin:21-jdk-jammy@sha256:742daacb9316be4b6fc60258f6e4ac2a11882f6c597373c92290a57bfad0d2e0 AS build
WORKDIR /build
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src src
# Caché de Maven entre compilaciones: Maven y las librerías se descargan solo la primera vez.
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy@sha256:affcab5356e1270ac35d2361ee83135af2f03e8d580eca78a6d5d6fdbf048db0
RUN groupadd --system polleria && useradd --system --gid polleria --home-dir /app polleria \
    && apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
RUN mkdir -p /app/imagenes-productos && chown polleria:polleria /app/imagenes-productos
WORKDIR /app
COPY --from=build --chown=polleria:polleria /build/target/polleria-backend-0.0.1-SNAPSHOT.jar app.jar
USER polleria
ENV SPRING_PROFILES_ACTIVE=prod TZ=America/Lima
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
