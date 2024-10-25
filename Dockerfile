# Use Tomcat base image with Java 11
FROM tomcat:9.0-jdk11-openjdk-slim

# Install curl, wget, and unzip
RUN apt-get update && apt-get install -y curl wget unzip

# Add ngrok repository and install ngrok
RUN curl -s https://ngrok-agent.s3.amazonaws.com/ngrok.asc | tee /etc/apt/trusted.gpg.d/ngrok.asc >/dev/null \
  && echo "deb https://ngrok-agent.s3.amazonaws.com buster main" | tee /etc/apt/sources.list.d/ngrok.list \
  && apt-get update \
  && apt-get install -y ngrok
