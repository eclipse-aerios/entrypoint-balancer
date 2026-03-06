FROM openjdk:21-jdk-slim
LABEL org.opencontainers.image.authors="aerios-project"
ARG APP_VERSION

RUN addgroup -S aerioseb && adduser -S aerioseb -G aerioseb
USER aerioseb

COPY target/entrypoint-balancer-${APP_VERSION}.jar entrypoint-balancer.jar
ENTRYPOINT ["java","-jar","/entrypoint-balancer-${APP_VERSION}.jar"]