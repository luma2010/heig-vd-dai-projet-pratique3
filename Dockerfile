FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/heig-vd-dai-projet-pratique3-1.0-SNAPSHOT.jar heig-vd-dai-projet-pratique3-1.0-SNAPSHOT.jar
COPY src/main/resources/data.json /app/data.json

ENTRYPOINT ["java","-jar","/app/heig-vd-dai-projet-pratique3-1.0-SNAPSHOT.jar"]
