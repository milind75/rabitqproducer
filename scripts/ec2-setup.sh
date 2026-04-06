#!/bin/bash

# EC2 Setup Script for RabbitMQ Producer Application
# This script prepares the EC2 instance for the GitHub Actions deployment

set -e

echo "==================================="
echo "EC2 Setup for RabbitMQ Producer"
echo "==================================="

# Update system packages
echo ""
echo "Step 1: Updating system packages..."
sudo apt-get update
sudo apt-get upgrade -y

# Install Java 17
echo ""
echo "Step 2: Installing Java 17..."
sudo apt-get install -y openjdk-17-jre-headless
java -version

# Create application directory
echo ""
echo "Step 3: Creating application directory..."
mkdir -p ~/rabitqproducer/app
cd ~/rabitqproducer/app

# Create systemd service file (optional, for automatic restart)
echo ""
echo "Step 4: Creating systemd service (optional)..."

sudo tee /etc/systemd/system/rabitqproducer.service > /dev/null << EOF
[Unit]
Description=RabbitMQ Producer Application
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/rabitqproducer/app
ExecStart=/usr/bin/java -jar /home/ubuntu/rabitqproducer/app/rabitqproducer-*.jar
Restart=on-failure
RestartSec=10
StandardOutput=append:/home/ubuntu/rabitqproducer/app/app.log
StandardError=append:/home/ubuntu/rabitqproducer/app/app.log

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload

echo ""
echo "==================================="
echo "✓ EC2 Setup Complete!"
echo "==================================="
echo ""
echo "Next steps:"
echo "1. Add your public SSH key to ~/.ssh/authorized_keys"
echo "2. Configure GitHub Secrets in your repository"
echo "3. Push to main branch to trigger deployment"
echo ""
echo "Useful commands:"
echo "  - View logs: tail -f ~/rabitqproducer/app/app.log"
echo "  - Check status: ps aux | grep java"
echo "  - Use systemd: sudo systemctl start rabitqproducer"
echo ""

