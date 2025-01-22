FROM gradle:7-jdk11 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle ./settings.gradle.kts settings.gradle.kts
COPY --chown=gradle:gradle ./xmlrss/build.gradle xmlrss/build.gradle
COPY --chown=gradle:gradle ./pdfhtmlrss_platform_core/build.gradle.kts pdfhtmlrss_platform_core/build.gradle.kts
#RUN gradle build --refresh-dependencies --no-daemon
COPY --chown=gradle:gradle ./xmlrss/src/main xmlrss/src/main
COPY --chown=gradle:gradle ./pdfhtmlrss_platform_core/src/main pdfhtmlrss_platform_core/src/main
RUN gradle build -x test --no-daemon

#FROM ubuntu:24.04
FROM openjdk:11-jre-slim
RUN apt-get update && apt-get install -y \
    poppler-utils \
    tidy
#    openjdk-11-jre-headless \
ARG JAR_FILE=/home/gradle/src/pdfhtmlrss_platform_core/build/libs/*.jar
COPY --from=build ${JAR_FILE} pdfhtmlrss.jar
ENTRYPOINT ["java","-jar","/pdfhtmlrss.jar"]
WORKDIR /app
EXPOSE 8080