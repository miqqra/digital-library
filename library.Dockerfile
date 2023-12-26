FROM openjdk:17-alpine

COPY digital-library/target/digital-library-1.0.jar /library.jar

CMD ["java", "-jar", "/library.jar"]