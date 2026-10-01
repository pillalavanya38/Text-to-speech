
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:25-jre

WORKDIR /app

# Install Python
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        python3 \
        python3-pip \
    && rm -rf /var/lib/apt/lists/*

# Install Piper TTS
RUN pip3 install --break-system-packages --no-cache-dir piper-tts

# Download Piper voices used by the application

# English
RUN python3 -m piper.download_voices en_US-lessac-medium
RUN python3 -m piper.download_voices en_US-ryan-medium

# Hindi
RUN python3 -m piper.download_voices hi_IN-priyamvada-medium
RUN python3 -m piper.download_voices hi_IN-pratham-medium

# Marathi
RUN python3 -m piper.download_voices mr_IN-google-medium

# Spanish
RUN python3 -m piper.download_voices es_ES-davefx-medium

# German
RUN python3 -m piper.download_voices de_DE-kerstin-low
RUN python3 -m piper.download_voices de_DE-thorsten-medium

# French
RUN python3 -m piper.download_voices fr_FR-siwis-medium

# Copy Spring Boot JAR
COPY --from=build /app/target/*.jar app.jar

# Render provides the PORT environment variable
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -Dserver.address=0.0.0.0 -jar app.jar"]

