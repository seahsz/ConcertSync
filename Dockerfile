# Build the client
FROM node:23 AS ngBuilder

WORKDIR /ngSrc

# Install Angular
RUN npm i -g @angular/cli

# Copy Angular source
COPY client/*.json .
COPY client/public public
COPY client/src src

# Run npm to install node_modules -> package.json
RUN npm ci && ng build

# Build from Spring Boot
FROM eclipse-temurin:23-jdk AS jBuilder

WORKDIR /javaSrc

COPY server/.mvn .mvn
COPY server/src src
COPY server/mvnw .
COPY server/pom.xml .

# Copy Angular files over to static
# Remove the * from browser/* because * only copies direct files, not the subdirectories!!!
COPY --from=ngBuilder /ngSrc/dist/client/browser/ src/main/resources/static/

RUN chmod a+x mvnw && ./mvnw package -Dmaven.test.skip=true

# Copy the JAR file over to the final container
FROM eclipse-temurin:23-jre

WORKDIR /myapp

# TO EDIT: Get the name of the jar file after running mvn package -Dmaven.test.skip=true
COPY --from=jBuilder /javaSrc/target/server-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8080

#Mongo
ENV SPRING_DATA_MONGODB_URI=

# MySQL
ENV SPRING_DATASOURCE_URL=
ENV SPRING_DATASOURCE_USERNAME=
ENV SPRING_DATASOURCE_PASSWORD=

# Spotify API
ENV SPOTIFY_API_CLIENT_ID=
ENV SPOTIFY_API_CLIENT_SECRET=

# To enable Multipart Form Data Processing
ENV SPRING_SERVLET_MULTIPART_ENABLED=true
ENV SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=10MB
ENV SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=10MB

# S3 Bucket
ENV DO_SPACE_KEY=
ENV DO_SPACE_SECRET=
ENV DO_SPACE_BUCKET=
ENV DO_SPACE_ENDPOINT=
ENV DO_SPACE_REGION=

# JavaMailSender
ENV SPRING_MAIL_HOST=smtp.gmail.com
ENV SPRING_MAIL_PORT=587
ENV SPRING_MAIL_USERNAME=
ENV SPRING_MAIL_PASSWORD=
ENV SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
ENV SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true

# JWT
ENV JWT_SECRET=
ENV JWT_ACCESS_EXPIRATION=2592000

# To disable Spring Security Default Behavior
ENV SPRING_AUTOCONFIGURE_EXCLUDE=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration

# Stripe payment
ENV STRIPE_API_KEY=
ENV STRIPE_WEBHOOK_SECRET=
ENV SUBSCRIPTION_PRICE_ID=
ENV STRIPE_SUCCESS_URL=
ENV STRIPE_CANCEL_URL=

# Setlist.fm API
ENV SETLIST_FM_API_KEY=

# Premium vs Non-premium
ENV PREMIUM_GROUP_LIMIT=10
ENV NON_PREMIUM_GROUP_LIMIT=2

EXPOSE ${PORT}

SHELL [ "/bin/sh", "-c" ]
ENTRYPOINT SERVERPORT=${PORT} java -jar app.jar