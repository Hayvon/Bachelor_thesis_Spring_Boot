FROM openjdk:8
WORKDIR /home/app/spring
COPY target/Bachelorarbeit_Spring_Boot-1.0.0-SNAPSHOT.jar /home/app/spring/build/app.jar
#RUN java -jar build/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","build/app.jar"]