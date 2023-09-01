FROM maven:3-amazoncorretto-17 as develop-stage-professorat-manager
WORKDIR /app

COPY /config/ /resources/

COPY /api/gestsuite-common/ /external/
RUN mvn clean compile install -f /external/pom.xml

COPY /api/gestsuite-professorat-manager .
RUN mvn clean package -f pom.xml
ENTRYPOINT ["mvn","spring-boot:run","-f","pom.xml"]

FROM maven:3-amazoncorretto-17 as build-stage-professorat-manager
WORKDIR /resources

COPY /api/gestsuite-common/ /external/
RUN mvn clean compile install -f /external/pom.xml


COPY /api/gestsuite-professorat-manager .
RUN mvn clean package -f pom.xml

FROM amazoncorretto:17-alpine-jdk as production-stage-professorat-manager
COPY --from=build-stage-professorat-manager /resources/target/professorat-manager-0.0.1-SNAPSHOT.jar professoratmanager.jar
COPY /config/ /resources/
ENTRYPOINT ["java","-jar","/professoratmanager.jar"]
