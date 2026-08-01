# Valley Rush API — Deploy (Docker → ECR → EC2)

## Architecture (kept deliberately simple)
```
Flutter app ─HTTPS─▶ EC2 :8080  (one Docker container: the Spring Boot API)
                          │
                          └────▶ Supabase (managed PostgreSQL)  + AWS (S3/SES/SNS) + Razorpay + FCM
```
**One container.** No Elasticsearch (search uses Postgres full-text search in
Supabase), no Grafana, no compose. Easy to run, easy to debug. The API is
redeployed weekly; the database is managed by Supabase, so nothing else to babysit.

## CI/CD (`.github/workflows/deploy.yml`)
On push to `main`: build image → push to **ECR** → SSH to **EC2** → `docker pull`
+ `docker run` the new image. That's it.

### GitHub repo secrets
| Secret | Value |
|---|---|
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | IAM user allowed to push to ECR |
| `AWS_REGION` | e.g. `us-east-1` |
| `EC2_HOST` | EC2 public IP / DNS |
| `EC2_USER` | e.g. `ubuntu` / `ec2-user` |
| `EC2_SSH_KEY` | private key (PEM) for that user |

## One-time AWS setup
```bash
aws ecr create-repository --repository-name valleyrush-api --region <AWS_REGION>
```
Attach an IAM role to the EC2 instance with `AmazonEC2ContainerRegistryReadOnly`
so it can pull from ECR without any keys stored on the box.

## One-time EC2 setup
```bash
sudo yum -y install docker awscli || sudo apt-get -y install docker.io awscli
sudo systemctl enable --now docker
sudo usermod -aG docker $USER          # re-login after this

# Two config files in the home dir (that's all the container needs):
#   ~/.env                 — every config value the API reads
#   ~/secrets/service.json — Firebase service-account JSON
mkdir -p ~/secrets
# upload your production .env  -> ~/.env
# upload rush-valley-*.json    -> ~/secrets/service.json
```
In `~/.env`, set the FCM path to the **in-container** path (the file is bind-mounted there):
```
FCM_SERVICE_ACCOUNT_JSON=/app/service.json
```
`~/.env` also holds `DATABASE_*`, `ACCESS_KEY`, `SECRET_ACCESS_KEY`, `AWS_REGION`,
`S3_BUCKET_NAME`, `CLOUDFRONT_DOMAIN`, `RAZORPAY_*`, `PAYMENT_SPLIT_MODE`,
`MAIL_FROM`, `DEV_MODE`. Open port **8080** (or front it with Nginx/ALB for HTTPS 443).

## Deploy
Push to `main` → the pipeline deploys automatically. Manual rollout on the box:
```bash
aws ecr get-login-password --region <region> \
  | docker login --username AWS --password-stdin <acct>.dkr.ecr.<region>.amazonaws.com
./run.sh <acct>.dkr.ecr.<region>.amazonaws.com/valleyrush-api:latest
```
`deploy/run.sh` does: `docker pull` → stop old → `docker run -d --env-file ~/.env
-v ~/secrets/service.json:/app/service.json:ro`.

## Local build/run (sanity check)
```bash
docker build -t valleyrush-api:local .
docker run --rm -p 8080:8080 --env-file .env \
  -v "$PWD/rush-valley-01-XXXX.json:/app/service.json:ro" valleyrush-api:local
# with FCM_SERVICE_ACCOUNT_JSON=/app/service.json in .env
```

## Do I need Elasticsearch / Grafana?
**No.** Search is Postgres full-text (already built). Grafana is optional
monitoring the app doesn't currently export to. If you later want dashboards, run
a **separate, independent** Prometheus+Grafana stack (its own containers) — the API
doesn't need to change. Ask and it can be added without touching this deploy.
