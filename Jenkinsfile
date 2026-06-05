pipeline {
    agent any

    environment {
        // Credencial criada no Jenkins: Manage Jenkins > Credentials
        DOCKER_CREDENTIALS = credentials('docker-hub-credentials')
        DOCKER_USER        = "${DOCKER_CREDENTIALS_USR}"
        IMAGE_TAG          = "${env.GIT_COMMIT.take(8)}"
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
                            junit 'services/sensor-service/target/surefire-reports/*.xml'
                            publishHTML(target: [
                                allowMissing         : false,
                                alwaysLinkToLastBuild: true,
                                keepAll              : true,
                                reportDir            : 'services/sensor-service/target/site/jacoco',
                                reportFiles          : 'index.html',
                                reportName           : 'Cobertura - sensor-service'
                            ])
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
        }

        // ------------------------------------------------------------------ //
        stage('Docker Build & Push') {
            steps {
                script {
                    def services = ['config-server', 'eureka-server', 'api-gateway', 'sensor-service', 'alert-service']

                    docker.withRegistry('https://index.docker.io/v1/', 'docker-hub-credentials') {
                        services.each { svc ->
                            def image = docker.build(
                                "${DOCKER_USER}/saferoute-${svc}:${IMAGE_TAG}",
                                "services/${svc}"
                            )
                            image.push()
                            image.push('latest')
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
                // kubeconfig deve estar configurado no Jenkins como credencial do tipo "Secret file"
                // com ID 'kubeconfig'
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
        success {
            echo "Pipeline concluido com sucesso. Imagem: ${DOCKER_USER}/saferoute-*:${IMAGE_TAG}"
        }
        failure {
            echo "Pipeline falhou. Verifique os logs acima."
        }
        always {
            cleanWs()
        }
    }
}
