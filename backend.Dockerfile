FROM gradle:7-jdk11 as build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle ./settings.gradle.kts settings.gradle.kts
COPY --chown=gradle:gradle ./xmlrss/build.gradle xmlrss/build.gradle
COPY --chown=gradle:gradle ./xmlrss/src xmlrss/src
COPY --chown=gradle:gradle ./pdfhtmlrss_platform_core/build.gradle.kts pdfhtmlrss_platform_core/build.gradle.kts
COPY --chown=gradle:gradle ./pdfhtmlrss_platform_core/src pdfhtmlrss_platform_core/src
RUN gradle clean build -x test --no-daemon

FROM openjdk:11-jre-slim
RUN apt-get update && apt-get install -y \
    poppler-utils
#ARG JAR_FILE=build/libs/*.jar
ARG JAR_FILE=/home/gradle/src/pdfhtmlrss_platform_core/build/libs/*.jar
#COPY ${JAR_FILE} pdfhtmlrss.jar
COPY --from=build ${JAR_FILE} pdfhtmlrss.jar
ENTRYPOINT ["java","-jar","/pdfhtmlrss.jar"]
WORKDIR /app
EXPOSE 8080