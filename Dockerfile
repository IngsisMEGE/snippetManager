FROM gradle:8.7.0-jdk17 AS build

WORKDIR /home/gradle/src

COPY build.gradle settings.gradle gradle/ ./

COPY src ./src

COPY .editorconfig ./

COPY fakeEnv .env

WORKDIR /home/gradle/src

RUN gradle build


EXPOSE ${PORT}

ENTRYPOINT ["java","-jar","/home/gradle/src/build/libs/snippetManager-0.0.1-SNAPSHOT.jar"]