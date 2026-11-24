#!/bin/sh
# Which parts changed between the previous release tag and this one?
# Usage: scripts/changed-parts.sh <tag>   → prints backend=true|false and frontend=true|false
# Compared with the previous release tag (not the last commit), because a release
# contains every commit since the last release, not just the last one.
set -eu
tag=$1

prev=$(git describe --tags --abbrev=0 --match 'v*.*.*' "$tag^" 2>/dev/null || true)
if [ -z "$prev" ]; then
  # First release: nothing is deployed yet, so everything is new
  echo "previous=none" >&2
  echo "backend=true"
  echo "frontend=true"
  exit 0
fi
echo "previous=$prev" >&2

files=$(git diff --name-only "$prev" "$tag")

# Backend = everything that ends up in the jar. build.gradle counts, but not the
# version line: it changes in every release and would make every release a backend release.
backend=false
if echo "$files" | grep -qE '^(src/main/|settings\.gradle$|gradle/|gradlew)'; then
  backend=true
elif git diff "$prev" "$tag" -- build.gradle | grep -E '^[+-][^+-]' | grep -qvE "^[+-]version = "; then
  backend=true
fi

frontend=false
if echo "$files" | grep -q '^frontend/'; then
  frontend=true
fi

# Everything else (tests, docs, infra/, .github/, .devcontainer/) changes neither the jar nor the frontend
echo "backend=$backend"
echo "frontend=$frontend"
