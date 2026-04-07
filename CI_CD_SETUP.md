# CI/CD Pipeline Setup Guide

This pipeline builds a Docker image, pushes it to Docker Hub, and deploys the container on EC2.

## Workflow Overview

1. **test**: Runs Gradle tests
2. **docker**: Builds Docker image and pushes to Docker Hub
3. **deploy**: Pulls latest image on EC2 and restarts container

## Trigger Events

- **Push to `master` or `develop`**: test + docker image build/push
- **Push to `master`**: deploy to EC2
- **Pull request to `master`**: test only

## Workflow File

- `.github/workflows/deploy-ec2.yml`

## Required GitHub Environment (prod)

Create environment: `prod`

### Secrets (prod)

- `EC2_SSH_KEY`: contents of `milkeyED.pem`
- `DOCKERHUB_USERNAME`: your Docker Hub username
- `DOCKERHUB_TOKEN`: Docker Hub access token (not password)
- `RABBITMQ_PASSWORD`: RabbitMQ password

### Variables (prod)

- `EC2_HOST`: `ec2-13-222-56-46`
- `RABBITMQ_USERNAME`: RabbitMQ username (for example `guest`)
- `RABBITMQ_VHOST`: RabbitMQ vhost (for example `/`)

## EC2 Prerequisites

Install Docker and allow `ubuntu` to run it.

```bash
sudo apt-get update
sudo apt-get install -y docker.io
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker ubuntu
```

Re-login after `usermod` so group membership applies.

## Deployment Behavior

On `master` push, workflow does:

1. SSH to `ubuntu@${EC2_HOST}.compute-1.amazonaws.com`
2. `docker login` to Docker Hub
3. Pull `${DOCKERHUB_USERNAME}/rabitqproducer:latest`
4. Remove old `rabitqproducer` container if present
5. Run new container with env vars and port `8080:8080`
6. Verify Swagger endpoint from EC2

## Manual Verification

```bash
ssh -i milkeyED.pem ubuntu@ec2-13-222-56-46.compute-1.amazonaws.com

docker ps --filter "name=rabitqproducer"
docker logs rabitqproducer --tail 100
curl -s http://localhost:8080/swagger-ui.html
```

## First Run Checklist

- [ ] Docker Hub repository exists (for example `youruser/rabitqproducer`)
- [ ] `prod` environment created in GitHub
- [ ] All required `prod` secrets/variables are added
- [ ] EC2 has Docker installed
- [ ] `EC2_SSH_KEY` matches EC2 authorized key pair

## Common Issues

- **Docker login fails**: verify `DOCKERHUB_TOKEN`
- **Permission denied on Docker**: re-login after adding `ubuntu` to docker group
- **Container starts then exits**: inspect `docker logs rabitqproducer`
- **RabbitMQ timeout**: verify RabbitMQ credentials and network access
