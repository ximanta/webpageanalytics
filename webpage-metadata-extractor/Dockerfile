FROM java:8-jre

ADD ./target/account-service.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/webpage-metadata-extractor.jar"]

EXPOSE 6000