FROM gradle:jdk8
VOLUME /tmp
VOLUME /app
COPY . /app
WORKDIR /app
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false"
#ENTRYPOINT ["gradle", "build", "--no-daemon"]