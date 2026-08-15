#!/bin/sh
# Gradle start up script for Unix (EC2 / Docker).
APP_HOME=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
exec java -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
