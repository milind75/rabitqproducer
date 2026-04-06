# RabbitMQ Producer API

A Spring Boot REST API for publishing messages to RabbitMQ with automated CI/CD deployment to AWS EC2 using GitHub Actions.

## 📋 Overview

This project provides a simple, scalable REST API for producing/publishing messages to RabbitMQ queues. It includes:

- ✅ Two REST endpoints for message publishing
- ✅ Swagger UI for interactive API testing
- ✅ Automated CI/CD pipeline using GitHub Actions
- ✅ Secure EC2 deployment with ED25519 SSH key
- ✅ Comprehensive setup documentation

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Gradle
- RabbitMQ server (host: `13.222.56.46:5672`)

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/milind75/rabitqproducer.git
   cd rabitqproducer
   ```

2. **Configure RabbitMQ credentials** (optional for local development)
   ```bash
   export RABBITMQ_USERNAME=guest
   export RABBITMQ_PASSWORD=guest
   export RABBITMQ_VHOST=/
   ```

3. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

4. **Access Swagger UI**
   - Open: `http://localhost:8080/swagger-ui.html`

## 📡 API Endpoints

### 1. Publish to Default Queue
```bash
POST /api/rabbitmq/publish

Content-Type: application/json

{
  "message": "Your message here"
}
```

**Response (200 OK):**
```json
{
  "status": "Message published",
  "queue": "producer.queue"
}
```

### 2. Publish with Routing Key
```bash
POST /api/rabbitmq/publish-with-key

Content-Type: application/json

{
  "key": "order.created",
  "message": "Your message here"
}
```

**Response (200 OK):**
```json
{
  "status": "Message published",
  "routingKey": "order.created"
}
```

### Error Responses

**400 Bad Request:** Invalid or missing message/key
```json
{
  "error": "message must not be blank"
}
```

**503 Service Unavailable:** RabbitMQ connection failed
```json
{
  "error": "Failed to publish message to RabbitMQ",
  "routingKey": "order.created",
  "details": "TimeoutException"
}
```

## 🏗️ Project Structure

```
rabitqproducer/
├── .github/
│   └── workflows/
│       └── deploy.yml              # GitHub Actions CI/CD pipeline
├── src/
│   ├── main/
│   │   ├── java/com/order/queue/rabitq/
│   │   │   ├── RabitqproducerApplication.java
│   │   │   ├── controller/
│   │   │   │   └── RabbitMqController.java
│   │   │   └── config/
│   │   │       └── OpenApiConfig.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/com/order/queue/rabitq/
│           └── RabitqproducerApplicationTests.java
├── scripts/
│   └── ec2-setup.sh                # EC2 preparation script
├── build.gradle
├── CI_CD_SETUP.md                  # Comprehensive CI/CD guide
├── GITHUB_SECRETS_SETUP.md         # GitHub Secrets configuration
└── CICD_SUMMARY.md                 # Quick reference guide
```

## ⚙️ Configuration

### application.yml

```yaml
spring:
  application:
    name: rabitqproducer
  rabbitmq:
    host: 13.222.56.46
    port: 5672
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    virtual-host: ${RABBITMQ_VHOST:/}
    connection-timeout: 5000

app:
  rabbitmq:
    queue: producer.queue

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

## 🔄 CI/CD Pipeline

The GitHub Actions workflow automatically:

1. **Build**: Compiles code, runs tests, creates JAR
2. **Deploy**: Deploys to EC2 on master branch pushes

### Triggers

| Event | Build | Deploy |
|-------|-------|--------|
| Push to `master` | ✅ | ✅ |
| Push to `develop` | ✅ | ❌ |
| Pull Request to `master` | ✅ | ❌ |

## 🔐 GitHub Actions Setup

### 1. Add GitHub Secrets

```bash
gh secret set EC2_SSH_KEY < milkeyED.pem
gh secret set RABBITMQ_USERNAME --body "guest"
gh secret set RABBITMQ_PASSWORD --body "your-password"
gh secret set RABBITMQ_VHOST --body "/"
```

### 2. Or Use GitHub Web UI

1. Go to: Repository → Settings → Secrets and Variables → Actions
2. Add these secrets:
   - `EC2_SSH_KEY`: ED25519 private key content
   - `RABBITMQ_USERNAME`: RabbitMQ username
   - `RABBITMQ_PASSWORD`: RabbitMQ password
   - `RABBITMQ_VHOST`: RabbitMQ virtual host

See [GITHUB_SECRETS_SETUP.md](./GITHUB_SECRETS_SETUP.md) for detailed instructions.

## 🖥️ EC2 Deployment

### Prerequisites on EC2

```bash
# SSH into EC2
ssh -i milkeyED.pem ubuntu@ec2-13-222-56-46.compute-1.amazonaws.com

# Run setup script
bash -c "$(curl -fsSL https://raw.githubusercontent.com/milind75/rabitqproducer/master/scripts/ec2-setup.sh)"
```

### Manual Setup

1. Install Java 17:
   ```bash
   sudo apt-get update
   sudo apt-get install -y openjdk-17-jre-headless
   ```

2. Create app directory:
   ```bash
   mkdir -p ~/rabitqproducer/app
   ```

### Access Deployed Application

```bash
# Swagger UI
curl http://ec2-13-222-56-46.compute-1.amazonaws.com:8080/swagger-ui.html

# Test endpoint
curl -X POST http://ec2-13-222-56-46.compute-1.amazonaws.com:8080/api/rabbitmq/publish \
  -H "Content-Type: application/json" \
  -d '{"message": "Test message"}'
```

## 📊 Monitoring

### View Logs

```bash
ssh -i milkeyED.pem ubuntu@ec2-13-222-56-46.compute-1.amazonaws.com
tail -f ~/rabitqproducer/app/app.log
```

### Check Application Status

```bash
ssh -i milkeyED.pem ubuntu@ec2-13-222-56-46.compute-1.amazonaws.com
ps aux | grep "java -jar"
```

## 🔨 Build & Test

### Run Tests Locally

```bash
./gradlew test
```

### Build JAR

```bash
./gradlew build
```

The JAR will be created at: `build/libs/rabitqproducer-*.jar`

### Run Application Locally

```bash
./gradlew bootRun
```

## 📚 Documentation

- [CI_CD_SETUP.md](./CI_CD_SETUP.md) - Comprehensive CI/CD pipeline guide
- [GITHUB_SECRETS_SETUP.md](./GITHUB_SECRETS_SETUP.md) - GitHub Secrets configuration
- [CICD_SUMMARY.md](./CICD_SUMMARY.md) - Quick reference and checklist

## 🔑 Technologies Used

- **Spring Boot 4.0.5** - Framework
- **Spring AMQP** - RabbitMQ integration
- **Springdoc OpenAPI** - Swagger UI and OpenAPI documentation
- **Gradle** - Build tool
- **GitHub Actions** - CI/CD pipeline
- **Docker** - (Optional) containerization

## 📋 Requirements

- Java 17 or higher
- Gradle 7+
- RabbitMQ 3.8+
- Git

## 🐛 Troubleshooting

### RabbitMQ Connection Timeout

```
java.util.concurrent.TimeoutException at com.rabbitmq.utility.BlockingCell
```

**Solutions:**
1. Verify RabbitMQ is accessible at `13.222.56.46:5672`
2. Check credentials in GitHub Secrets
3. Verify network connectivity: `Test-NetConnection 13.222.56.46 -Port 5672`

### SSH Connection Failed

```
Permission denied (publickey)
```

**Solutions:**
1. Verify ED25519 key content in GitHub Secret `EC2_SSH_KEY`
2. Verify public key is in EC2 `~/.ssh/authorized_keys`
3. Check key permissions: `chmod 600 ~/.ssh/authorized_keys`

### Application Won't Start

Check logs:
```bash
tail -f ~/rabitqproducer/app/app.log
```

## 🤝 Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -am 'Add feature'`
3. Push to branch: `git push origin feature/your-feature`
4. Create Pull Request to `master`

## 📝 License

This project is open source and available under the MIT License.

## 👤 Author

**Milind**
- GitHub: [@milind75](https://github.com/milind75)

## 📧 Support

For issues, questions, or suggestions, please open an issue on GitHub.

---

**Last Updated:** April 2026

