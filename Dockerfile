# Use Amazon Linux with Java 17 pre-installed (no authentication needed)
FROM amazonlinux:2023

# Install Java 17 and Maven
RUN yum update -y && \
    yum install -y java-17-amazon-corretto-devel maven && \
    yum clean all

# Set working directory
WORKDIR /app

# Copy Maven configuration file first (for better caching)
COPY pom.xml .

# Download dependencies (cached if pom.xml unchanged)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "target/spring-boot-sql-crud-app-0.0.1-SNAPSHOT.jar"]