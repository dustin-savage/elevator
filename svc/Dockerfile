FROM ubuntu:22.04

RUN apt update && \
    apt install -y curl java-common apt-utils && \
    curl -LO "https://corretto.aws/downloads/latest/amazon-corretto-17-x64-linux-jdk.deb" && \
    dpkg --install amazon-corretto-17*.deb

ARG JAR_FILE=target/*.jar
ENV JAVA_OPTS=""

COPY pom.xml entrypoint.sh ./
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["bash", "entrypoint.sh"]
