FROM openjdk:17

ENV SAVE_PATH "/save_location"
WORKDIR /usr/src
COPY src/* .
RUN javac main/Main.java
CMD ["java", "main.Main"]