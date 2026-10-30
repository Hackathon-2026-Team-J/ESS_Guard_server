FROM eclipse-temurin:21-jre-alpine
ARG JAR_NAME=ess-sentinel-0.0.1-SNAPSHOT.jar
COPY ./build/libs/${JAR_NAME} app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
