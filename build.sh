#!/bin/bash
set -e

echo "=== Building Angular Frontend ==="
cd frontend
npm ci
npm run build -- --configuration production

echo "=== Copying to Spring Boot static ==="
rm -rf ../src/main/resources/static/*
cp -r dist/frontend/browser/* ../src/main/resources/static/

echo "=== Building Spring Boot Backend ==="
cd ..
mvn clean package -DskipTests -q

echo "=== Build Complete ==="
