# Docker 镜像构建
# @author xing
FROM maven:3.6.1-jdk-8-slim as builder

# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY xingbi-backend-0.0.1-SNAPSHOT.jar .

# Build a release artifact.
# RUN mvn package -DskipTests

# Run the web service on container startup.
CMD ["java","-jar","xingbi-backend-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]