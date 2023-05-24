FROM maven:3-amazoncorretto-17 as develop-stage-webiesmanacor
WORKDIR /resources

COPY /api/gestsuite-common/ /external/
RUN mvn clean compile install -f /external/pom.xml

COPY /api/gestsuite-web-iesmanacor .
RUN mvn clean package -f pom.xml
ENTRYPOINT ["mvn","spring-boot:run","-f","pom.xml"]

FROM maven:3-amazoncorretto-17 as build-stage-webiesmanacor
WORKDIR /resources

COPY /api/gestsuite-common/ /external/
RUN mvn clean compile install -f /external/pom.xml


COPY /api/gestsuite-web-iesmanacor .
RUN mvn clean package -f pom.xml

FROM amazoncorretto:17-alpine-jdk as production-stage-webiesmanacor
COPY --from=build-stage-webiesmanacor /resources/target/webiesmanacor-0.0.1-SNAPSHOT.jar webiesmanacor.jar
COPY /config/iesmanacor-e0d4f26d9c2c.json /resources/iesmanacor-e0d4f26d9c2c.json
ENTRYPOINT ["java","-jar","/webiesmanacor.jar"]