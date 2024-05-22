FROM gradle:8.7.0-jdk17
COPY . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build
EXPOSE 8081
ENTRYPOINT ["java","-jar","/home/gradle/src/build/libs/usersManager-0.0.1-SNAPSHOT.jar"]