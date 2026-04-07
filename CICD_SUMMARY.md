# CI/CD Pipeline Summary

## Files Created

### 1. `.github/workflows/deploy.yml`
The main GitHub Actions workflow file that:
- **Build Stage**: Compiles code with Java 17, runs tests, builds JAR
- **Deploy Stage**: Deploys to EC2 automatically on `main` branch pushes
- **Triggers**: Push/PR to main/develop branches
- **Requirements**: GitHub Secrets (EC2_SSH_KEY, RABBITMQ_USERNAME, RABBITMQ_PASSWORD, RABBITMQ_VHOST)

### 2. `CI_CD_SETUP.md`
Comprehensive setup guide including:
- Workflow overview and trigger events
- Prerequisites (SSH key, Java 17)
- GitHub Secrets configuration
- Deployment configuration
- Monitoring and logging instructions
- Security best practices
- Troubleshooting guide

### 3. `GITHUB_SECRETS_SETUP.md`
Quick reference for setting up GitHub Secrets:
- SSH key generation steps
- How to add public key to EC2
- GitHub CLI or Web UI instructions
- Verification steps
- Security notes

### 4. `scripts/ec2-setup.sh`
Shell script to prepare EC2 instance:
- Updates system packages
- Installs Java 17
- Creates application directory
- Creates optional systemd service file

## Deployment Target

- **Hostname**: ec2-13-222-56-46.compute-1.amazonaws.com
- **Username**: ubuntu
- **Application Port**: 8080
- **App Directory**: ~/rabitqproducer/app/

## Workflow Triggers

| Event | Build | Deploy |
|-------|-------|--------|
| Push to `main` | ✓ | ✓ |
| Push to `develop` | ✓ | ✗ |
| Pull Request to `main` | ✓ | ✗ |

## Required GitHub Secrets

```
EC2_SSH_KEY           → Private SSH key for EC2 authentication
RABBITMQ_USERNAME     → RabbitMQ username (default: guest)
RABBITMQ_PASSWORD     → RabbitMQ password
RABBITMQ_VHOST        → RabbitMQ virtual host (default: /)
```

## Setup Checklist

- [ ] **ED25519 SSH Key Available**
  - Using: `milkeyED.pem` (ED25519 format - secure & modern)
  ```bash
  # Verify key format
  head -1 milkeyED.pem  # Should show: -----BEGIN OPENSSH PRIVATE KEY-----
  ```

- [ ] **Add Public Key to EC2**
  ```bash
  # SSH into EC2 and add public key to ~/.ssh/authorized_keys
  ```

- [ ] **Add GitHub Secrets** (See GITHUB_SECRETS_SETUP.md)
  ```bash
  gh secret set EC2_SSH_KEY < milkeyED.pem
  gh secret set RABBITMQ_USERNAME --body "guest"
  gh secret set RABBITMQ_PASSWORD --body "password"
  gh secret set RABBITMQ_VHOST --body "/"
  ```

- [ ] **Commit and Push**
  ```bash
  git add .github/workflows/deploy-ec2.yml CI_CD_SETUP.md GITHUB_SECRETS_SETUP.md
  git commit -m "Add GitHub Actions CI/CD pipeline"
  git push origin main
  ```

- [ ] **Monitor Deployment**
  - Go to: GitHub Repository → Actions tab
  - View real-time build and deployment logs

## Workflow Steps

### Build Job
1. ✓ Checkout code
2. ✓ Set up Java 17 (Temurin)
3. ✓ Run Gradle tests
4. ✓ Build JAR (skip tests)
5. ✓ Upload JAR artifact (5-day retention)

### Deploy Job (main branch only)
1. ✓ Download JAR artifact
2. ✓ Configure SSH connection to EC2
3. ✓ Create app directory on EC2
4. ✓ Copy JAR to EC2
5. ✓ Copy application.yml to EC2
6. ✓ Kill previous application instance
7. ✓ Start new application with env variables
8. ✓ Verify application is running
9. ✓ Health check via Swagger UI

## Environment Variables

The deployment injects these variables into the running application:

```
spring.application.name=rabitqproducer
spring.rabbitmq.host=13.222.56.46
spring.rabbitmq.port=5672
spring.rabbitmq.username=[from RABBITMQ_USERNAME]
spring.rabbitmq.password=[from RABBITMQ_PASSWORD]
spring.rabbitmq.virtual-host=[from RABBITMQ_VHOST]
```

## Monitoring After Deployment

### View Application Logs
```bash
ssh -i milkeyED.pem ubuntu@ec2-13-222-56-46.compute-1.amazonaws.com
tail -f ~/rabitqproducer/app/app.log
```

### Check Application Status
```bash
ssh -i milkeyED.pem ubuntu@ec2-13-222-56-46.compute-1.amazonaws.com
ps aux | grep "java -jar"
```

### Test API Endpoints
```bash
# Swagger UI
curl http://ec2-13-222-56-46.compute-1.amazonaws.com:8080/swagger-ui.html

# Publish message
curl -X POST http://ec2-13-222-56-46.compute-1.amazonaws.com:8080/api/rabbitmq/publish \
  -H "Content-Type: application/json" \
  -d '{"message": "Test"}'

# Publish with routing key
curl -X POST http://ec2-13-222-56-46.compute-1.amazonaws.com:8080/api/rabbitmq/publish-with-key \
  -H "Content-Type: application/json" \
  -d '{"key": "order.created", "message": "Test"}'
```

## Security Considerations

1. **ED25519 SSH Keys**: Modern, secure standard. Shorter and stronger than RSA 4096-bit
2. **Secrets**: All sensitive data stored in GitHub Secrets, never committed
3. **Network**: Restrict EC2 security groups to necessary ports only
4. **Credentials**: Rotate RabbitMQ passwords periodically
5. **Logs**: Monitor deployment logs for suspicious activity

## Troubleshooting

### Build Fails
- Check Java version: `java -version` on EC2
- Run locally first: `./gradlew clean test build`
- Review test failures in GitHub Actions logs

### Deployment Fails
- Verify SSH key in secrets matches EC2 authorized_keys
- Check RabbitMQ credentials
- Ensure EC2 security group allows SSH (port 22)
- Verify Java is installed on EC2

### Application Won't Start
- Check logs: `tail -f ~/rabitqproducer/app/app.log`
- Verify port 8080 is not in use: `lsof -i :8080`
- Test RabbitMQ connection from EC2

## Next Steps

1. **Immediate**: Follow the Setup Checklist above
2. **Short-term**: Test deployment with a small change
3. **Medium-term**: Add automated tests, code coverage reporting
4. **Long-term**: Add staging environment, database backups, monitoring

## Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Spring Boot Deployment](https://spring.io/guides/gs/spring-boot/)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [AWS EC2 Documentation](https://docs.aws.amazon.com/ec2/)

