FROM jenkins/jenkins:lts-jdk21

# Instala plugins via jenkins-plugin-cli (incluido na imagem lts)
RUN jenkins-plugin-cli --plugins \
    blueocean \
    docker-workflow \
    docker-plugin \
    git \
    workflow-aggregator \
    pipeline-stage-view \
    credentials-binding \
    jacoco \
    html-publisher \
    junit \
    timestamper \
    ws-cleanup

USER root

# Instala Docker CLI, Python3 e pip (necessarios para o pipeline)
RUN apt-get update && apt-get install -y \
    docker.io \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

# Instala kubectl
RUN curl -LO "https://dl.k8s.io/release/$(curl -Ls https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" \
    && chmod +x kubectl && mv kubectl /usr/local/bin/kubectl

USER jenkins
