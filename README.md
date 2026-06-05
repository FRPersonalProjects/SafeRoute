# SafeRoute

Sistema de monitoramento de telemetria em tempo real para caminhões refrigerados, desenvolvido com arquitetura de microsserviços usando Spring Cloud.

Sensores nos veículos enviam continuamente dados de temperatura e localização. O sistema detecta anomalias e gera alertas quando a temperatura sai dos limites seguros (-2°C a 8°C).

---

## Sumário

- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Instalação e Execução](#instalação-e-execução)
- [Endpoints da API](#endpoints-da-api)
- [Testes](#testes)
- [Jenkins CI/CD](#jenkins-cicd)
- [Docker Hub](#docker-hub)
- [Uso de IA](#uso-de-ia)

---

## Arquitetura

```
Cliente / Simulator
        │
        ▼
  API Gateway :8080        ← ponto de entrada único, retry e roteamento
        │
        ▼
Sensor Service :8081        ← recebe telemetria, circuit breaker
        │  (Feign + Resilience4j)
        ▼
 Alert Service :8082        ← analisa temperatura e persiste alertas
        │
        ▼
   PostgreSQL :5432         ← banco de dados relacional

Infraestrutura transversal:
 Config Server :8888        ← configuração centralizada de todos os serviços
 Eureka Server :8761        ← service discovery e registro
```

**Padrões implementados:** API Gateway, Service Discovery, Centralized Config, Circuit Breaker, Retry com backoff exponencial, Database per Service.

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5 + Spring Cloud 2025 |
| Service Discovery | Netflix Eureka |
| Configuração | Spring Cloud Config |
| Gateway | Spring Cloud Gateway (Webflux) |
| Resiliência | Resilience4j (Circuit Breaker + Retry) |
| Banco de dados | PostgreSQL 15 |
| Migrations | Flyway |
| Containers | Docker (multi-stage build) |
| Orquestração local | Docker Compose |
| Orquestração produção | Kubernetes + HPA |
| CI/CD | Jenkins (em container) + Blue Ocean |
| Cobertura de testes | JaCoCo (mínimo 90%) |
| Testes | JUnit 5 + Mockito |
| Documentação API | Swagger / OpenAPI (SpringDoc) |
| Notificação | Python 3 + SMTP |

---

## Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- Git

---

## Instalação e Execução

### 1. Clonar o repositório

```bash
git clone https://github.com/<seu-usuario>/saferoute.git
cd saferoute
```

### 2. Configurar variáveis de ambiente

```bash
cp .env.example .env
```

Edite o `.env` com as credenciais do banco:

```env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=sua_senha
POSTGRES_DB=alert-service
```

### 3. Subir com Docker Compose

```bash
docker compose up -d
```

O startup é ordenado por healthchecks — aguarde ~60s para todos os serviços ficarem prontos:

```
Config Server (saudável) → Eureka (saudável) → Gateway, Sensor, Alert
PostgreSQL (saudável)    → Alert Service
```

### 4. Verificar serviços

| Serviço | URL |
|---|---|
| API Gateway | http://localhost:8080 |
| Swagger UI | http://localhost:8081/swagger-ui.html |
| Eureka Dashboard | http://localhost:8761 |
| Config Server | http://localhost:8888 |

### 5. Teste de carga (opcional)

```bash
pip install aiohttp
python simulator.py
```

O simulador envia 1.000 telemetrias assíncronas, com 5% de dados inválidos e 10% de temperaturas fora do intervalo seguro.

---

## Endpoints da API

Todos os endpoints são acessados via **API Gateway** em `http://localhost:8080`.

### POST /sensor/telemetry

Envia dados de telemetria de um caminhão.

**Request body:**
```json
{
  "truckId": "TRUCK-001",
  "temperature": 5.5,
  "latitude": -23.55,
  "longitude": -46.63,
  "timestamp": "2024-01-01T10:00:00"
}
```

| Resposta | Significado |
|---|---|
| `200 OK` | Dados processados com sucesso |
| `202 ACCEPTED` | Circuit breaker aberto — dados salvos, análise em espera |

### Limites de temperatura

| Estado | Condição |
|---|---|
| Normal | -2°C ≤ temperatura ≤ 8°C |
| Alerta | temperatura < -2°C ou temperatura > 8°C |

Alertas fora do intervalo são persistidos automaticamente no banco de dados com timestamp.

---

## Testes

### Executar testes de cada serviço

```bash
# sensor-service
cd services/sensor-service
mvn test jacoco:report

# alert-service
cd services/alert-service
mvn test jacoco:report
```

### Relatório de cobertura

Gerado em HTML após os testes:

```
services/sensor-service/target/site/jacoco/index.html
services/alert-service/target/site/jacoco/index.html
```

**Cobertura mínima configurada: 90% de linhas.** O build falha automaticamente se não atingir.

### Testes criados

| Serviço | Classe de teste | Testes |
|---|---|---|
| sensor-service | `TelemetryDTOTest` | 4 |
| sensor-service | `TelemetryServiceTest` | 3 |
| sensor-service | `TelemetryControllerTest` | 6 |
| alert-service | `TelemetryDTOTest` | 7 |
| alert-service | `AlertTest` | 8 |
| alert-service | `AlertServiceTest` | 12 |
| alert-service | `AlertControllerTest` | 4 |
| **Total** | | **44 testes** |

---

## Jenkins CI/CD

### Subir o Jenkins

```bash
cd jenkins
docker compose up -d
```

Acesse: **http://localhost:8090**

### Configurar credenciais no Jenkins

Acesse **Manage Jenkins > Credentials > System > Global credentials** e crie:

| ID | Tipo | Descrição |
|---|---|---|
| `docker-hub-credentials` | Username with password | Login Docker Hub |
| `smtp-password` | Secret text | Senha SMTP para notificações |
| `kubeconfig` | Secret file | Arquivo kubeconfig (deploy K8s) |

### Configurar variáveis de ambiente

Acesse **Manage Jenkins > Configure System > Global properties > Environment variables**:

| Variável | Exemplo | Descrição |
|---|---|---|
| `NOTIFY_EMAIL` | `equipe@email.com` | Destinatário das notificações |
| `SMTP_HOST` | `smtp.gmail.com` | Servidor SMTP |
| `SMTP_PORT` | `587` | Porta SMTP |
| `SMTP_USER` | `ci@email.com` | E-mail remetente |

> O endereço de e-mail **nunca está fixado no código** — vem sempre de variável de ambiente.

### Stages do pipeline

```
Checkout
    └── Testes & Cobertura (paralelo)
            ├── sensor-service  → junit + publishHTML + archiveArtifacts (JaCoCo)
            └── alert-service   → junit + publishHTML + archiveArtifacts (JaCoCo)
    └── Build JARs (paralelo)
            ├── config-server
            ├── eureka-server
            ├── api-gateway
            ├── sensor-service
            └── alert-service   → archiveArtifacts (JARs)
    └── Docker Build & Push     → push com tag do commit + latest
    └── Deploy Kubernetes       → apenas na branch main
    └── [post] Notificação      → scripts/notify.py via SMTP
```

---

## Docker Hub

Imagens publicadas automaticamente pelo pipeline a cada merge na `main`:

| Serviço | Imagem |
|---|---|
| Config Server | [user-name/saferoute-config-server](https://hub.docker.com/r/user-name/saferoute-config-server) |
| Eureka Server | [user-name/saferoute-eureka-server](https://hub.docker.com/r/user-name/saferoute-eureka-server) |
| API Gateway | [user-name/saferoute-api-gateway](https://hub.docker.com/r/user-name/saferoute-api-gateway) |
| Sensor Service | [user-name/saferoute-sensor-service](https://hub.docker.com/r/user-name/saferoute-sensor-service) |
| Alert Service | [user-name/saferoute-alert-service](https://hub.docker.com/r/user-name/saferoute-alert-service) |

---

## Uso de IA

O uso de IA foi amplo e declarado de forma transparente. O modelo principal utilizado foi o **Claude (Anthropic)** — Sonnet 4.5 e 4.6 — via interface Claude Code / FleetView.

### Para quê foi usado

| Área | Uso |
|---|---|
| Arquitetura | Discussão sobre monorepo vs multi-repo, padrões de microsserviços Spring Cloud |
| Docker | Revisão dos Dockerfiles, criação dos `.dockerignore`, identificação de bugs |
| Docker Compose | Adição de volumes, healthchecks, mix Dockerfile local + Docker Hub |
| Kubernetes | Revisão de manifests, PVC, Secrets, probes de readiness/liveness, memory limits |
| Jenkins | Criação do `Jenkinsfile` completo e do `jenkins/Dockerfile` |
| Testes | Criação dos 44 testes unitários (JUnit 5 + Mockito) e configuração JaCoCo |
| E-mail | Criação do `scripts/notify.py` com SMTP via variáveis de ambiente |
| README | Estrutura e conteúdo deste arquivo |

### Prompts reais utilizados

**Prompt 1 — Revisão de infraestrutura:**
> *"veja se os dockerfiles, dockercompose, gitignore, e estrutura de kubernetes estao bem fundamentadas e corretas"*
>
> Resultado aceito: IA identificou 15+ problemas (porta errada no K8s, sem volume no Postgres, credenciais hardcoded, sem probes, sem `.dockerignore`) e corrigiu todos.

**Prompt 2 — Implementação do pipeline completo:**
> *"proximos passos agora -> eu jenkins, via docker com blueocean, pipe, testes automatizados (90% de cobertura com geração de relatorio) build etc"*
>
> Resultado aceito: Jenkinsfile completo, jenkins/Dockerfile, 44 testes unitários, JaCoCo configurado em ambos os pom.xml, application.properties de teste.

**Prompt 3 — Checagem contra o PDF da disciplina:**
> *"esse é o pdf do que se pede no projeto, veja se ja contempla tudo"*
>
> Resultado aceito: IA identificou os critérios faltantes (e-mail, archiveArtifacts, mix Docker Hub no compose, README) e implementou todos.

### O que foi aceito, ajustado ou descartado

| Situação | O quê |
|---|---|
| Aceito sem alteração | Dockerfiles, manifests K8s, JaCoCo config, testes unitários, Jenkinsfile, notify.py |
| Ajustado manualmente | `.env.example` (senha simplificada), `.gitignore` (comentários removidos) |
| Descartado | Nenhuma proposta foi totalmente descartada |

### Dinâmica de uso

A IA foi utilizada em **pair programming**: o grupo revisava cada proposta, testava localmente e ajustava quando necessário. Nenhum arquivo foi copiado sem leitura e entendimento do que foi gerado.

### O que NÃO foi feito por IA

- Código de negócio dos microsserviços (controllers, services, entities, DTOs, Feign clients)
- Schema do banco de dados (migration Flyway)
- Configurações Spring Cloud (application.yml de cada serviço)
- Configurações de circuit breaker e retry (sensor-service.yml, gateway.yml)
- Simulador de carga (`simulator.py`)
- Decisões de arquitetura (Spring Cloud, Resilience4j, PostgreSQL, padrão de microsserviços)
- Commits e histórico do repositório
