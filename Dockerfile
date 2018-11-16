FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/simple-rest-api-0.0.1-SNAPSHOT-standalone.jar /simple-rest-api/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/simple-rest-api/app.jar"]
