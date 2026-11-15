FROM jenkins/jenkins:lts-jdk17

USER root

# Install prerequisites for git, curl, and build tools
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        git \
        ca-certificates && \
    rm -rf /var/lib/apt/lists/*

USER jenkins
