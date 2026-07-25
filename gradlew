#!/usr/bin/env sh
set -eu

BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROPERTIES_FILE="$BASE_DIR/gradle/wrapper/gradle-wrapper.properties"

if [ ! -f "$PROPERTIES_FILE" ]; then
  echo "Missing Gradle wrapper properties: $PROPERTIES_FILE" >&2
  exit 1
fi

DISTRIBUTION_URL=$(sed -n 's/^distributionUrl=//p' "$PROPERTIES_FILE" | sed 's#\\:#:#g')
if [ -z "$DISTRIBUTION_URL" ]; then
  echo "distributionUrl is not configured in $PROPERTIES_FILE" >&2
  exit 1
fi

ARCHIVE_NAME=$(basename "$DISTRIBUTION_URL")
GRADLE_VERSION=$(printf '%s' "$ARCHIVE_NAME" | sed -n 's/^gradle-\(.*\)-bin\.zip$/\1/p')
if [ -z "$GRADLE_VERSION" ]; then
  echo "Unsupported Gradle distribution archive: $ARCHIVE_NAME" >&2
  exit 1
fi

DIST_DIR="$BASE_DIR/.gradle/wrapper/dists/gradle-$GRADLE_VERSION-bin"
GRADLE_HOME="$DIST_DIR/gradle-$GRADLE_VERSION"
ZIP="$DIST_DIR/$ARCHIVE_NAME"

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  mkdir -p "$DIST_DIR"
  if [ ! -f "$ZIP" ]; then
    curl -L --fail --retry 3 --retry-delay 2 -o "$ZIP" "$DISTRIBUTION_URL"
  fi
  unzip -q -o "$ZIP" -d "$DIST_DIR"
fi

exec "$GRADLE_HOME/bin/gradle" "$@"
