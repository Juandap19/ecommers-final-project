FROM maven:3.9.9-amazoncorretto-17-alpine

WORKDIR /app

COPY . /app

RUN ls -l /app
RUN ls -l /app/e2e-tests
RUN cat /app/pom.xml
RUN cat /app/e2e-tests/pom.xml

RUN mvn install -DskipTests

CMD ["mvn", "verify", "-pl", "e2e-tests", "-am"]