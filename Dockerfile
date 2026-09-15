FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:25-jre

WORKDIR /app

# Install Python and required packages
RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 python3-pip \
    && rm -rf /var/lib/apt/lists/*

# Install Piper TTS
RUN pip3 install --break-system-packages --no-cache-dir piper-tts

# Download Piper voices
RUN python3 -m piper.download_voices es_ES-davefx-medium \
    && python3 -m piper.download_voices es_AR-daniela-high \
    && python3 -m piper.download_voices fr_FR-siwis-medium \
    && python3 -m piper.download_voices fr_FR-gilles-low \
    && python3 -m piper.download_voices de_DE-thorsten-medium \
    && python3 -m piper.download_voices de_DE-kerstin-low

# Copy Spring Boot JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -Dserver.address=0.0.0.0 -jar app.jar"]