FROM openjdk:8
#VOLUME /tmp
ARG JAR_FILE
#COPY ${JAR_FILE} app.jar
COPY build/libs/*.jar /app/
ENTRYPOINT ["java","-jar","/app/pdfhtmlrss_platform-0.0.1-SNAPSHOT-plain.jar"]
#WORKDIR /app
EXPOSE 80