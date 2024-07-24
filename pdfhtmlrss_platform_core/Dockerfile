FROM pdfhtmlrss/base
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} pdfhtmlrss.jar
ENTRYPOINT ["java","-jar","/pdfhtmlrss.jar"]
WORKDIR /app
EXPOSE 8080