# GitHub Secrets Quick Setup

This guide shows how to quickly set up the required GitHub Secrets for CI/CD deployment.

## Step 1: Generate or Retrieve SSH Key

### Option A: Use existing ED25519 key

You have an ED25519 key: `milkeyED.pem`

This is a strong, modern key format. Use this key for GitHub Secrets.

### Option B: Generate a new ED25519 key (if you don't have one)

```bash
# On your local machine
ssh-keygen -t ed25519 -f ec2-deploy-key -N ""

# This creates two files:
# - ec2-deploy-key (private key - store securely)
# - ec2-deploy-key.pub (public key - add to EC2)
```

**Note**: ED25519 keys are shorter and more secure than RSA. They're the modern standard for SSH authentication.

## Step 2: Add Public Key to EC2

SSH into your EC2 instance and add the public key:

```bash
# On your local machine
ssh -i your-existing-key.pem ubuntu@ec2-13-222-56-46.compute-1.amazonaws.com

# On the EC2 instance
mkdir -p ~/.ssh
cat >> ~/.ssh/authorized_keys << 'EOF'
# Paste contents of ec2-deploy-key.pub here
EOF

chmod 600 ~/.ssh/authorized_keys
```

## Step 3: Add GitHub Repository Secrets

### Using GitHub CLI (Recommended)

```bash
# Navigate to your local repository
cd ~/path/to/rabitqproducer

# Add EC2 SSH Private Key (ED25519)
gh secret set EC2_SSH_KEY < milkeyED.pem

# Add RabbitMQ Credentials
gh secret set RABBITMQ_USERNAME --body "guest"
gh secret set RABBITMQ_PASSWORD --body "your-rabbitmq-password"
gh secret set RABBITMQ_VHOST --body "/"
```

### Using GitHub Web UI

1. Go to your repository on GitHub
2. Click **Settings** (top navigation)
3. Click **Secrets and variables** → **Actions** (left sidebar)
4. Click **New repository secret**

Add each secret:

#### Secret 1: EC2_SSH_KEY
- **Name**: `EC2_SSH_KEY`
- **Value**: Contents of your ED25519 private key `milkeyED.pem`
  ```bash
  cat milkeyED.pem
  # Copy the entire output (including -----BEGIN OPENSSH PRIVATE KEY----- and END lines)
  ```

#### Secret 2: RABBITMQ_USERNAME
- **Name**: `RABBITMQ_USERNAME`
- **Value**: Your RabbitMQ username (default: `guest`)

#### Secret 3: RABBITMQ_PASSWORD
- **Name**: `RABBITMQ_PASSWORD`
- **Value**: Your RabbitMQ password (default: `guest`)

#### Secret 4: RABBITMQ_VHOST
- **Name**: `RABBITMQ_VHOST`
- **Value**: RabbitMQ virtual host (default: `/`)

## Verification

### Check Secrets are Added

```bash
# Using GitHub CLI
gh secret list

# Expected output:
# EC2_SSH_KEY
# RABBITMQ_PASSWORD
# RABBITMQ_USERNAME
# RABBITMQ_VHOST
```

### Test SSH Connection

```bash
# From your local machine using your ED25519 key
ssh -i milkeyED.pem ubuntu@ec2-13-222-56-46.compute-1.amazonaws.com

# If connection succeeds, GitHub Actions will also succeed
echo "✓ SSH connection verified"
exit
```

## Quick Checklist

- [ ] ED25519 key (`milkeyED.pem`) is available locally
- [ ] Public key added to EC2 `~/.ssh/authorized_keys`
- [ ] `EC2_SSH_KEY` secret added to GitHub (contents of milkeyED.pem)
- [ ] `RABBITMQ_USERNAME` secret added to GitHub
- [ ] `RABBITMQ_PASSWORD` secret added to GitHub
- [ ] `RABBITMQ_VHOST` secret added to GitHub
- [ ] SSH connection tested locally with `ssh -i milkeyED.pem ubuntu@ec2-13-222-56-46.compute-1.amazonaws.com`

## Troubleshooting

### SSH Connection Fails
```
Permission denied (publickey)
```
**Solution**: 
- Verify public key is in EC2 `~/.ssh/authorized_keys`
- Check permissions: `chmod 600 ~/.ssh/authorized_keys`
- Verify private key content in GitHub secret matches local file

### Secret Not Accessible in Workflow
```
Variable is not found
```
**Solution**:
- Check secret name spelling (case-sensitive)
- Verify secret is added to the correct repository
- Workflow must access secrets as: `${{ secrets.SECRET_NAME }}`

### RabbitMQ Connection Fails
```
TimeoutException
```
**Solution**:
- Verify `RABBITMQ_USERNAME` and `RABBITMQ_PASSWORD` are correct
- Check RabbitMQ host is reachable from EC2 (port 5672)
- Verify virtual host exists in RabbitMQ

## Security Notes

- **ED25519 Keys**: Shorter and more secure than RSA. Industry standard for modern SSH authentication
- **Never commit SSH keys** to git
- Keep private keys secure with restricted file permissions (600)
- Rotate SSH keys periodically
- Use strong, unique RabbitMQ passwords
- Consider using AWS Secrets Manager for production

## Next Steps

1. After secrets are configured, push to main branch:
   ```bash
   git add .
   git commit -m "Add CI/CD configuration"
   git push origin main
   ```

2. Monitor deployment in GitHub: Go to **Actions** tab in your repository

3. Check EC2 logs:
   ```bash
   ssh -i milkeyED.pem ubuntu@ec2-13-222-56-46.compute-1.amazonaws.com
   tail -f ~/rabitqproducer/app/app.log
   ```

