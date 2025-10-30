# Stage 1: Build the native executable
FROM paketobuildpacks/builder-jammy-base:latest as builder

WORKDIR /workspace

# Copy Maven wrapper and pom.xml to leverage Docker cache
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of the application source code
COPY src src

# Build the native executable
# The -DskipTests flag is added to avoid running tests during the build
RUN ./mvnw -Pnative -DskipTests package

# Stage 2: Create the final, minimal image
FROM gcr.io/distroless/cc-static

# Add a non-root user
RUN adduser --system --disabled-password appuser

WORKDIR /app

# Copy the native executable from the builder stage
COPY --from=builder /workspace/target/document-management-service-challenge .

# Expose the application port
EXPOSE 8080

# Run as the non-root user
USER appuser

# Set the entrypoint
ENTRYPOINT ["./document-management-service-challenge"]
