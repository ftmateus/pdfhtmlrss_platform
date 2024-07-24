FROM ubuntu
RUN apt-get update && apt-get install -y \
    openjdk-11-jre-headless  \
    poppler-utils
#FROM alpine
#RUN apk update && apk add \
#    shadow \
#    bash \
#    openjdk11-jre \
#    poppler-utils
#RUN chsh -s /bin/bash
