FROM openjdk:17

ENV SAVE_PATH "/save_location"
WORKDIR /usr
COPY . .
WORKDIR /usr/src
RUN javac main/Main.java
CMD ["java", "main.Main"]