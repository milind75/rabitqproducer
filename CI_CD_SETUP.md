# CI/CD Pipeline Setup Guide

This document outlines the GitHub Actions CI/CD pipeline for deploying the RabbitMQ Producer application to EC2.

## Workflow Overview

The pipeline consists of two jobs:
1. **Build**: Compiles the code, runs tests, and creates a JAR artifact
2. **Deploy**: Deploys the JAR to the EC2 instance (runs only on `main` branch pushes)

## Trigger Events

- **Push to `main` or `develop`**: Triggers build job
- **Push to `main`**: Triggers both build and deploy jobs
- **Pull Request to `main`**: Triggers build job only (no deployment)

## Prerequisites

### 1. Generate EC2 SSH Key Pair

If you don't have an SSH key pair for your EC2 instance, use ED25519 (modern, secure standard):

```bash
# On your local machine
ssh-keygen -t ed25519 -f ec2-key -N ""

# Or use your existing key
# For this project: milkeyED.pem (ED25519 format)
```

### 2. Add SSH Key to EC2 Instance

On your EC2 instance (as `ubuntu` user):

```bash
# Add your public key to authorized_keys
cat >> ~/.ssh/authorized_keys << EOF
<paste-contents-of-ec2-key.pub>
EOF

chmod 600 ~/.ssh/authorized_keys
```

### 3. Install Java on EC2

```bash
sudo apt-get update
sudo apt-get install -y openjdk-17-jre-headless
java -version  # Verify installation
```

## GitHub Secrets Configuration

Add the following secrets to your GitHub repository:

1. **EC2_SSH_KEY** (Required)
   - Value: Contents of your EC2 private SSH key (ec2-key)
   - How to add: Settings → Secrets and Variables → Actions → New Repository Secret

2. **RABBITMQ_USERNAME** (Required)
   - Value: Your RabbitMQ username (default: `guest`)
   - Example: `guest` or your custom username

3. **RABBITMQ_PASSWORD** (Required)
   - Value: Your RabbitMQ password
   - Example: `guest` or your secure password

4. **RABBITMQ_VHOST** (Optional)
   - Value: RabbitMQ virtual host path
   - Default: `/`

### Add Secrets to GitHub

```bash
# Using GitHub CLI (recommended)
# Add your ED25519 private key
gh secret set EC2_SSH_KEY < milkeyED.pem
gh secret set RABBITMQ_USERNAME --body "guest"
gh secret set RABBITMQ_PASSWORD --body "your-password"
gh secret set RABBITMQ_VHOST --body "/"
```

Or use GitHub Web UI:
1. Go to Repository → Settings → Secrets and Variables → Actions
2. Click "New repository secret"
3. Add each secret with the values above

## Deployment Configuration

### EC2 Instance Details

- **Hostname**: `ec2-13-222-56-46.compute-1.amazonaws.com`
- **Username**: `ubuntu`
- **Application Port**: `8080` (default Spring Boot)
- **App Directory**: `~/rabitqproducer/app/` on EC2

### RabbitMQ Connection

The pipeline uses the following RabbitMQ configuration:
- **Host**: `13.222.56.46`
- **Port**: `5672` (AMQP)
- **Connection Timeout**: `5000ms`

Credentials are injected from GitHub Secrets at deployment time.

## Workflow File Location

The workflow file is located at:
```
.github/workflows/deploy.yml
```

## What Happens During Build

1. Checks out the latest code
2. Sets up Java 17 (Temurin distribution)
3. Runs Gradle tests (`./gradlew test`)
4. Builds the application (`./gradlew build -x test`)
5. Uploads the JAR artifact (retained for 5 days)

## What Happens During Deploy

1. Downloads the JAR artifact from the build job
2. Configures SSH connection to EC2
3. Creates application directory on EC2 if it doesn't exist
4. Copies the JAR file to EC2
5. Copies `application.yml` configuration to EC2
6. Stops any running instance of the application
7. Starts the application with environment variables from GitHub Secrets
8. Verifies the application is running
9. Performs a health check against the Swagger UI endpoint

## Monitoring & Logs

### View Application Logs on EC2

```bash
ssh -i ec2-key ubuntu@ec2-13-222-56-46.compute-1.amazonaws.com
tail -f ~/rabitqproducer/app/app.log
```

### Check Application Status

```bash
ssh -i ec2-key ubuntu@ec2-13-222-56-46.compute-1.amazonaws.com
ps aux | grep "java -jar"
```

### Access Application Endpoints

```bash
# Swagger UI
curl -s http://ec2-13-222-56-46.compute-1.amazonaws.com:8080/swagger-ui.html

# API Docs
curl -s http://ec2-13-222-56-46.compute-1.amazonaws.com:8080/v3/api-docs

# Test Publish Endpoint
curl -X POST http://ec2-13-222-56-46.compute-1.amazonaws.com:8080/api/rabbitmq/publish \
  -H "Content-Type: application/json" \
  -d '{"message": "Test message"}'
```

## Security Best Practices

1. **SSH Key Management**
   - Keep your EC2 private key secure
   - Use strong permissions on `~/.ssh/authorized_keys` (644 or 600)
   - Rotate keys regularly

2. **Secrets Management**
   - Never commit secrets to the repository
   - Use GitHub Secrets for all sensitive data
   - Rotate RabbitMQ credentials periodically

3. **Network Security**
   - Use security groups to restrict access to EC2
   - Limit SSH access to known IP addresses
   - Consider using AWS Systems Manager Session Manager as alternative to SSH

4. **Application Security**
   - Update dependencies regularly
   - Run security scanning in CI/CD
   - Monitor deployment logs for errors

## Troubleshooting

### SSH Connection Failed
```
Error: Permission denied (publickey)
```
**Solution**: Verify ED25519 private key (milkeyED.pem) is correctly added to GitHub Secrets and public key is in EC2 `~/.ssh/authorized_keys`

### Java Not Found
```
Error: java: command not found
```
**Solution**: Install Java 17 on EC2: `sudo apt-get install -y openjdk-17-jre-headless`

### RabbitMQ Connection Timeout
```
TimeoutException at com.rabbitmq.utility.BlockingCell
```
**Solution**: Verify RabbitMQ credentials and connection settings in GitHub Secrets

### Port 8080 Already in Use
```
Error: Address already in use
```
**Solution**: Kill the existing process: `pkill -f "java -jar"`

## Next Steps

1. Commit this workflow to your repository:
   ```bash
   git add .github/workflows/deploy.yml
   git commit -m "Add GitHub Actions CI/CD pipeline"
   git push origin main
   ```

2. Add GitHub Secrets as described above

3. Push a commit to trigger the pipeline:
   ```bash
   git add .
   git commit -m "Trigger CI/CD pipeline"
   git push origin main
   ```

4. Monitor the deployment in GitHub Actions: Repository → Actions tab

## Support

For issues or questions about the CI/CD pipeline, check:
- GitHub Actions documentation: https://docs.github.com/en/actions
- Spring Boot deployment guides
- RabbitMQ documentation

