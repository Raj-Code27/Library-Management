pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 20, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout SCM') {
            steps {
                checkout scm
                echo "Building commit: ${env.GIT_COMMIT} on branch: ${env.GIT_BRANCH}"
            }
        }

        stage('Test') {
            steps {
                sh './gradlew clean test'
            }
            post {
                always {
                    junit 'build/test-results/test/*.xml'
                }
            }
        }

        stage('Build') {
            steps {
                sh './gradlew build -x test'
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            }
        }
    }

    post {
        success {
            echo "CI Pipeline completed successfully for build #${env.BUILD_NUMBER} (${env.GIT_COMMIT})"
        }
        failure {
            echo "CI Pipeline failed for build #${env.BUILD_NUMBER} (${env.GIT_COMMIT}). Please inspect test/build logs."
        }
        always {
            echo "Pipeline run #${env.BUILD_NUMBER} finished on ${new Date().toString()}"
        }
    }
}
