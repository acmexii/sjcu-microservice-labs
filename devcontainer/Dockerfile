FROM mcr.microsoft.com/devcontainers/base:ubuntu

# 기본 패키지 설치 (init.sh에서 추가 설치할 수도 있으므로 최소만)
RUN apt-get update && apt-get install -y \
    unzip \
    curl \
    python3-pip \
    && apt-get clean
