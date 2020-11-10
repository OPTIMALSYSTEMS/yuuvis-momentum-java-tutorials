FROM openjdk:8-jre-alpine

ENV SERVER_PORT=10666
EXPOSE 10666

RUN addgroup -S yuuvis && adduser -S bender -G yuuvis -h /home/bender -u 1729
USER bender

VOLUME /tmp
ADD ExampleInterceptor app.jar
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
