#!/usr/bin/env bash
# Pull the latest API image from ECR and (re)start it as a single container.
# Simple by design — one server container talking to Supabase; no compose, no
# extra services. Run on the EC2 host: ./run.sh <ecr-image-uri>
#
# On the VM you keep two files (paths overridable via env):
#   ~/.env                 — all config (incl. FCM_SERVICE_ACCOUNT_JSON=/app/service.json)
#   ~/secrets/service.json — Firebase service-account JSON (bind-mounted read-only)
set -euo pipefail

IMAGE="${1:?usage: run.sh <ecr-image-uri>}"
ENV_FILE="${ENV_FILE:-$HOME/.env}"
FCM_FILE="${FCM_FILE:-$HOME/secrets/service.json}"

docker pull "$IMAGE"
docker rm -f valleyrush-api 2>/dev/null || true
docker run -d --name valleyrush-api --restart always \
  -p 8080:8080 \
  --env-file "$ENV_FILE" \
  -v "$FCM_FILE:/app/service.json:ro" \
  "$IMAGE"
docker image prune -f
echo "valleyrush-api is running on :8080"
