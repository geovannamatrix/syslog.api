FROM amazoncorretto:21-alpine3.18-jdk AS build
WORKDIR /build
RUN apk add --no-cache maven

COPY pom.xml ./
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -Dmaven.test.skip=true package
RUN mv ./target/*.jar ./application.jar

FROM amazoncorretto:21-alpine3.18 AS runner
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=build /build/application.jar ./application.jar
EXPOSE 8080
CMD ["sh", "-c", "java -Dspring.profiles.active=${ENV:-dev} -jar application.jar"]