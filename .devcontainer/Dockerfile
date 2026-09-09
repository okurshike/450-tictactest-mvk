FROM mcr.microsoft.com/devcontainers/base:alpine

RUN apk add --no-cache openjdk21-jdk

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

WORKDIR /workspace