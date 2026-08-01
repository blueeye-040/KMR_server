# Valley Rush API — Deploy (Docker → ECR → EC2)

CI/CD flow (`.github/workflows/deploy.yml`): on push to `main`, GitHub Actions
builds the Docker image, pushes it to **ECR**, then SSHes into **EC2** and rolls
out the new image with `docker compose`.

## 1. GitHub repo secrets (Settings → Secrets → Actions)
| Secret | Value |
|---|---|
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | IAM user allowed to push to ECR |
| `AWS_REGION` | e.g. `us-east-1` |
| `EC2_HOST` | EC2 public IP / DNS |
| `EC2_USER` | e.g. `ubuntu` or `ec2-user` |
| `EC2_SSH_KEY` | private key (PEM contents) for that user |

## 2. One-time AWS setup
```bash
# ECR repository (name must match ECR_REPOSITORY in the workflow)
aws ecr create-repository --repository-name valleyrush-api --region <AWS_REGION>
```
Attach an **IAM role to the EC2 instance** with `AmazonEC2ContainerRegistryReadOnly`
so the box can pull from ECR without storing keys.

## 3. One-time EC2 setup
```bash
# Docker + compose plugin + AWS CLI
sudo yum -y install docker awscli || sudo apt-get -y install docker.io awscli
sudo systemctl enable --now docker
sudo usermod -aG docker $USER        # re-login after this
# (install the docker compose v2 plugin if not bundled)

# App directory + config
sudo mkdir -p /opt/valleyrush/secrets
# copy these up from your machine:
#   deploy/docker-compose.prod.yml  -> /opt/valleyrush/docker-compose.prod.yml
#   your production .env             -> /opt/valleyrush/.env
#   rush-valley-*.json (FCM key)     -> /opt/valleyrush/secrets/fcm.json
```
`/opt/valleyrush/.env` holds every config value the app reads (`DATABASE_*`,
`ACCESS_KEY`, `SECRET_ACCESS_KEY`, `AWS_REGION`, `S3_BUCKET_NAME`,
`CLOUDFRONT_DOMAIN`, `RAZORPAY_*`, `PAYMENT_SPLIT_MODE`, `MAIL_FROM`, `DEV_MODE`).
`FCM_SERVICE_ACCOUNT_JSON` is set to the mounted path by the compose file — leave
it out of `.env` or it will be overridden anyway.

Open port **8080** (or put an ALB / Nginx in front for TLS on 443).

## 4. Deploy
Push to `main` → the pipeline runs automatically. Or trigger it from the Actions
tab (workflow_dispatch). To roll out by hand on the box:
```bash
cd /opt/valleyrush
aws ecr get-login-password --region <AWS_REGION> \
  | docker login --username AWS --password-stdin <acct>.dkr.ecr.<region>.amazonaws.com
export IMAGE=<acct>.dkr.ecr.<region>.amazonaws.com/valleyrush-api:latest
docker compose -f docker-compose.prod.yml pull && docker compose -f docker-compose.prod.yml up -d
```

## 5. Local image build/run (sanity check)
```bash
docker build -t valleyrush-api:local .
docker run --rm -p 8080:8080 --env-file .env \
  -v "$PWD/rush-valley-01-XXXX.json:/app/secrets/fcm.json:ro" \
  -e FCM_SERVICE_ACCOUNT_JSON=/app/secrets/fcm.json valleyrush-api:local
```
