#!/bin/sh
# Minimal launcher; CI installs Gradle when wrapper JAR is absent.
exec gradle "$@"
