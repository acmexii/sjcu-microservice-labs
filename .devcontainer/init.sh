#!/bin/bash
set -e

# 기본 유틸 설치
sudo apt-get update
sudo apt-get install -y net-tools iputils-ping unzip curl python3-pip pipx
pipx ensurepath

# httpie 설치
pipx install httpie
sudo apt install siege

curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "../awscliv2.zip"
unzip -o ../awscliv2.zip
sudo ./aws/install 

export EKSCTL_VERSION=v0.130.0
curl --silent --location "https://github.com/weaveworks/eksctl/releases/download/$EKSCTL_VERSION/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin

cd kafka
docker-compose up
