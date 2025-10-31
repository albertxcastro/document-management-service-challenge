# Stage 1: Build the native executable
FROM ubuntu:22.04 as builder

# Install required dependencies
RUN apt-get update && apt-get install -y curl build-essential libz-dev

# Set environment variables
ENV GRAALVM_HOME=/usr/lib/jvm/graalvm
ENV MAVEN_HOME=/usr/share/maven
ENV PATH="${GRAALVM_HOME}/bin:${MAVEN_HOME}/bin:${PATH}"

# Install GraalVM 21 (compatible with Java 17 source)
RUN curl -L -o graalvm.tar.gz https://download.oracle.com/graalvm/21/archive/graalvm-jdk-21.0.3_linux-x64_bin.tar.gz && \
    mkdir -p ${GRAALVM_HOME} && \
    tar -xzf graalvm.tar.gz -C ${GRAALVM_HOME} --strip-components=1

# Install Maven
RUN curl -L -o maven.tar.gz https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz && \
    mkdir -p ${MAVEN_HOME} && \
    tar -xzf maven.tar.gz -C ${MAVEN_HOME} --strip-components=1

WORKDIR /workspace

# Copy pom.xml and download dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline

# Copy source code
COPY src src

# Build native executable (remove unsupported flags)
RUN mvn -Pnative -DskipTests \
    -Dnative-image.build-args="--gc=serial --no-fallback --enable-url-protocols=http,https --report-unsupported-elements-at-runtime --initialize-at-build-time=ch.qos.logback,org.slf4j" \
    package

# ------------------------
# Stage 2: Final minimal image
# ------------------------
FROM debian:12-slim

# Install runtime dependency for zlib
RUN apt-get update && apt-get install -y zlib1g && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the native executable
COPY --from=builder /workspace/target/document-management-service-challenge .

# Expose application port
EXPOSE 8080

# Create non-root user and switch to it
RUN useradd -ms /bin/bash nonroot
USER nonroot

# Set entrypoint
ENTRYPOINT ["./document-management-service-challenge"]
