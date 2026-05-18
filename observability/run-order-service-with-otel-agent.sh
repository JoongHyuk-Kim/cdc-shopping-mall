#!/bin/sh

set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)"
SERVICE_DIR="$ROOT_DIR/order-service"
AGENT_JAR="$ROOT_DIR/observability/javaagent/opentelemetry-javaagent.jar"
JAVA_HOME_21="$(/usr/libexec/java_home -v 21)"

if [ ! -f "$AGENT_JAR" ]; then
  echo "OpenTelemetry Java agent not found: $AGENT_JAR"
  echo "Run: sh observability/download-otel-javaagent.sh"
  exit 1
fi

cd "$SERVICE_DIR"
export JAVA_HOME="$JAVA_HOME_21"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew bootJar

APP_JAR="$(find build/libs -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' | head -n 1)"

if [ -z "$APP_JAR" ]; then
  echo "Boot jar not found under $SERVICE_DIR/build/libs"
  exit 1
fi

exec java \
  -javaagent:"$AGENT_JAR" \
  -Dotel.service.name=order-service \
  -Dotel.exporter.otlp.endpoint=http://localhost:4318 \
  -Dotel.exporter.otlp.protocol=http/protobuf \
  -Dotel.metrics.exporter=none \
  -Dotel.logs.exporter=none \
  -Dotel.resource.attributes=deployment.environment=local \
  -jar "$APP_JAR"
