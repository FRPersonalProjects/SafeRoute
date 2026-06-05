pipeline {
    agent any

    environment {
        // Credencial criada em: Manage Jenkins > Credentials
        DOCKER_CREDENTIALS = credentials('docker-hub-credentials')
        DOCKER_USER        = "${DOCKER_CREDENTIALS_USR}"
        IMAGE_TAG          = "${env.GIT_COMMIT.take(8)}"

        // E-mail — NUNCA hardcoded.
        // Configurar em: Manage Jenkins > Configure System > Global properties > Environment variables
        // NOTIFY_EMAIL  = destinatario
        // SMTP_HOST     = ex: smtp.gmail.com
        // SMTP_PORT     = ex: 587
        // SMTP_USER     = remetente
        // SMTP_PASSWORD = credencial do tipo "Secret text" com ID 'smtp-password'
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {

        // ------------------------------------------------------------------ //
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // ------------------------------------------------------------------ //
        stage('Testes & Cobertura') {
            parallel {

                stage('sensor-service') {
                    steps {
                        dir('services/sensor-service') {
                            sh 'mvn test jacoco:report --batch-mode'
                        }
                    }
                    post {
                        always {
                            // Publica resultado dos testes no Jenkins
                            junit 'services/sensor-service/target/surefire-reports/*.xml'
                            // Publica relatorio HTML de cobertura
                            publishHTML(target: [
                                allowMissing         : false,
                                alwaysLinkToLastBuild: true,
                                keepAll              : true,
                                reportDir            : 'services/sensor-service/target/site/jacoco',
                                reportFiles          : 'index.html',
                                reportName           : 'Cobertura - sensor-service'
                            ])
                            // Arquiva o relatorio de cobertura como artefato
                            archiveArtifacts(
                                artifacts         : 'services/sensor-service/target/site/jacoco/**',
                                allowEmptyArchive : false
                            )
                        }
                    }
                }

                stage('alert-service') {
                    steps {
                        dir('services/alert-service') {
                            sh 'mvn test jacoco:report --batch-mode'
                        }
                    }
                    post {
                        always {
                            junit 'services/alert-service/target/surefire-reports/*.xml'
                            publishHTML(target: [
                                allowMissing         : false,
                                alwaysLinkToLastBuild: true,
                                keepAll              : true,
                                reportDir            : 'services/alert-service/target/site/jacoco',
                                reportFiles          : 'index.html',
                                reportName           : 'Cobertura - alert-service'
                            ])
                            archiveArtifacts(
                                artifacts         : 'services/alert-service/target/site/jacoco/**',
                                allowEmptyArchive : false
                            )
                        }
                    }
                }
            }
        }

        // ------------------------------------------------------------------ //
        stage('Build JARs') {
            parallel {
                stage('config-server')  {
                    steps { dir('services/config-server')  { sh 'mvn package -DskipTests --batch-mode' } }
                }
                stage('eureka-server')  {
                    steps { dir('services/eureka-server')  { sh 'mvn package -DskipTests --batch-mode' } }
                }
                stage('api-gateway')    {
                    steps { dir('services/api-gateway')    { sh 'mvn package -DskipTests --batch-mode' } }
                }
                stage('sensor-service') {
                    steps { dir('services/sensor-service') { sh 'mvn package -DskipTests --batch-mode' } }
                }
                stage('alert-service')  {
                    steps { dir('services/alert-service')  { sh 'mvn package -DskipTests --batch-mode' } }
                }
            }
            post {
                always {
                    // Arquiva os JARs gerados como artefatos do build
                    archiveArtifacts(
                        artifacts         : 'services/*/target/*.jar',
                        excludes          : 'services/*/target/*.original',
                        fingerprint       : true,
                        allowEmptyArchive : false
                    )
                }
            }
        }

        // ------------------------------------------------------------------ //
        stage('Docker Build & Push') {
            steps {
                script {
                    def services = [
                        'config-server',
                        'eureka-server',
                        'api-gateway',
                        'sensor-service',
                        'alert-service'
                    ]
                    docker.withRegistry('https://index.docker.io/v1/', 'docker-hub-credentials') {
                        services.each { svc ->
                            def image = docker.build(
                                "${DOCKER_USER}/saferoute-${svc}:${IMAGE_TAG}",
                                "services/${svc}"
                            )
                            image.push()          // push com tag do commit
                            image.push('latest')  // push como latest
                        }
                    }
                }
            }
        }

        // ------------------------------------------------------------------ //
        stage('Deploy Kubernetes') {
            // So executa na branch main
            when { branch 'main' }
            steps {
                // kubeconfig: credencial do tipo "Secret file" com ID 'kubeconfig'
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
                    sh '''
                        kubectl apply -f k8s/0-postgres-secret.yaml
                        kubectl apply -f k8s/1-postgres-db.yaml
                        kubectl apply -f k8s/4-config-server.yaml
                        kubectl apply -f k8s/5-eureka-server.yaml
                        kubectl apply -f k8s/2-sensor-service.yaml
                        kubectl apply -f k8s/3-hpa-sensor.yaml
                        kubectl apply -f k8s/6-api-gateway.yaml
                        kubectl apply -f k8s/8-hpa-gateway.yaml
                        kubectl apply -f k8s/7-alert-service.yaml
                        kubectl apply -f k8s/9-hpa-alert.yaml
                        kubectl rollout status deployment/sensor-service --timeout=120s
                        kubectl rollout status deployment/alert-service   --timeout=120s
                        kubectl rollout status deployment/api-gateway      --timeout=120s
                    '''
                }
            }
        }
    }

    // ---------------------------------------------------------------------- //
    post {
        always {
            script {
                // Envia notificacao por e-mail via script Python
                // O endereco de e-mail vem da variavel de ambiente NOTIFY_EMAIL — nunca hardcoded
                def buildResult = currentBuild.currentResult ?: 'UNKNOWN'
                withCredentials([string(credentialsId: 'smtp-password', variable: 'SMTP_PASSWORD')]) {
                    withEnv(["BUILD_STATUS=${buildResult}"]) {
                        sh 'python3 scripts/notify.py'
                    }
                }
            }
            cleanWs()
        }
    }
}
