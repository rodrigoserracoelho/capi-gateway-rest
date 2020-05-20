FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/capi-gateway-rest-${env.VERSION}.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
