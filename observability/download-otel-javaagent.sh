#!/bin/sh

set -eu

VERSION="${OTEL_JAVA_AGENT_VERSION:-2.23.0}"
TARGET_DIR="$(CDPATH= cd -- "$(dirname "$0")" && pwd)/javaagent"
TARGET_FILE="$TARGET_DIR/opentelemetry-javaagent.jar"
URL="https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${VERSION}/opentelemetry-javaagent.jar"

mkdir -p "$TARGET_DIR"

echo "Downloading OpenTelemetry Java agent ${VERSION}"
echo "Source: ${URL}"
curl -L "$URL" -o "$TARGET_FILE"
echo "Saved to ${TARGET_FILE}"
