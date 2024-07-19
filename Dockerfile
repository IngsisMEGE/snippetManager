FROM gradle:8.7.0-jdk17 AS build

WORKDIR /home/gradle/src

COPY build.gradle settings.gradle gradle/ ./

COPY src ./src

COPY .editorconfig ./
ARG NEW_RELIC_LICENSE_KEY

ENV NEW_RELIC_LICENSE_KEY=$NEW_RELIC_LICENSE_KEY

COPY fakeEnv .env

WORKDIR /home/gradle/src

RUN gradle build


EXPOSE ${PORT}

COPY newrelic/newrelic.jar /app/newrelic.jar

ENTRYPOINT ["java","-jar","-javaagent:/app/newrelic.jar", "-Dnewrelic.config.license_key=${NEW_RELIC_LICENSE_KEY}","/home/gradle/src/build/libs/snippetManager-0.0.1-SNAPSHOT.jar"]