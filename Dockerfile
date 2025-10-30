# Stage 1: Build the native executable in a self-contained environment
FROM ubuntu:22.04 as builder

# Install required dependencies
RUN apt-get update && apt-get install -y curl build-essential libz-dev

# Set environment variables
ENV GRAALVM_HOME=/usr/lib/jvm/graalvm
ENV MAVEN_HOME=/usr/share/maven
ENV PATH="${GRAALVM_HOME}/bin:${MAVEN_HOME}/bin:${PATH}"

# Install GraalVM
RUN curl -L -o graalvm.tar.gz https://download.oracle.com/graalvm/17/archive/graalvm-jdk-17.0.12_linux-x64_bin.tar.gz && \
    mkdir -p ${GRAALVM_HOME} && \
    tar -xzf graalvm.tar.gz -C ${GRAALVM_HOME} --strip-components=1

# Install Maven
RUN curl -L -o maven.tar.gz https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz && \
    mkdir -p ${MAVEN_HOME} && \
    tar -xzf maven.tar.gz -C ${MAVEN_HOME} --strip-components=1

WORKDIR /workspace

COPY pom.xml ./

# Download dependencies
RUN mvn dependency:go-offline

# Copy the rest of the application source code
COPY src src

# Build the native executable
RUN mvn -Pnative -DskipTests -Dnative-image.build-args="--optimize=size" package

# Stage 2: Create the final, minimal image
FROM gcr.io/distroless/base-debian12:nonroot

# Install zlib dependency
RUN apt-get update && apt-get install -y zlib1g && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the native executable from the builder stage
COPY --from=builder /workspace/target/document-management-service-challenge .

# Expose the application port
EXPOSE 8080

# Create a non-root user and switch to it
RUN useradd -ms /bin/bash nonroot
USER nonroot

# Set the entrypoint
ENTRYPOINT ["./document-management-service-challenge"]
