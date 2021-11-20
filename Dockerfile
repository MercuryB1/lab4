FROM openjdk:15
WORKDIR /app/
COPY ./src/* /app/

RUN javac -encoding UTF-8 *.java

