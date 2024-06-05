FROM openjdk:11-jre-slim
VOLUME /tmp
COPY target/your-app.jar app.jar
COPY src/main/resources/application.yml /config/application.yml
COPY src/main/resources/cloud.yml /config/cloud.yml
ENTRYPOINT ["java","-Dspring.config.location=classpath:/config/application.yml,classpath:/config/cloud.yml","-jar","/app.jar"]
