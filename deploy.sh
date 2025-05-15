#!/bin/bash

# Usage: ./deploy.sh green | blue
set -e

TARGET="$1"
OTHER=""

if [ "$TARGET" == "green" ]; then
  OTHER="blue"
elif [ "$TARGET" == "blue" ]; then
  OTHER="green"
else
  echo "Usage: $0 [green|blue]"
  exit 1
fi

echo "🚀 Deploying $TARGET version..."
docker compose up -d review-service-$TARGET

echo "🛑 Stopping $OTHER version..."
docker compose stop review-service-$OTHER

echo "🔄 Updating Nginx config..."
cp nginx-$TARGET.conf nginx.conf

echo "🔁 Reloading Nginx..."
docker compose exec nginx nginx -s reload

echo "✅ Switched to $TARGET deployment."